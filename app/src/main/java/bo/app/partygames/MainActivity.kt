package bo.app.partygames

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import bo.app.partygames.data.GameMode
import bo.app.partygames.ui.screens.HomeScreen
import bo.app.partygames.ui.screens.ImpostorGameScreen
import bo.app.partygames.ui.theme.PartyGamesTheme
import bo.app.partygames.ui.viewmodel.PartyViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PartyGamesTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PartyNavHost()
                }
            }
        }
    }
}

private object Routes {
    const val HOME = "home"
    const val IMPOSTOR = "impostor"
}

@Composable
private fun PartyNavHost() {
    val navController = rememberNavController()
    // Un solo ViewModel a nivel de Activity, compartido por todas las pantallas.
    val partyViewModel: PartyViewModel = viewModel()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = partyViewModel,
                onStartGame = { mode ->
                    when (mode) {
                        GameMode.IMPOSTOR -> if (partyViewModel.startImpostorGame()) {
                            navController.navigate(Routes.IMPOSTOR) { launchSingleTop = true }
                        }
                    }
                },
            )
        }
        composable(Routes.IMPOSTOR) {
            ImpostorGameScreen(
                viewModel = partyViewModel,
                onExit = {
                    partyViewModel.exitGame()
                    navController.popBackStack(Routes.HOME, inclusive = false)
                },
            )
        }
    }
}
