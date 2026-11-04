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

// ------------------------------------------------------------------ Yo nunca nunca

@Immutable
data class NeverHaveIEverState(
    val players: List<Player> = emptyList(),
    val prompt: String = "",
    val number: Int = 0,
    /** Quiénes confiesan haberlo hecho en la frase actual. */
    val confessed: Set<Long> = emptySet(),
    val totals: Map<Long, Int> = emptyMap(),
)

class NeverHaveIEverViewModel : ViewModel() {

    private val _state = MutableStateFlow(NeverHaveIEverState())
    val state: StateFlow<NeverHaveIEverState> = _state.asStateFlow()

    private val deck = Deck(Prompts.neverHaveIEver)

    fun start(players: List<Player>) {
        if (_state.value.players.isNotEmpty() || players.isEmpty()) return
        _state.value = NeverHaveIEverState(
            players = players,
            prompt = deck.draw(),
            number = 1,
            totals = players.associate { it.id to 0 },
        )
    }

    fun toggleConfessed(id: Long) = _state.update { s ->
        s.copy(confessed = if (id in s.confessed) s.confessed - id else s.confessed + id)
    }

    /** Suma las confesiones de esta frase y saca la siguiente. */
    fun next() = _state.update { s ->
        s.copy(
            prompt = deck.draw(),
            number = s.number + 1,
            confessed = emptySet(),
            totals = s.totals.mapValues { (id, n) -> if (id in s.confessed) n + 1 else n },
        )
    }
}

// ------------------------------------------------------------------ ¿Quién es más probable?

@Immutable
data class MostLikelyState(
    val players: List<Player> = emptyList(),
    val prompt: String = "",
    val number: Int = 0,
    val selectedId: Long? = null,
    val scores: Map<Long, Int> = emptyMap(),
)

class MostLikelyViewModel : ViewModel() {

    private val _state = MutableStateFlow(MostLikelyState())
    val state: StateFlow<MostLikelyState> = _state.asStateFlow()

    private val deck = Deck(Prompts.mostLikely)

    fun start(players: List<Player>) {
        if (_state.value.players.isNotEmpty() || players.isEmpty()) return
        _state.value = MostLikelyState(
            players = players,
            prompt = deck.draw(),
            number = 1,
            scores = players.associate { it.id to 0 },
        )
    }

    fun select(id: Long) = _state.update { s ->
        s.copy(selectedId = if (s.selectedId == id) null else id)
    }

    /** Da el punto al más señalado (si se eligió) y pasa a la siguiente pregunta. */
    fun next() = _state.update { s ->
        val winner = s.selectedId
        s.copy(
            prompt = deck.draw(),
            number = s.number + 1,
            selectedId = null,
            scores = if (winner == null) s.scores else s.scores + (winner to (s.scores[winner] ?: 0) + 1),
        )
    }
}
