package bo.app.partygames.ui.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bo.app.partygames.data.GameMode
import bo.app.partygames.data.ImpostorGame
import bo.app.partygames.data.ImpostorPhase
import bo.app.partygames.data.Player
import bo.app.partygames.data.Role
import bo.app.partygames.data.WordBank
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

@Immutable
data class PartyUiState(
    val players: List<Player> = emptyList(),
    val nameInput: String = "",
    val nameError: String? = null,
    val selectedMode: GameMode = GameMode.IMPOSTOR,
    val impostorCount: Int = 1,
    val impostorKnowsCategory: Boolean = true,
    val discussionSeconds: Int = DEFAULT_DISCUSSION_SECONDS,
    val game: ImpostorGame? = null,
) {
    val maxImpostors: Int get() = maxImpostorsFor(players.size)
    val canStart: Boolean get() = players.size >= selectedMode.minPlayers
    val playersMissing: Int get() = (selectedMode.minPlayers - players.size).coerceAtLeast(0)

    val alivePlayers: List<Player>
        get() = game?.let { g -> players.filter { g.isAlive(it.id) } } ?: players

    fun playerById(id: Long): Player? = players.firstOrNull { it.id == id }

    companion object {
        const val MAX_PLAYERS = 20
        const val MAX_NAME_LENGTH = 20
        const val DEFAULT_DISCUSSION_SECONDS = 180
        val DISCUSSION_OPTIONS = listOf(60, 120, 180, 300, 0) // 0 = sin límite

        /** Los impostores siempre deben ser minoría estricta. */
        fun maxImpostorsFor(players: Int): Int = ((players - 1) / 2).coerceAtLeast(1)
    }
}

