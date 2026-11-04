package bo.app.partygames.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bo.app.partygames.data.GameMode
import bo.app.partygames.data.Player
import bo.app.partygames.ui.viewmodel.PartyUiState
import bo.app.partygames.ui.viewmodel.PartyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PartyViewModel,
    onStartGame: (GameMode) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Party Games Local", fontWeight = FontWeight.Black) },
            )
        },
        bottomBar = {
            StartBar(state = state, onStart = { onStartGame(state.selectedMode) })
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "players_header") {
                SectionTitle("Jugadores", "${state.players.size} / ${PartyUiState.MAX_PLAYERS}")
            }
            item(key = "name_input") {
                NameInput(
                    value = state.nameInput,
                    error = state.nameError,
                    onValueChange = viewModel::onNameInputChange,
                    onAdd = viewModel::addPlayer,
                )
            }
            if (state.players.isEmpty()) {
                item(key = "empty") {
                    Text(
                        "Agrega al menos ${state.selectedMode.minPlayers} jugadores para empezar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                }
            }
            itemsIndexed(state.players, key = { _, p -> p.id }) { index, player ->
                PlayerRow(
                    index = index,
                    player = player,
                    onRemove = { viewModel.removePlayer(player.id) },
                    modifier = Modifier.animateItem(),
                )
            }
            item(key = "mode_header") {
                Spacer(Modifier.height(8.dp))
                SectionTitle("Modo de juego")
            }
            items(GameMode.entries.size, key = { "mode_${GameMode.entries[it].name}" }) { i ->
                val mode = GameMode.entries[i]
                ModeCard(
                    mode = mode,
                    selected = mode == state.selectedMode,
                    onSelect = { viewModel.selectMode(mode) },
                )
            }
            if (state.selectedMode == GameMode.IMPOSTOR) {
                item(key = "impostor_settings") {
                    ImpostorSettings(
                        state = state,
                        onImpostorCount = viewModel::setImpostorCount,
                        onHint = viewModel::setImpostorKnowsCategory,
                        onSeconds = viewModel::setDiscussionSeconds,
                    )
                }
            }
            if (state.selectedMode == GameMode.CHARADES) {
                item(key = "charades_settings") {
                    CharadesSettings(
                        state = state,
                        onRounds = viewModel::setCharadesRounds,
                        onSeconds = viewModel::setCharadesSeconds,
                    )
                }
            }
            item(key = "bottom_space") { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun SectionTitle(title: String, trailing: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) {
            Text(trailing, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun NameInput(
    value: String,
    error: String?,
    onValueChange: (String) -> Unit,
    onAdd: () -> Unit,
) {
    Row(verticalAlignment = Alignment.Top) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            label = { Text("Nombre del jugador") },
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done,
            ),
            // Done agrega y mantiene el teclado abierto para escribir el siguiente nombre.
            keyboardActions = KeyboardActions(onDone = { onAdd() }),
        )
        Spacer(Modifier.width(8.dp))
        FilledIconButton(
            onClick = onAdd,
            modifier = Modifier
                .padding(top = 8.dp)
                .size(48.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Añadir jugador")
        }
    }
}

@Composable
private fun PlayerRow(
    index: Int,
    player: Player,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(32.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "${index + 1}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(
                player.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Close, contentDescription = "Eliminar a ${player.name}")
            }
        }
    }
}

@Composable
private fun ModeCard(mode: GameMode, selected: Boolean, onSelect: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onSelect, role = Role.RadioButton),
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected, onClick = null)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(mode.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    mode.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "Mínimo ${mode.minPlayers} jugadores",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ImpostorSettings(
    state: PartyUiState,
    onImpostorCount: (Int) -> Unit,
    onHint: (Boolean) -> Unit,
    onSeconds: (Int) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Impostores", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (n in 1..state.maxImpostors) {
                    FilterChip(
                        selected = state.impostorCount == n,
                        onClick = { onImpostorCount(n) },
                        label = { Text("$n") },
                    )
                }
            }

            Text("Tiempo de debate", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PartyUiState.DISCUSSION_OPTIONS.forEach { secs ->
                    FilterChip(
                        selected = state.discussionSeconds == secs,
                        onClick = { onSeconds(secs) },
                        label = { Text(if (secs == 0) "Libre" else "${secs / 60} min") },
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Pista para el impostor", style = MaterialTheme.typography.labelLarge)
                    Text(
                        "El impostor ve la categoría de la palabra",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = state.impostorKnowsCategory, onCheckedChange = onHint)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CharadesSettings(
    state: PartyUiState,
    onRounds: (Int) -> Unit,
    onSeconds: (Int) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Rondas por equipo", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PartyUiState.CHARADES_ROUND_OPTIONS.forEach { n ->
                    FilterChip(
                        selected = state.charadesRounds == n,
                        onClick = { onRounds(n) },
                        label = { Text("$n") },
                    )
                }
            }
            Text("Tiempo por turno", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PartyUiState.CHARADES_SECONDS_OPTIONS.forEach { secs ->
                    FilterChip(
                        selected = state.charadesSeconds == secs,
                        onClick = { onSeconds(secs) },
                        label = { Text("$secs s") },
                    )
                }
            }
            Text(
                "Los equipos se arman al azar; puedes rehacerlos antes del primer turno.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StartBar(state: PartyUiState, onStart: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainer, tonalElevation = 3.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (!state.canStart) {
                Text(
                    "Faltan ${state.playersMissing} jugador(es)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            Button(
                onClick = onStart,
                enabled = state.canStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Jugar ${state.selectedMode.title}", fontWeight = FontWeight.Bold)
            }
        }
    }
}
