package bo.app.partygames.data

import kotlin.random.Random

/**
 * Mazo barajado que no repite cartas hasta agotarse; entonces se vuelve a barajar
 * evitando que la primera carta nueva sea igual a la última.
 */
class Deck<T>(private val items: List<T>, private val random: Random = Random.Default) {
    private val pending = ArrayDeque<T>()
    private var last: T? = null

    init {
        require(items.isNotEmpty()) { "El mazo no puede estar vacío" }
    }

    fun draw(): T {
        if (pending.isEmpty()) {
            val shuffled = items.shuffled(random).toMutableList()
            if (shuffled.size > 1 && shuffled.first() == last) {
                shuffled.add(shuffled.removeAt(0))
            }
            pending.addAll(shuffled)
        }
        return pending.removeFirst().also { last = it }
    }
}
