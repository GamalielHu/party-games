# Party Games Local

Juegos de fiesta para un solo teléfono que se pasa de mano en mano. Sin internet.

## Modo El Impostor
1. **Inicio:** escribe los nombres (mínimo 3), elige el modo y ajusta impostores, tiempo de debate y pista.
2. **Reparto:** "Pasa el teléfono a [Nombre]". El rol solo se ve mientras se **mantiene presionada** la tarjeta; al soltar se oculta.
3. **Debate y votación:** temporizador, el grupo elige a quién expulsar y se revela si era impostor. Ganan los inocentes al expulsar a todos los impostores; gana el impostor si iguala en número a los inocentes.

## Otros modos
- **Verdad o Reto** (2+): por turnos; verdad vale 1 punto y reto 2. Se puede pedir otra carta.
- **Charadas por equipos** (4+): dos equipos al azar, rondas y tiempo configurables, botones Adivinaron / Pasar.
- **Yo nunca nunca** (2+): se marcan los que confiesan; cuenta quién tiene más historias.
- **¿Quién es más probable?** (3+): todos señalan y el más señalado se lleva el punto.

## Código
- `ui/screens/HomeScreen.kt`, `ui/screens/ImpostorGameScreen.kt`
- `ui/viewmodel/PartyViewModel.kt` (MVVM, `StateFlow<PartyUiState>` inmutable; jugadores y ajustes)
- Un ViewModel por juego nuevo (`TruthOrDareViewModel`, `CharadesViewModel`, `PromptRoundsViewModels`) y sus pantallas en `ui/screens/`
- `ui/components/GameScaffold.kt` estructura común (salir con confirmación, pantalla encendida)
- `data/` modelos y banco de palabras

## Compilación
Cada push a `main` compila en GitHub Actions y publica **PartyGames.apk** en Releases (tag `v1.<n>`). Si falla, los errores quedan en la rama `registro`.
