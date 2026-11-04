package bo.app.partygames.ui.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bo.app.partygames.data.Deck
import bo.app.partygames.data.Player
import bo.app.partygames.data.Prompts
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Immutable
data class CharadesTeam(
    val name: String,
    val members: List<Player>,
)

@Immutable
sealed interface CharadesPhase {
    /** Antes del turno: se entrega el teléfono al actor. */
    data object Ready : CharadesPhase

    data class Playing(val word: String, val secondsLeft: Int) : CharadesPhase

    data object TurnSummary : CharadesPhase

    data object Finished : CharadesPhase
}

@Immutable
data class CharadesState(
    val teams: List<CharadesTeam> = emptyList(),
    val scores: List<Int> = emptyList(),
    val turnsPlayed: Int = 0,
    val roundsPerTeam: Int = PartyUiState.DEFAULT_CHARADES_ROUNDS,
    val secondsPerTurn: Int = PartyUiState.DEFAULT_CHARADES_SECONDS,
    val phase: CharadesPhase = CharadesPhase.Ready,
    val guessed: List<String> = emptyList(),
    val passed: List<String> = emptyList(),
) {
    val started: Boolean get() = teams.isNotEmpty()
    val totalTurns: Int get() = roundsPerTeam * teams.size
    val currentTeamIndex: Int get() = if (teams.isEmpty()) 0 else turnsPlayed % teams.size
    val currentTeam: CharadesTeam? get() = teams.getOrNull(currentTeamIndex)
    val currentRound: Int get() = if (teams.isEmpty()) 1 else turnsPlayed / teams.size + 1

    /** Dentro de cada equipo los actores rotan ronda a ronda. */
    val currentActor: Player?
        get() {
            val team = currentTeam ?: return null
            if (team.members.isEmpty()) return null
            return team.members[(turnsPlayed / teams.size) % team.members.size]
        }
}

class CharadesViewModel : ViewModel() {

    private val _state = MutableStateFlow(CharadesState())
    val state: StateFlow<CharadesState> = _state.asStateFlow()

    private val deck = Deck(Prompts.charades)
    private var timerJob: Job? = null

    fun start(players: List<Player>, roundsPerTeam: Int, secondsPerTurn: Int) {
        if (_state.value.started || players.size < 2) return
        _state.value = CharadesState(
            teams = splitTeams(players),
            scores = listOf(0, 0),
            roundsPerTeam = roundsPerTeam.coerceAtLeast(1),
            secondsPerTurn = secondsPerTurn.coerceAtLeast(10),
        )
    }

    /** Solo antes del primer turno. */
    fun reshuffleTeams() = _state.update { s ->
        if (s.turnsPlayed != 0 || s.phase != CharadesPhase.Ready) return@update s
        s.copy(teams = splitTeams(s.teams.flatMap { it.members }))
    }

    fun startTurn() {
        val s = _state.value
        if (s.phase != CharadesPhase.Ready || s.turnsPlayed >= s.totalTurns) return
        _state.update {
            it.copy(
                phase = CharadesPhase.Playing(deck.draw(), it.secondsPerTurn),
                guessed = emptyList(),
                passed = emptyList(),
            )
        }
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1_000)
                val playing = _state.value.phase as? CharadesPhase.Playing ?: break
                val left = playing.secondsLeft - 1
                if (left <= 0) {
                    endTurn()
                    break
                }
                _state.update { st ->
                    val p = st.phase as? CharadesPhase.Playing ?: return@update st
                    st.copy(phase = p.copy(secondsLeft = left))
                }
            }
        }
    }

    fun guessed() = advanceWord(guessed = true)

    fun pass() = advanceWord(guessed = false)

    private fun advanceWord(guessed: Boolean) = _state.update { s ->
        val p = s.phase as? CharadesPhase.Playing ?: return@update s
        val team = s.currentTeamIndex
        s.copy(
            phase = p.copy(word = deck.draw()),
            guessed = if (guessed) s.guessed + p.word else s.guessed,
            passed = if (guessed) s.passed else s.passed + p.word,
            scores = if (guessed) s.scores.mapIndexed { i, v -> if (i == team) v + 1 else v } else s.scores,
        )
    }

    /** Termina el turno antes de tiempo (o al acabarse el reloj). */
    fun endTurn() {
        timerJob?.cancel()
        timerJob = null
        _state.update { s ->
            if (s.phase !is CharadesPhase.Playing) s
            else s.copy(phase = CharadesPhase.TurnSummary, turnsPlayed = s.turnsPlayed + 1)
        }
    }

    fun continueAfterSummary() = _state.update { s ->
        if (s.phase != CharadesPhase.TurnSummary) return@update s
        s.copy(phase = if (s.turnsPlayed >= s.totalTurns) CharadesPhase.Finished else CharadesPhase.Ready)
    }

    /** Revancha con los mismos equipos. */
    fun playAgain() = _state.update { s ->
        s.copy(
            scores = List(s.teams.size) { 0 },
            turnsPlayed = 0,
            phase = CharadesPhase.Ready,
            guessed = emptyList(),
            passed = emptyList(),
        )
    }

    private fun splitTeams(players: List<Player>): List<CharadesTeam> {
        val shuffled = players.shuffled()
        return listOf(
            CharadesTeam("Equipo Rojo", shuffled.filterIndexed { i, _ -> i % 2 == 0 }),
            CharadesTeam("Equipo Azul", shuffled.filterIndexed { i, _ -> i % 2 == 1 }),
        )
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
