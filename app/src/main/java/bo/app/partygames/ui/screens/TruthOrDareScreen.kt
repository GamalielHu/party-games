package bo.app.partygames.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import bo.app.partygames.data.Player
import bo.app.partygames.ui.components.GameScaffold
import bo.app.partygames.ui.components.ScoreRow
import bo.app.partygames.ui.theme.CrewGreen
import bo.app.partygames.ui.theme.ImpostorRed
import bo.app.partygames.ui.viewmodel.TruthOrDareKind
import bo.app.partygames.ui.viewmodel.TruthOrDarePhase
import bo.app.partygames.ui.viewmodel.TruthOrDareViewModel

@Composable
fun TruthOrDareScreen(
    players: List<Player>,
    onExit: () -> Unit,
    viewModel: TruthOrDareViewModel = viewModel(),
) {
    LaunchedEffect(Unit) { viewModel.start(players) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val player = state.currentPlayer

    GameScaffold(title = "Verdad o Reto · Ronda ${state.round}", onExit = onExit) {
        if (player == null) return@GameScaffold

        Text("Turno de", style = MaterialTheme.typography.titleMedium)
        Text(
            player.name,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
        )

        when (val phase = state.phase) {
            TruthOrDarePhase.Choose -> {
                Text(
                    "¿Verdad (1 punto) o reto (2 puntos)?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    BigChoice("VERDAD", MaterialTheme.colorScheme.primary, Modifier.weight(1f)) {
                        viewModel.choose(TruthOrDareKind.TRUTH)
                    }
                    BigChoice("RETO", ImpostorRed, Modifier.weight(1f)) {
                        viewModel.choose(TruthOrDareKind.DARE)
                    }
                }
            }
            is TruthOrDarePhase.Card -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                ) {
                    Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            phase.kind.label.uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (phase.kind == TruthOrDareKind.DARE) ImpostorRed else MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            phase.text,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    }
                }
                TextButton(onClick = viewModel::reroll) { Text("Otra ${phase.kind.label.lowercase()}") }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { viewModel.resolve(completed = false) },
                        modifier = Modifier.weight(1f).height(52.dp),
                    ) { Text("Se rindió") }
                    Button(
                        onClick = { viewModel.resolve(completed = true) },
                        colors = ButtonDefaults.buttonColors(containerColor = CrewGreen, contentColor = Color.Black),
                        modifier = Modifier.weight(1f).height(52.dp),
                    ) { Text("¡Cumplió!", fontWeight = FontWeight.Bold) }
                }
            }
        }

        Text(
            "Marcador",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
        val best = state.scores.values.maxOrNull() ?: 0
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(state.players.sortedByDescending { state.scores[it.id] ?: 0 }, key = { it.id }) { p ->
                val score = state.scores[p.id] ?: 0
                ScoreRow(
                    name = p.name,
                    score = score,
                    highlight = best > 0 && score == best,
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

@Composable
private fun BigChoice(label: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.White),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        }
    }
}
