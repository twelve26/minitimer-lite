package app.contadores

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal val notebookJson = Json { ignoreUnknownKeys = true }

@Serializable
internal enum class Appearance {
    System, Light, Dark
}

@Serializable
internal data class Counter(
    val id: String,
    val name: String,
    val value: Int = 0,
    val active: Boolean = true,
    val included: Boolean = true,
)

@Serializable
internal data class Session(
    val id: String,
    val name: String,
    val description: String = "",
    val allowNegative: Boolean = false,
    val showTotal: Boolean = true,
    val counters: List<Counter> = emptyList(),
) {
    val activeCounters: List<Counter> get() = counters.filter(Counter::active)
    val total: Long get() = activeCounters.filter(Counter::included).sumOf { it.value.toLong() }

    fun exportText(): String = buildString {
        append("total - ")
        append(total)
        for (counter in counters) {
            append('\n')
            append(counter.name.lineSequence().joinToString(" "))
            append(" - ")
            append(counter.value)
        }
    }

    fun moveActive(counterId: String, targetId: String): Session {
        val ordered = activeCounters.toMutableList()
        val from = ordered.indexOfFirst { it.id == counterId }
        val to = ordered.indexOfFirst { it.id == targetId }
        if (from < 0 || to < 0 || from == to) return this
        ordered.add(to, ordered.removeAt(from))
        val reordered = ordered.iterator()
        return copy(counters = counters.map { if (it.active) reordered.next() else it })
    }

    fun adjust(counterId: String, delta: Int): Session = copy(counters = counters.map { counter ->
        if (counter.id != counterId || !counter.active) counter
        else {
            val minimum = if (allowNegative) Int.MIN_VALUE else 0
            val value = (counter.value.toLong() + delta).coerceIn(minimum.toLong(), Int.MAX_VALUE.toLong())
            counter.copy(value = value.toInt())
        }
    })
}

@Serializable
internal data class Notebook(
    val appearance: Appearance = Appearance.Light,
    val sessions: List<Session> = emptyList(),
) {
    fun upsert(session: Session): Notebook =
        if (sessions.any { it.id == session.id }) copy(sessions = sessions.map {
            if (it.id == session.id) session else it
        }) else copy(sessions = listOf(session) + sessions)
}

internal class SessionDraft(initial: Session, val isNew: Boolean = false) {
    var session by mutableStateOf(initial)
}

internal class NotebookStore(
    initial: Notebook,
    private val write: (Notebook) -> Unit,
    val readFailed: Boolean = false,
) {
    var editor by mutableStateOf<SessionDraft?>(null)
    var state by mutableStateOf(initial)
        private set
    var saveFailed by mutableStateOf(false)
        private set

    fun startSession(newId: () -> String) {
        val names = state.sessions.mapTo(mutableSetOf(), Session::name)
        val number = generateSequence(1) { it + 1 }.first { "Sesión $it" !in names }
        val session = Session(
            id = newId(),
            name = "Sesión $number",
            counters = listOf(Counter(newId(), "Contador 1")),
        )
        editor = SessionDraft(session, isNew = true)
    }

    fun save(next: Notebook): Boolean {
        if (readFailed) return false
        if (state == next) return true
        return try {
            write(next)
            state = next
            saveFailed = false
            true
        } catch (_: Exception) {
            saveFailed = true
            false
        }
    }

    fun moveActive(sessionId: String, counterId: String, targetId: String) {
        val session = state.sessions.firstOrNull { it.id == sessionId } ?: return
        save(state.upsert(session.moveActive(counterId, targetId)))
    }

    fun adjust(sessionId: String, counterId: String, delta: Int) {
        val session = state.sessions.firstOrNull { it.id == sessionId } ?: return
        save(state.upsert(session.adjust(counterId, delta)))
    }
}
