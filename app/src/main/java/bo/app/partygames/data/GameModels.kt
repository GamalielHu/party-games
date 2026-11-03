package bo.app.partygames.data

import androidx.compose.runtime.Immutable

@Immutable
data class Player(
    val id: Long,
    val name: String,
)

enum class GameMode(
    val title: String,
    val description: String,
    val minPlayers: Int,
) {
    IMPOSTOR(
        title = "El Impostor",
        description = "Todos reciben la misma palabra secreta, menos el impostor. " +
            "Descubran quién finge saberla antes de que sea tarde.",
        minPlayers = 3,
    ),
}

enum class Role { CREW, IMPOSTOR }

@Immutable
data class SecretCard(
    val category: String,
    val word: String,
)

/** Fases del modo El Impostor. Cada fase es inmutable y lleva solo lo que su pantalla necesita. */
@Immutable
sealed interface ImpostorPhase {
    /** Se pasa el teléfono a [playerIndex] (índice en la lista de jugadores). */
    data class Reveal(val playerIndex: Int, val hasPeeked: Boolean) : ImpostorPhase

    /** Debate en voz alta. [starterId] empieza a hablar. */
    data class Discussion(
        val round: Int,
        val starterId: Long,
        val secondsLeft: Int,
        val timerRunning: Boolean,
    ) : ImpostorPhase

    /** El grupo elige a quién expulsar. */
    data class Voting(val round: Int, val selectedId: Long?) : ImpostorPhase

    /** Resultado de la expulsión de esta ronda. */
    data class Elimination(val round: Int, val eliminatedId: Long, val wasImpostor: Boolean) : ImpostorPhase

    /** Fin de partida. */
    data class GameOver(val winner: Role) : ImpostorPhase
}

@Immutable
data class ImpostorGame(
    val card: SecretCard,
    val roles: Map<Long, Role>,
    val eliminatedIds: Set<Long>,
    val impostorKnowsCategory: Boolean,
    val phase: ImpostorPhase,
) {
    fun roleOf(id: Long): Role = roles[id] ?: Role.CREW
    val impostorIds: Set<Long> get() = roles.filterValues { it == Role.IMPOSTOR }.keys
    fun isAlive(id: Long): Boolean = id !in eliminatedIds
}
