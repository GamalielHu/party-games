package bo.app.partygames.ui.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import bo.app.partygames.data.Deck
import bo.app.partygames.data.Player
import bo.app.partygames.data.Prompts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class TruthOrDareKind(val label: String) { TRUTH("Verdad"), DARE("Reto") }

@Immutable
sealed interface TruthOrDarePhase {
    data object Choose : TruthOrDarePhase
    data class Card(val kind: TruthOrDareKind, val text: String) : TruthOrDarePhase
}

@Immutable
data class TruthOrDareState(
    val players: List<Player> = emptyList(),
    val turn: Int = 0,
    val phase: TruthOrDarePhase = TruthOrDarePhase.Choose,
    val scores: Map<Long, Int> = emptyMap(),
) {
    val currentPlayer: Player? get() = players.getOrNull(turn % players.size.coerceAtLeast(1))
    val round: Int get() = if (players.isEmpty()) 1 else turn / players.size + 1
}

class TruthOrDareViewModel : ViewModel() {

    private val _state = MutableStateFlow(TruthOrDareState())
    val state: StateFlow<TruthOrDareState> = _state.asStateFlow()

    private val truths = Deck(Prompts.truths)
    private val dares = Deck(Prompts.dares)

    /** Solo inicia una vez por pantalla (sobrevive a rotaciones). */
    fun start(players: List<Player>) {
        if (_state.value.players.isNotEmpty() || players.isEmpty()) return
        _state.value = TruthOrDareState(
            players = players.shuffled(),
            scores = players.associate { it.id to 0 },
        )
    }

    fun choose(kind: TruthOrDareKind) {
        if (_state.value.phase != TruthOrDarePhase.Choose) return
        _state.update { it.copy(phase = TruthOrDarePhase.Card(kind, draw(kind))) }
    }

    /** Otra carta del mismo tipo (sin coste). */
    fun reroll() = _state.update { s ->
        val card = s.phase as? TruthOrDarePhase.Card ?: return@update s
        s.copy(phase = card.copy(text = draw(card.kind)))
    }

    /** [completed]: el jugador cumplió; suma un punto (los retos valen 2). */
    fun resolve(completed: Boolean) = _state.update { s ->
        val card = s.phase as? TruthOrDarePhase.Card ?: return@update s
        val player = s.currentPlayer ?: return@update s
        val points = if (!completed) 0 else if (card.kind == TruthOrDareKind.DARE) 2 else 1
        s.copy(
            turn = s.turn + 1,
            phase = TruthOrDarePhase.Choose,
            scores = s.scores + (player.id to (s.scores[player.id] ?: 0) + points),
        )
    }

    private fun draw(kind: TruthOrDareKind): String = when (kind) {
        TruthOrDareKind.TRUTH -> truths.draw()
        TruthOrDareKind.DARE -> dares.draw()
    }
}
