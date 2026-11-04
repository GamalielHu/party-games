package bo.app.partygames.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Estructura común de las pantallas de juego: barra con título, botón de salir
 * y confirmación al salir (botón o gesto atrás).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScaffold(
    title: String,
    onExit: () -> Unit,
    confirmExit: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    var askExit by rememberSaveable { mutableStateOf(false) }
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
    val requestExit: () -> Unit = {
        if (confirmExit) {
            askExit = true
        } else {
            onExit()
        }
    }
    BackHandler { requestExit() }

    if (askExit) {
        AlertDialog(
            onDismissRequest = { askExit = false },
            title = { Text("¿Salir del juego?") },
            text = { Text("Se perderán los puntos de esta partida. Los jugadores se conservan.") },
            confirmButton = { TextButton(onClick = { askExit = false; onExit() }) { Text("Salir") } },
            dismissButton = { TextButton(onClick = { askExit = false }) { Text("Seguir jugando") } },
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = { requestExit() }) {
                        Icon(Icons.Filled.Close, contentDescription = "Salir")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

/** Fila de marcador: nombre y puntos, resaltada si [highlight]. */
@Composable
fun ScoreRow(
    name: String,
    score: Int,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
    accent: Color = MaterialTheme.colorScheme.secondary,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (highlight) accent.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceContainer,
                RoundedCornerShape(12.dp),
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            "$score",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (highlight) accent else MaterialTheme.colorScheme.onSurface,
        )
    }
}
