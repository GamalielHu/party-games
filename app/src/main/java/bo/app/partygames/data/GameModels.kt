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
    TRUTH_OR_DARE(
        title = "Verdad o Reto",
        description = "Por turnos, cada jugador elige: responder con la verdad o cumplir un reto. " +
            "Gana quien cumpla más.",
        minPlayers = 2,
    ),
    CHARADES(
        title = "Charadas por equipos",
        description = "Dos equipos. Uno actúa la palabra sin hablar y su equipo adivina antes " +
            "de que se acabe el tiempo.",
        minPlayers = 4,
    ),
    NEVER_HAVE_I_EVER(
        title = "Yo nunca nunca",
        description = "Sale una frase; quien sí lo hizo lo confiesa. Al final se ve quién tiene " +
            "más historias.",
        minPlayers = 2,
    ),
    MOST_LIKELY(
        title = "¿Quién es más probable?",
        description = "Sale una pregunta, todos señalan a la vez y el más señalado se lleva el punto.",
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

/** Con quién se juega: cambia qué juegos aparecen y qué frases salen. */
enum class Audience(
    val title: String,
    val description: String,
    /** Texto de castigo para quien se rinde/confiesa, o null si no hay. */
    val penalty: String?,
) {
    PARTY(
        title = "Fiesta / grupo",
        description = "Varios, con coqueteo entre todos, confesiones y tragos. +18",
        penalty = "toma un trago",
    ),
    COUPLE(
        title = "Pareja",
        description = "Solo ustedes dos: preguntas íntimas, románticas y retos atrevidos. +18",
        penalty = "cumple un castigo que elija el otro",
    ),
    FRIENDS(
        title = "Entre amigos / amigas",
        description = "Chismes, exes, crushes y confesiones, sin retos de coquetear entre ustedes. +18",
        penalty = "toma un trago",
    ),
    FAMILY(
        title = "Familia / suave",
        description = "Versión tranquila, apta para todo público.",
        penalty = null,
    ),
}

/** Juegos disponibles según el grupo (en pareja no hay impostor ni equipos). */
fun GameMode.isAvailableFor(audience: Audience): Boolean =
    audience != Audience.COUPLE || this != GameMode.IMPOSTOR && this != GameMode.CHARADES

/** Mínimo de jugadores según el grupo: en pareja todo se juega de a dos. */
fun GameMode.minPlayersFor(audience: Audience): Int =
    if (audience == Audience.COUPLE) 2 else minPlayers
