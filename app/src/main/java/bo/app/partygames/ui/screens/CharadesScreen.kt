package bo.app.partygames.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import bo.app.partygames.data.Player
import bo.app.partygames.ui.components.GameScaffold
import bo.app.partygames.ui.theme.CrewGreen
import bo.app.partygames.ui.theme.ImpostorRed
import bo.app.partygames.ui.viewmodel.CharadesPhase
import bo.app.partygames.ui.viewmodel.CharadesState
import bo.app.partygames.ui.viewmodel.CharadesViewModel

private val TeamBlue = Color(0xFF60A5FA)
private fun teamColor(index: Int): Color = if (index == 0) ImpostorRed else TeamBlue

@Composable
fun CharadesScreen(
    players: List<Player>,
    roundsPerTeam: Int,
    secondsPerTurn: Int,
    onExit: () -> Unit,
    viewModel: CharadesViewModel = viewModel(),
) {
    LaunchedEffect(Unit) { viewModel.start(players, roundsPerTeam, secondsPerTurn) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    if (!state.started) return

    val title = when (state.phase) {
        CharadesPhase.Finished -> "Charadas · Final"
        else -> "Charadas · Ronda ${state.currentRound.coerceAtMost(state.roundsPerTeam)} de ${state.roundsPerTeam}"
    }
    GameScaffold(title = title, onExit = onExit, confirmExit = state.phase != CharadesPhase.Finished) {
        when (val phase = state.phase) {
            CharadesPhase.Ready -> ReadyPhase(
                state = state,
                onStart = viewModel::startTurn,
                onReshuffle = viewModel::reshuffleTeams,
            )
            is CharadesPhase.Playing -> PlayingPhase(
                state = state,
                phase = phase,
                onGuessed = viewModel::guessed,
                onPass = viewModel::pass,
                onEnd = viewModel::endTurn,
            )
            CharadesPhase.TurnSummary -> SummaryPhase(state = state, onContinue = viewModel::continueAfterSummary)
            CharadesPhase.Finished -> FinishedPhase(state = state, onAgain = viewModel::playAgain, onHome = onExit)
        }
    }
}

@Composable
private fun ColumnScope.ReadyPhase(state: CharadesState, onStart: () -> Unit, onReshuffle: () -> Unit) {
    val team = state.currentTeam ?: return
    val actor = state.currentActor
    val color = teamColor(state.currentTeamIndex)

    Text(team.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = color)
    if (actor != null) {
        Text("Actúa", style = MaterialTheme.typography.titleMedium)
        Text(
            actor.name,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
        )
    }
    Text(
        "Pásale el teléfono. Solo esa persona mira la pantalla y actúa sin hablar; su equipo adivina. " +
            "Tienen ${state.secondsPerTurn} segundos.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
    TeamsBoard(state = state, modifier = Modifier.weight(1f))
    if (state.turnsPlayed == 0) {
        TextButton(onClick = onReshuffle) { Text("Rehacer equipos") }
    }
    Button(
        onClick = onStart,
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.White),
        modifier = Modifier.fillMaxWidth().height(56.dp),
    ) { Text("Empezar turno", fontWeight = FontWeight.Bold) }
}

@Composable
private fun ColumnScope.PlayingPhase(
    state: CharadesState,
    phase: CharadesPhase.Playing,
    onGuessed: () -> Unit,
    onPass: () -> Unit,
    onEnd: () -> Unit,
) {
    val urgent = phase.secondsLeft <= 10
    Text(
        "${phase.secondsLeft}",
        fontSize = 56.sp,
        fontWeight = FontWeight.Black,
        color = if (urgent) ImpostorRed else MaterialTheme.colorScheme.onSurface,
    )
    Text(
        "Aciertos: ${state.guessed.size}",
        style = MaterialTheme.typography.titleMedium,
        color = CrewGreen,
    )
    Column(
        modifier = Modifier.weight(1f).fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            phase.word,
            fontSize = 44.sp,
            lineHeight = 50.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = onPass, modifier = Modifier.weight(1f).height(72.dp)) {
            Text("Pasar", style = MaterialTheme.typography.titleMedium)
        }
        Button(
            onClick = onGuessed,
            colors = ButtonDefaults.buttonColors(containerColor = CrewGreen, contentColor = Color.Black),
            modifier = Modifier.weight(1f).height(72.dp),
        ) {
            Text("¡Adivinaron!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
    TextButton(onClick = onEnd) { Text("Terminar turno") }
}

@Composable
private fun ColumnScope.SummaryPhase(state: CharadesState, onContinue: () -> Unit) {
    // turnsPlayed ya avanzó: el equipo que acaba de jugar es el anterior.
    val lastTeam = (state.turnsPlayed - 1).mod(state.teams.size)
    Text("¡Tiempo!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
    Text(
        "${state.teams[lastTeam].name}: ${state.guessed.size} acierto(s)",
        style = MaterialTheme.typography.titleMedium,
        color = teamColor(lastTeam),
    )
    LazyColumn(
        modifier = Modifier.fillMaxWidth().weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(vertical = 4.dp),
    ) {
        items(state.guessed) { w -> Text("✓ $w", color = CrewGreen, style = MaterialTheme.typography.bodyLarge) }
        items(state.passed) { w ->
            Text(
                w,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textDecoration = TextDecoration.LineThrough,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
    TeamsBoard(state = state, compact = true)
    Button(onClick = onContinue, modifier = Modifier.fillMaxWidth().height(52.dp)) {
        Text(if (state.turnsPlayed >= state.totalTurns) "Ver resultado" else "Siguiente equipo", fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ColumnScope.FinishedPhase(state: CharadesState, onAgain: () -> Unit, onHome: () -> Unit) {
    val best = state.scores.maxOrNull() ?: 0
    val winners = state.scores.indices.filter { state.scores[it] == best }
    val tie = winners.size > 1
    Spacer(Modifier.weight(0.5f))
    Text(
        if (tie) "¡Empate!" else "¡Gana el ${state.teams[winners.first()].name}!",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Black,
        color = if (tie) MaterialTheme.colorScheme.secondary else teamColor(winners.first()),
        textAlign = TextAlign.Center,
    )
    TeamsBoard(state = state)
    Spacer(Modifier.weight(0.5f))
    Button(onClick = onAgain, modifier = Modifier.fillMaxWidth().height(52.dp)) {
        Text("Revancha", fontWeight = FontWeight.Bold)
    }
    OutlinedButton(onClick = onHome, modifier = Modifier.fillMaxWidth()) { Text("Volver al inicio") }
}

@Composable
private fun TeamsBoard(state: CharadesState, modifier: Modifier = Modifier, compact: Boolean = false) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        state.teams.forEachIndexed { i, team ->
            val color = teamColor(i)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(color.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(team.name, color = color, fontWeight = FontWeight.Bold)
                Text("${state.scores.getOrElse(i) { 0 }}", fontSize = 36.sp, fontWeight = FontWeight.Black)
                if (!compact) {
                    team.members.forEach { m ->
                        Text(m.name, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                    }
                }
            }
        }
    }
}
