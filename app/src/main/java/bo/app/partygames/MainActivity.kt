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
import bo.app.partygames.ui.screens.CharadesScreen
import bo.app.partygames.ui.screens.HomeScreen
import bo.app.partygames.ui.screens.ImpostorGameScreen
import bo.app.partygames.ui.screens.MostLikelyScreen
import bo.app.partygames.ui.screens.NeverHaveIEverScreen
import bo.app.partygames.ui.screens.TruthOrDareScreen
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
    const val TRUTH_OR_DARE = "verdad_o_reto"
    const val CHARADES = "charadas"
    const val NEVER = "yo_nunca"
    const val MOST_LIKELY = "mas_probable"
}

@Composable
private fun PartyNavHost() {
    val navController = rememberNavController()
    // Un solo ViewModel a nivel de Activity, compartido por todas las pantallas.
    val partyViewModel: PartyViewModel = viewModel()
    val goHome: () -> Unit = { navController.popBackStack(Routes.HOME, inclusive = false) }

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = partyViewModel,
                onStartGame = { mode ->
                    val route = when (mode) {
                        GameMode.IMPOSTOR -> if (partyViewModel.startImpostorGame()) Routes.IMPOSTOR else null
                        GameMode.TRUTH_OR_DARE -> Routes.TRUTH_OR_DARE
                        GameMode.CHARADES -> Routes.CHARADES
                        GameMode.NEVER_HAVE_I_EVER -> Routes.NEVER
                        GameMode.MOST_LIKELY -> Routes.MOST_LIKELY
                    }
                    if (route != null && partyViewModel.state.value.canStart) {
                        navController.navigate(route) { launchSingleTop = true }
                    }
                },
            )
        }
        composable(Routes.TRUTH_OR_DARE) {
            TruthOrDareScreen(players = partyViewModel.state.value.players, onExit = goHome)
        }
        composable(Routes.CHARADES) {
            val s = partyViewModel.state.value
            CharadesScreen(
                players = s.players,
                roundsPerTeam = s.charadesRounds,
                secondsPerTurn = s.charadesSeconds,
                onExit = goHome,
            )
        }
        composable(Routes.NEVER) {
            NeverHaveIEverScreen(players = partyViewModel.state.value.players, onExit = goHome)
        }
        composable(Routes.MOST_LIKELY) {
            MostLikelyScreen(players = partyViewModel.state.value.players, onExit = goHome)
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
