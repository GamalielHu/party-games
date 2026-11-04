package bo.app.partygames.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import bo.app.partygames.data.Player
import bo.app.partygames.ui.components.GameScaffold
import bo.app.partygames.ui.viewmodel.MostLikelyViewModel
import bo.app.partygames.ui.viewmodel.NeverHaveIEverViewModel

// ------------------------------------------------------------------ Yo nunca nunca

@Composable
fun NeverHaveIEverScreen(
    players: List<Player>,
    onExit: () -> Unit,
    viewModel: NeverHaveIEverViewModel = viewModel(),
) {
    LaunchedEffect(Unit) { viewModel.start(players) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    if (state.players.isEmpty()) return

    GameScaffold(title = "Yo nunca nunca · #${state.number}", onExit = onExit) {
        PromptCard(state.prompt)
        Text(
            "¿Quién sí lo hizo? Marca a los que confiesan.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
        ) {
            items(state.players, key = { it.id }) { p ->
                val checked = p.id in state.confessed
                PlayerChoiceRow(
                    name = p.name,
                    trailing = "${state.totals[p.id] ?: 0}",
                    selected = checked,
                    modifier = Modifier.toggleable(
                        value = checked,
                        role = Role.Checkbox,
                        onValueChange = { viewModel.toggleConfessed(p.id) },
                    ),
                ) { Checkbox(checked = checked, onCheckedChange = null) }
            }
        }
        val leader = state.players.maxByOrNull { state.totals[it.id] ?: 0 }
        val leaderScore = leader?.let { state.totals[it.id] } ?: 0
        if (leader != null && leaderScore > 0) {
            Text(
                "Más historias: ${leader.name} ($leaderScore)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        Button(onClick = viewModel::next, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Siguiente frase", fontWeight = FontWeight.Bold)
        }
    }
}

// ------------------------------------------------------------------ ¿Quién es más probable?

@Composable
fun MostLikelyScreen(
    players: List<Player>,
    onExit: () -> Unit,
    viewModel: MostLikelyViewModel = viewModel(),
) {
    LaunchedEffect(Unit) { viewModel.start(players) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    if (state.players.isEmpty()) return

    GameScaffold(title = "¿Quién es más probable? · #${state.number}", onExit = onExit) {
        PromptCard(state.prompt)
        Text(
            "A la cuenta de tres, todos señalan. Elige al más señalado.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
        ) {
            items(state.players, key = { it.id }) { p ->
                val selected = p.id == state.selectedId
                PlayerChoiceRow(
                    name = p.name,
                    trailing = "${state.scores[p.id] ?: 0}",
                    selected = selected,
                    modifier = Modifier.selectable(
                        selected = selected,
                        role = Role.RadioButton,
                        onClick = { viewModel.select(p.id) },
                    ),
                ) { RadioButton(selected = selected, onClick = null) }
            }
        }
        val selectedName = state.players.firstOrNull { it.id == state.selectedId }?.name
        Button(onClick = viewModel::next, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text(
                if (selectedName != null) "Punto para $selectedName" else "Saltar pregunta",
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ------------------------------------------------------------------ Comunes

@Composable
private fun ColumnScope.PromptCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun PlayerChoiceRow(
    name: String,
    trailing: String,
    selected: Boolean,
    modifier: Modifier,
    control: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().then(modifier),
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.secondary) else null,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            control()
            Spacer(Modifier.width(8.dp))
            Text(
                name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                trailing,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(end = 8.dp),
            )
        }
    }
}