class PartyViewModel(
    private val random: Random = Random.Default,
) : ViewModel() {

    private val _state = MutableStateFlow(PartyUiState())
    val state: StateFlow<PartyUiState> = _state.asStateFlow()

    private var nextId = 1L
    private var timerJob: Job? = null

    // ---------------------------------------------------------------- Inicio

    fun onNameInputChange(value: String) {
        _state.update {
            it.copy(nameInput = value.take(PartyUiState.MAX_NAME_LENGTH), nameError = null)
        }
    }

    fun addPlayer() {
        _state.update { s ->
            val name = s.nameInput.trim().replace(Regex("\\s+"), " ")
            val error = when {
                name.isEmpty() -> "Escribe un nombre"
                s.players.size >= PartyUiState.MAX_PLAYERS -> "Máximo ${PartyUiState.MAX_PLAYERS} jugadores"
                s.players.any { it.name.equals(name, ignoreCase = true) } -> "\"$name\" ya está en la lista"
                else -> null
            }
            if (error != null) {
                s.copy(nameError = error)
            } else {
                val players = s.players + Player(nextId++, name)
                s.copy(
                    players = players,
                    nameInput = "",
                    nameError = null,
                    impostorCount = s.impostorCount.coerceAtMost(PartyUiState.maxImpostorsFor(players.size)),
                )
            }
        }
    }

    fun removePlayer(id: Long) {
        _state.update { s ->
            val players = s.players.filterNot { it.id == id }
            s.copy(
                players = players,
                impostorCount = s.impostorCount
                    .coerceAtMost(PartyUiState.maxImpostorsFor(players.size))
                    .coerceAtLeast(1),
            )
        }
    }

    fun selectMode(mode: GameMode) = _state.update { it.copy(selectedMode = mode) }

    fun setImpostorCount(count: Int) = _state.update {
        it.copy(impostorCount = count.coerceIn(1, it.maxImpostors))
    }

    fun setImpostorKnowsCategory(value: Boolean) = _state.update { it.copy(impostorKnowsCategory = value) }

    fun setDiscussionSeconds(seconds: Int) = _state.update { it.copy(discussionSeconds = seconds.coerceAtLeast(0)) }

    // ---------------------------------------------------------------- Partida

    /** Reparte roles nuevos. Devuelve false si no hay jugadores suficientes. */
    fun startImpostorGame(): Boolean {
        val s = _state.value
        if (!s.canStart) return false
        stopTimer()
        val impostors = s.players.shuffled(random).take(s.impostorCount).map { it.id }.toSet()
        val roles = s.players.associate { p -> p.id to if (p.id in impostors) Role.IMPOSTOR else Role.CREW }
        val game = ImpostorGame(
            card = WordBank.randomCard(random, avoid = s.game?.card),
            roles = roles,
            eliminatedIds = emptySet(),
            impostorKnowsCategory = s.impostorKnowsCategory,
            phase = ImpostorPhase.Reveal(playerIndex = 0, hasPeeked = false),
        )
        _state.update { it.copy(game = game) }
        return true
    }

    /** El jugador actual mantuvo presionado y vio su rol al menos una vez. */
    fun markRolePeeked() = updatePhase<ImpostorPhase.Reveal> { it.copy(hasPeeked = true) }

    fun nextReveal() {
        val s = _state.value
        val game = s.game ?: return
        val phase = game.phase as? ImpostorPhase.Reveal ?: return
        if (!phase.hasPeeked) return
        val next = phase.playerIndex + 1
        if (next < s.players.size) {
            setPhase(ImpostorPhase.Reveal(next, hasPeeked = false))
        } else {
            startDiscussion(round = 1)
        }
    }

    private fun startDiscussion(round: Int) {
        val s = _state.value
        val alive = s.alivePlayers
        if (alive.isEmpty()) return
        setPhase(
            ImpostorPhase.Discussion(
                round = round,
                starterId = alive.random(random).id,
                secondsLeft = s.discussionSeconds,
                timerRunning = s.discussionSeconds > 0,
            )
        )
        if (s.discussionSeconds > 0) startTimer()
    }

    fun toggleTimer() {
        val phase = _state.value.game?.phase as? ImpostorPhase.Discussion ?: return
        if (phase.secondsLeft <= 0) return
        if (phase.timerRunning) {
            stopTimer()
            updatePhase<ImpostorPhase.Discussion> { it.copy(timerRunning = false) }
        } else {
            updatePhase<ImpostorPhase.Discussion> { it.copy(timerRunning = true) }
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1_000)
                val phase = _state.value.game?.phase as? ImpostorPhase.Discussion ?: break
                if (!phase.timerRunning) break
                val left = (phase.secondsLeft - 1).coerceAtLeast(0)
                updatePhase<ImpostorPhase.Discussion> {
                    it.copy(secondsLeft = left, timerRunning = left > 0)
                }
                if (left == 0) break
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun goToVoting() {
        val phase = _state.value.game?.phase as? ImpostorPhase.Discussion ?: return
        stopTimer()
        setPhase(ImpostorPhase.Voting(round = phase.round, selectedId = null))
    }

    fun selectVote(id: Long) {
        val game = _state.value.game ?: return
        if (!game.isAlive(id)) return
        updatePhase<ImpostorPhase.Voting> { it.copy(selectedId = if (it.selectedId == id) null else id) }
    }

    fun confirmElimination() {
        val game = _state.value.game ?: return
        val phase = game.phase as? ImpostorPhase.Voting ?: return
        val target = phase.selectedId ?: return
        if (!game.isAlive(target)) return
        _state.update { s ->
            val g = s.game ?: return@update s
            s.copy(
                game = g.copy(
                    eliminatedIds = g.eliminatedIds + target,
                    phase = ImpostorPhase.Elimination(
                        round = phase.round,
                        eliminatedId = target,
                        wasImpostor = g.roleOf(target) == Role.IMPOSTOR,
                    ),
                )
            )
        }
    }

    /** Tras mostrar la expulsión: decide si termina la partida o sigue otra ronda. */
    fun continueAfterElimination() {
        val game = _state.value.game ?: return
        val phase = game.phase as? ImpostorPhase.Elimination ?: return
        val alive = game.roles.keys.filter { game.isAlive(it) }
        val impostorsAlive = alive.count { game.roleOf(it) == Role.IMPOSTOR }
        val crewAlive = alive.size - impostorsAlive
        when {
            impostorsAlive == 0 -> setPhase(ImpostorPhase.GameOver(winner = Role.CREW))
            impostorsAlive >= crewAlive -> setPhase(ImpostorPhase.GameOver(winner = Role.IMPOSTOR))
            else -> startDiscussion(round = phase.round + 1)
        }
    }

    /** Termina la partida actual y vuelve a la configuración (se conservan los jugadores). */
    fun exitGame() {
        stopTimer()
        _state.update { it.copy(game = null) }
    }

    // ---------------------------------------------------------------- Utilidades

    private fun setPhase(phase: ImpostorPhase) {
        _state.update { s -> s.game?.let { s.copy(game = it.copy(phase = phase)) } ?: s }
    }

    private inline fun <reified T : ImpostorPhase> updatePhase(crossinline transform: (T) -> ImpostorPhase) {
        _state.update { s ->
            val g = s.game ?: return@update s
            val p = g.phase as? T ?: return@update s
            s.copy(game = g.copy(phase = transform(p)))
        }
    }

    override fun onCleared() {
        stopTimer()
        super.onCleared()
    }
}
