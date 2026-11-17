package app.contadores

import kotlin.test.Test
import kotlin.test.assertEquals

class SessionTest {
    @Test
    fun exportStartsWithCalculatedTotalAndIncludesAllCountersInOrder() {
        val counters = listOf(
            Counter("a", "Asistentes", value = 27),
            Counter("b", "Equipo", value = -2, included = false),
            Counter("c", "Visitantes", active = false),
        )
        val session = Session("service", "Servicio", allowNegative = true, counters = counters)

        assertEquals("total - 27\nAsistentes - 27\nEquipo - -2\nVisitantes - 0", session.exportText())
    }

    @Test
    fun newSessionCanBeSavedWithItsDefaults() {
        val existing = Session("existing", "Sesión 1")
        val store = NotebookStore(Notebook(sessions = listOf(existing)), write = {})
        val ids = listOf("new", "first").iterator()
        store.startSession { ids.next() }

        val draft = requireNotNull(store.editor)
        assertEquals(true, draft.isNew)
        assertEquals("Sesión 2", draft.session.name)
        assertEquals("", draft.session.description)
        assertEquals(Counter("first", "Contador 1"), draft.session.activeCounters.single())
        assertEquals(true, store.save(store.state.upsert(draft.session)))
        assertEquals(0L, store.state.sessions.first().total)
    }

    @Test
    fun movingActiveCountersUpdatesFirstAndPreservesInactiveSlots() {
        val a = Counter("a", "Asistentes", value = 27)
        val b = Counter("b", "Equipo", value = 10)
        val inactive = Counter("c", "Otro", active = false)
        val session = Session("service", "Servicio", counters = listOf(a, inactive, b))
        val moved = session.moveActive(b.id, a.id)

        assertEquals(listOf(b, inactive, a), moved.counters)
        assertEquals(b, moved.activeCounters.first())
        assertEquals(37L, moved.total)
        assertEquals(session, moved.moveActive(b.id, a.id))
        assertEquals(moved, moved.moveActive(inactive.id, a.id))
    }

    @Test
    fun existingDataOpensAfterRemovingMetadata() {
        val notebook = notebookJson.decodeFromString<Notebook>(
            """
            {
              "sessions": [{
                "id": "service",
                "name": "Servicio",
                "description": "Descripción de la sesión",
                "tag": "Etiqueta anterior",
                "counters": [{
                  "id": "a",
                  "name": "Asistentes",
                  "description": "Descripción anterior",
                  "tag": "Etiqueta anterior",
                  "value": 27
                }]
              }]
            }
            """.trimIndent(),
        )
        val session = notebook.sessions.single()
        assertEquals("Descripción de la sesión", session.description)
        assertEquals(Counter("a", "Asistentes", value = 27), session.counters.single())
        assertEquals(27L, session.total)
    }

    @Test
    fun totalOnlyUsesActiveIncludedCounters() {
        val counters = listOf(
            Counter("a", "Asistentes", value = 80),
            Counter("b", "Equipo", value = 10),
            Counter("c", "Visitantes", value = 5),
            Counter("d", "Otro", value = 7),
        )
        val session = Session("service", "Servicio", counters = counters)
        assertEquals(102L, session.total)

        val fewer = session.copy(counters = counters.mapIndexed { index, counter ->
            counter.copy(active = index == 0)
        })
        assertEquals(80L, fewer.total)
        assertEquals(listOf(80, 10, 5, 7), fewer.counters.map(Counter::value))
        assertEquals(102L, fewer.copy(counters = fewer.counters.map { it.copy(active = true) }).total)
        assertEquals(95L, session.copy(counters = counters.dropLast(1) + counters.last().copy(included = false)).total)
    }

    @Test
    fun adjustmentRespectsSessionNegativePolicy() {
        val counter = Counter("a", "Asistentes")
        val session = Session("service", "Servicio", counters = listOf(counter))
        assertEquals(0, session.adjust(counter.id, -1).counters.single().value)
        assertEquals(-1, session.copy(allowNegative = true).adjust(counter.id, -1).counters.single().value)
    }
}
