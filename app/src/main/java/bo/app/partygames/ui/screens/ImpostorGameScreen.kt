package bo.app.partygames.ui.screens

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role as SemanticsRole
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bo.app.partygames.data.ImpostorGame
import bo.app.partygames.data.ImpostorPhase
import bo.app.partygames.data.Player
import bo.app.partygames.data.Role
import bo.app.partygames.ui.theme.CrewGreen
import bo.app.partygames.ui.theme.ImpostorRed
import bo.app.partygames.ui.viewmodel.PartyUiState
import bo.app.partygames.ui.viewmodel.PartyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImpostorGameScreen(
    viewModel: PartyViewModel,
    onExit: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val game = state.game

    // Si no hay partida (p. ej. el sistema cerró la app), volver al inicio.
    LaunchedEffect(game == null) { if (game == null) onExit() }
    if (game == null) return

    SecureWindow()
    KeepScreenOn()

    var confirmExit by rememberSaveable { mutableStateOf(false) }
    val isOver = game.phase is ImpostorPhase.GameOver
    BackHandler { if (isOver) onExit() else confirmExit = true }

    if (confirmExit) {
        AlertDialog(
            onDismissRequest = { confirmExit = false },
            title = { Text("¿Salir de la partida?") },
            text = { Text("Se perderá la partida en curso. Los jugadores se conservan.") },
            confirmButton = {
                TextButton(onClick = { confirmExit = false; onExit() }) { Text("Salir") }
            },
            dismissButton = {
                TextButton(onClick = { confirmExit = false }) { Text("Seguir jugando") }
            },
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(phaseTitle(game.phase), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { if (isOver) onExit() else confirmExit = true }) {
                        Icon(Icons.Filled.Close, contentDescription = "Salir")
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (val phase = game.phase) {
                is ImpostorPhase.Reveal -> RevealPhase(
                    state = state,
                    game = game,
                    phase = phase,
                    onPeek = viewModel::markRolePeeked,
                    onNext = viewModel::nextReveal,
                )
                is ImpostorPhase.Discussion -> DiscussionPhase(
                    state = state,
                    game = game,
                    phase = phase,
                    onToggleTimer = viewModel::toggleTimer,
                    onVote = viewModel::goToVoting,
                )
                is ImpostorPhase.Voting -> VotingPhase(
                    state = state,
                    phase = phase,
                    onSelect = viewModel::selectVote,
                    onConfirm = viewModel::confirmElimination,
                )
                is ImpostorPhase.Elimination -> EliminationPhase(
                    state = state,
                    game = game,
                    phase = phase,
                    onContinue = viewModel::continueAfterElimination,
                )
                is ImpostorPhase.GameOver -> GameOverPhase(
                    state = state,
                    game = game,
                    phase = phase,
                    onPlayAgain = { viewModel.startImpostorGame() },
                    onHome = onExit,
                )
            }
        }
    }
}

private fun phaseTitle(phase: ImpostorPhase): String = when (phase) {
    is ImpostorPhase.Reveal -> "Reparto de roles"
    is ImpostorPhase.Discussion -> "Ronda ${phase.round}: debate"
    is ImpostorPhase.Voting -> "Ronda ${phase.round}: votación"
    is ImpostorPhase.Elimination -> "Ronda ${phase.round}: resultado"
    is ImpostorPhase.GameOver -> "Fin de la partida"
}

// --------------------------------------------------------------------------- Pantalla 2: revelar rol

@Composable
private fun RevealPhase(
    state: PartyUiState,
    game: ImpostorGame,
    phase: ImpostorPhase.Reveal,
    onPeek: () -> Unit,
    onNext: () -> Unit,
) {
    val player = state.players.getOrNull(phase.playerIndex) ?: return
    val isLast = phase.playerIndex == state.players.lastIndex

    PhaseColumn {
        LinearProgressIndicator(
            progress = { (phase.playerIndex + 1f) / state.players.size },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            "Jugador ${phase.playerIndex + 1} de ${state.players.size}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Text("Pasa el teléfono a", style = MaterialTheme.typography.titleMedium)
        Text(
            player.name,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))

        // key: al cambiar de jugador se reinicia el estado de presión.
        key(player.id) {
            HoldToReveal(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .aspectRatio(1f, matchHeightConstraintsFirst = true),
                onPressStart = onPeek,
            ) {
                RoleContent(game = game, role = game.roleOf(player.id))
            }
        }

        Text(
            if (phase.hasPeeked) "Suelta, y cuando nadie mire, pasa al siguiente."
            else "Nadie más debe mirar la pantalla.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onNext,
            enabled = phase.hasPeeked,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Text(if (isLast) "Listo, empezar el debate" else "Listo, pasar el teléfono", fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Tarjeta que solo muestra su contenido mientras el dedo la mantiene presionada.
 * Al soltar (o si el gesto se cancela) vuelve a ocultarse.
 */
@Composable
private fun HoldToReveal(
    modifier: Modifier,
    onPressStart: () -> Unit,
    content: @Composable () -> Unit,
) {
    var pressed by remember { mutableStateOf(false) }
    val container by animateColorAsState(
        if (pressed) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.primaryContainer,
        label = "revealColor",
    )
    Card(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    pressed = true
                    onPressStart()
                    try {
                        tryAwaitRelease()
                    } finally {
                        pressed = false
                    }
                },
            )
        },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = container),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (pressed) {
                content()
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Mantén presionado\npara revelar tu rol",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

@Composable
private fun RoleContent(game: ImpostorGame, role: Role) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        when (role) {
            Role.IMPOSTOR -> {
                Text(
                    "ERES EL\nIMPOSTOR",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = ImpostorRed,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    if (game.impostorKnowsCategory) "Pista: la categoría es «${game.card.category}»"
                    else "No conoces la palabra. ¡Disimula!",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
            }
            Role.CREW -> {
                Text(
                    "La palabra secreta es",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    game.card.word,
                    fontSize = 38.sp,
                    lineHeight = 42.sp,
                    fontWeight = FontWeight.Black,
                    color = CrewGreen,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Categoría: ${game.card.category}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// --------------------------------------------------------------------------- Pantalla 3: debate y votación

@Composable
private fun DiscussionPhase(
    state: PartyUiState,
    game: ImpostorGame,
    phase: ImpostorPhase.Discussion,
    onToggleTimer: () -> Unit,
    onVote: () -> Unit,
) {
    val starter = state.playerById(phase.starterId)
    PhaseColumn {
        Text(
            "Cada uno dice una palabra relacionada con la palabra secreta, sin decirla. " +
                "Descubran quién no la conoce.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (starter != null) {
            Text("Empieza", style = MaterialTheme.typography.labelLarge)
            Text(
                starter.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.secondary,
            )
        }

        if (state.discussionSeconds > 0) {
            val timeUp = phase.secondsLeft == 0
            Text(
                formatSeconds(phase.secondsLeft),
                fontSize = 64.sp,
                fontWeight = FontWeight.Black,
                color = if (timeUp) ImpostorRed else MaterialTheme.colorScheme.onSurface,
            )
            if (timeUp) {
                Text("¡Se acabó el tiempo! A votar.", color = ImpostorRed, fontWeight = FontWeight.Bold)
            } else {
                OutlinedButton(onClick = onToggleTimer) {
                    if (!phase.timerRunning) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (phase.timerRunning) "Pausar" else "Reanudar")
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(state.players, key = { it.id }) { p ->
                val alive = game.isAlive(p.id)
                PlayerStatusRow(player = p, alive = alive, revealedRole = if (alive) null else game.roleOf(p.id))
            }
        }

        Button(
            onClick = onVote,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Text("Ir a votación", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun VotingPhase(
    state: PartyUiState,
    phase: ImpostorPhase.Voting,
    onSelect: (Long) -> Unit,
    onConfirm: () -> Unit,
) {
    var confirm by rememberSaveable { mutableStateOf(false) }
    val selected = phase.selectedId?.let(state::playerById)

    if (confirm && selected != null) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("¿Expulsar a ${selected.name}?") },
            text = { Text("Se revelará si era el impostor. No se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { confirm = false; onConfirm() }) { Text("Expulsar") }
            },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text("Cancelar") } },
        )
    }

    PhaseColumn {
        Text(
            "Voten en grupo y elijan a quién expulsar.",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.alivePlayers, key = { it.id }) { p ->
                val isSelected = p.id == phase.selectedId
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = isSelected,
                            onClick = { onSelect(p.id) },
                            role = SemanticsRole.RadioButton,
                        ),
                    border = if (isSelected) BorderStroke(2.dp, ImpostorRed) else null,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = isSelected, onClick = null)
                        Spacer(Modifier.width(12.dp))
                        Text(p.name, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
        Button(
            onClick = { confirm = true },
            enabled = selected != null,
            colors = ButtonDefaults.buttonColors(containerColor = ImpostorRed, contentColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Text(selected?.let { "Expulsar a ${it.name}" } ?: "Elige a un jugador", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EliminationPhase(
    state: PartyUiState,
    game: ImpostorGame,
    phase: ImpostorPhase.Elimination,
    onContinue: () -> Unit,
) {
    val name = state.playerById(phase.eliminatedId)?.name ?: "?"
    val impostorsLeft = game.impostorIds.count { game.isAlive(it) }
    PhaseColumn(verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)) {
        Text(name, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
        Text(
            if (phase.wasImpostor) "¡ERA IMPOSTOR!" else "Era inocente",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = if (phase.wasImpostor) CrewGreen else ImpostorRed,
        )
        if (game.impostorIds.size > 1) {
            Text(
                "Impostores restantes: $impostorsLeft",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Text("Continuar", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun GameOverPhase(
    state: PartyUiState,
    game: ImpostorGame,
    phase: ImpostorPhase.GameOver,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit,
) {
    val crewWon = phase.winner == Role.CREW
    val impostorNames = game.impostorIds.mapNotNull { state.playerById(it)?.name }
    PhaseColumn(verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    (if (crewWon) CrewGreen else ImpostorRed).copy(alpha = 0.15f),
                    RoundedCornerShape(24.dp),
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                if (crewWon) "¡Ganan los inocentes!" else "¡Gana el impostor!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = if (crewWon) CrewGreen else ImpostorRed,
                textAlign = TextAlign.Center,
            )
        }
        Text("La palabra era", style = MaterialTheme.typography.titleMedium)
        Text(
            game.card.word,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
        )
        Text("Categoría: ${game.card.category}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            (if (impostorNames.size > 1) "Impostores: " else "Impostor: ") + impostorNames.joinToString(", "),
            style = MaterialTheme.typography.titleMedium,
            color = ImpostorRed,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onPlayAgain,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Jugar otra vez", fontWeight = FontWeight.Bold)
        }
        OutlinedButton(onClick = onHome, modifier = Modifier.fillMaxWidth()) {
            Text("Volver al inicio")
        }
    }
}

// --------------------------------------------------------------------------- Comunes

@Composable
private fun PhaseColumn(
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(10.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = verticalArrangement,
        content = content,
    )
}

@Composable
private fun PlayerStatusRow(player: Player, alive: Boolean, revealedRole: Role?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            player.name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            textDecoration = if (alive) null else TextDecoration.LineThrough,
            color = if (alive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (!alive && revealedRole != null) {
            Text(
                if (revealedRole == Role.IMPOSTOR) "Impostor" else "Inocente",
                style = MaterialTheme.typography.labelMedium,
                color = if (revealedRole == Role.IMPOSTOR) ImpostorRed else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatSeconds(total: Int): String = "%d:%02d".format(total / 60, total % 60)

/** Impide capturas de pantalla y oculta la miniatura en Recientes mientras hay roles secretos. */
@Composable
private fun SecureWindow() {
    val activity = LocalContext.current as? Activity ?: return
    DisposableEffect(activity) {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose { activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
    }
}

@Composable
private fun KeepScreenOn() {
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
}
