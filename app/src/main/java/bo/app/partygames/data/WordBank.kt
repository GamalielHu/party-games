package bo.app.partygames.data

import kotlin.random.Random

/** Banco de palabras local (sin internet). */
object WordBank {

    val categories: Map<String, List<String>> = mapOf(
        "Comida" to listOf(
            "Salteña", "Pizza", "Sushi", "Hamburguesa", "Helado", "Empanada", "Silpancho",
            "Pique macho", "Tacos", "Chocolate", "Sopa de maní", "Anticucho", "Palomitas",
        ),
        "Lugares" to listOf(
            "Playa", "Hospital", "Aeropuerto", "Cine", "Escuela", "Mercado", "Estadio",
            "Biblioteca", "Gimnasio", "Iglesia", "Salar", "Teleférico", "Discoteca", "Zoológico",
        ),
        "Animales" to listOf(
            "Llama", "Cóndor", "Perro", "Gato", "Elefante", "Delfín", "Jaguar", "Pingüino",
            "Tiburón", "Mono", "Caballo", "Búho", "Tortuga", "Serpiente",
        ),
        "Profesiones" to listOf(
            "Doctor", "Bombero", "Policía", "Profesor", "Cocinero", "Piloto", "Ingeniero",
            "Albañil", "Abogado", "Astronauta", "Payaso", "Topógrafo", "Peluquero",
        ),
        "Objetos" to listOf(
            "Celular", "Paraguas", "Guitarra", "Reloj", "Espejo", "Mochila", "Llave",
            "Linterna", "Tijeras", "Almohada", "Martillo", "Cámara", "Bicicleta",
        ),
        "Deportes" to listOf(
            "Fútbol", "Básquet", "Natación", "Tenis", "Voleibol", "Boxeo", "Ciclismo",
            "Ajedrez", "Raquetbol", "Esquí", "Karate", "Atletismo",
        ),
        "Fiesta" to listOf(
            "Carnaval", "Cumpleaños", "Boda", "Año Nuevo", "Navidad", "Graduación",
            "Halloween", "Entrada folklórica", "Karaoke", "Piñata",
        ),
    )

    fun randomCard(random: Random = Random.Default, avoid: SecretCard? = null): SecretCard {
        val all = categories.flatMap { (cat, words) -> words.map { SecretCard(cat, it) } }
        val pool = if (avoid != null && all.size > 1) all.filter { it != avoid } else all
        return pool.random(random)
    }
}
