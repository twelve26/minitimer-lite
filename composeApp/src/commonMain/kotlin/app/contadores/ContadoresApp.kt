package app.contadores

import app.contadores.resources.Res
import app.contadores.resources.ibm_plex_mono
import app.contadores.resources.instrument_serif
import app.contadores.resources.inter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import org.jetbrains.compose.resources.Font

private val lightColors = lightColorScheme(
    background = Color(0xFFF8F3E8), onBackground = Color(0xFF252823),
    surface = Color(0xFFF8F3E8), onSurface = Color(0xFF252823),
    primary = Color(0xFF285647), onPrimary = Color.White,
    surfaceVariant = Color(0xFFEDE4D4), onSurfaceVariant = Color(0xFF534B3F),
    surfaceContainer = Color(0xFFF1E9DB), surfaceContainerHigh = Color(0xFFEDE4D4),
    outline = Color(0xFF807567), outlineVariant = Color(0xFFD6CBBC),
)
private val darkColors = darkColorScheme(
    background = Color(0xFF121823), onBackground = Color(0xFFEDF3FF),
    surface = Color(0xFF121823), onSurface = Color(0xFFEDF3FF),
    primary = Color(0xFFB6EF73), onPrimary = Color(0xFF172610),
    surfaceVariant = Color(0xFF202B3D), onSurfaceVariant = Color(0xFFB9C8DF),
    surfaceContainer = Color(0xFF1B2534), surfaceContainerHigh = Color(0xFF202B3D),
    outline = Color(0xFF889BB8), outlineVariant = Color(0xFF3C4B62),
)

@Composable
internal fun ContadoresApp(
    store: NotebookStore,
    newId: () -> String,
    copyText: (String) -> Unit,
    bindVolume: (((Int) -> Unit)?) -> Unit = {},
    hasVolumeControls: Boolean = false,
    backHandler: @Composable (Boolean, () -> Unit) -> Unit = { _, _ -> },
) {
    var started by rememberSaveable { mutableStateOf(false) }
    var sessionId by rememberSaveable { mutableStateOf<String?>(null) }
    val editor = store.editor
    var valueId by rememberSaveable { mutableStateOf<String?>(null) }
    var resetId by rememberSaveable { mutableStateOf<String?>(null) }
    var deleteId by rememberSaveable { mutableStateOf<String?>(null) }
    val notebook = store.state
    val session = notebook.sessions.firstOrNull { it.id == sessionId }
    val first = session?.activeCounters?.firstOrNull()
    val canCount = started && editor == null &&
        valueId == null && resetId == null && deleteId == null
    DisposableEffect(session?.id, first?.id, canCount) {
        bindVolume(if (canCount && session != null && first != null) {
            { delta -> store.adjust(session.id, first.id, delta) }
        } else null)
        onDispose { bindVolume(null) }
    }
    backHandler(started && editor == null) {
        if (sessionId != null) sessionId = null else started = false
    }
    val dark = usesDarkTheme(notebook.appearance)
    val toggleTheme: () -> Unit = {
        store.save(store.state.copy(appearance = if (dark) Appearance.Light else Appearance.Dark))
    }
    NotebookTheme(dark) {
        Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                if (store.readFailed || store.saveFailed) {
                    Text(
                        if (store.readFailed) "No se pudieron abrir los datos guardados."
                        else "No se pudo guardar el cambio. Vuelve a intentarlo.",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp),
                    )
                }
                if (!started) {
                    WelcomeScreen(
                        dark = dark,
                        toggleTheme = toggleTheme,
                        themeEnabled = !store.readFailed,
                        start = { started = true },
                    )
                } else if (session == null) {
                    HomeScreen(
                        sessions = notebook.sessions,
                        create = { store.startSession(newId) },
                        open = { sessionId = it },
                        delete = { deleteId = it },
                        dark = dark,
                        toggleTheme = toggleTheme,
                        enabled = !store.readFailed,
                    )
                } else {
                    CountScreen(
                        session = session,
                        numbers = if (dark) FontFamily(Font(Res.font.ibm_plex_mono, FontWeight.Bold)) else MaterialTheme.typography.bodyLarge.fontFamily ?: FontFamily.SansSerif,
                        back = { sessionId = null },
                        edit = { store.editor = SessionDraft(session) },
                        dark = dark,
                        toggleTheme = toggleTheme,
                        adjust = { id, delta -> store.adjust(session.id, id, delta) },
                        move = { id, target -> store.moveActive(session.id, id, target) },
                        editValue = { valueId = it },
                        reset = { resetId = it },
                        hasVolume = hasVolumeControls,
                        export = { copyText(session.exportText()) },
                    )
                }
            }
        }
        if (editor != null) {
            SessionEditor(editor, newId, onClose = { store.editor = null }) { updated ->
                store.save(notebook.upsert(updated)).also { saved ->
                    if (saved) {
                        sessionId = updated.id
                        store.editor = null
                    }
                }
            }
        }
        val valueCounter = session?.counters?.firstOrNull { it.id == valueId }
        if (session != null && valueCounter != null) {
            ValueDialog(valueCounter, session.allowNegative, onClose = { valueId = null }) { value ->
                val changed = session.copy(counters = session.counters.map {
                    if (it.id == valueCounter.id) it.copy(value = value) else it
                })
                store.save(notebook.upsert(changed)).also { if (it) valueId = null }
            }
        }
        val resetCounter = session?.counters?.firstOrNull { it.id == resetId }
        if (session != null && resetCounter != null) {
            Confirm("Restablecer contador", "«${resetCounter.name}» volverá a cero.", "Restablecer", { resetId = null }) {
                val changed = session.copy(counters = session.counters.map {
                    if (it.id == resetCounter.id) it.copy(value = 0) else it
                })
                store.save(notebook.upsert(changed)).also { if (it) resetId = null }
            }
        }
        val toDelete = notebook.sessions.firstOrNull { it.id == deleteId }
        if (toDelete != null) {
            Confirm("Borrar sesión", "Se borrará «${toDelete.name}» con todos sus contadores.", "Borrar", { deleteId = null }) {
                store.save(notebook.copy(sessions = notebook.sessions.filterNot { it.id == toDelete.id }))
                    .also { if (it) deleteId = null }
            }
        }
    }
}

internal fun usesDarkTheme(appearance: Appearance): Boolean = appearance == Appearance.Dark

@Composable
private fun NotebookTheme(dark: Boolean, content: @Composable () -> Unit) {
    val headings = FontFamily(Font(Res.font.instrument_serif))
    val body = FontFamily(Font(Res.font.inter))
    val defaults = Typography()
    val title = TextStyle(fontFamily = headings, fontSize = 38.sp, lineHeight = 42.sp)
    MaterialTheme(
        colorScheme = if (dark) darkColors else lightColors,
        typography = Typography(
            headlineLarge = title,
            headlineSmall = defaults.headlineSmall.copy(fontFamily = headings),
            titleLarge = defaults.titleLarge.copy(fontFamily = body),
            titleMedium = defaults.titleMedium.copy(fontFamily = body),
            bodyLarge = defaults.bodyLarge.copy(fontFamily = body),
            bodyMedium = defaults.bodyMedium.copy(fontFamily = body),
            bodySmall = defaults.bodySmall.copy(fontFamily = body),
            labelLarge = defaults.labelLarge.copy(fontFamily = body),
            labelMedium = defaults.labelMedium.copy(fontFamily = body),
        ),
        content = content,
    )
}

@Composable
private fun HomeScreen(
    sessions: List<Session>,
    create: () -> Unit,
    open: (String) -> Unit,
    delete: (String) -> Unit,
    dark: Boolean,
    toggleTheme: () -> Unit,
    enabled: Boolean,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Sesiones", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.weight(1f))
                ThemeButton(dark, toggleTheme, enabled = enabled)
            }
        }
        item { Button(onClick = create, enabled = enabled) { Text("Nueva sesión") } }
        if (sessions.isEmpty() && enabled) item { Text("Aún no tienes sesiones.") }
        items(sessions, key = { it.id }) { session ->
            HorizontalDivider()
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(onClick = { open(session.id) }, modifier = Modifier.weight(1f)) {
                    Column(Modifier.padding(vertical = 14.dp)) {
                        Text(session.name, style = MaterialTheme.typography.titleLarge)
                        if (session.description.isNotBlank()) Text(session.description, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                TextButton(
                    onClick = { delete(session.id) },
                    modifier = Modifier.semantics { contentDescription = "Borrar sesión ${session.name}" },
                ) { Text("Borrar") }
            }
        }
    }
}

@Composable
private fun CountScreen(
    session: Session,
    numbers: FontFamily,
    back: () -> Unit,
    edit: () -> Unit,
    dark: Boolean,
    toggleTheme: () -> Unit,
    adjust: (String, Int) -> Unit,
    move: (String, String) -> Unit,
    editValue: (String) -> Unit,
    reset: (String) -> Unit,
    hasVolume: Boolean,
    export: () -> Unit,
) {
    val active = session.activeCounters
    val listState = rememberLazyListState()
    val drag = remember(session.id, listState) { CounterDrag(listState) }
    val reorder = if (active.size > 1) Modifier.reorderCounters(drag, move) else Modifier
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = back) { Text("‹ Sesiones") }
            ThemeButton(dark, toggleTheme)
        }
        LazyColumn(
            modifier = Modifier.weight(1f).then(reorder),
            state = listState,
            contentPadding = PaddingValues(bottom = 20.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        session.name,
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    GlyphButton(Glyph.Pencil, "Editar sesión", edit)
                }
                if (session.description.isNotBlank()) Text(session.description, modifier = Modifier.padding(top = 5.dp))
                if (session.showTotal) {
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(10.dp)) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Total", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                            NumberText(session.total.toString(), numbers, 28)
                        }
                    }
                }
                if (active.isEmpty()) Text("Activa un contador en Editar sesión.", modifier = Modifier.padding(top = 16.dp))
            }
            items(active, key = { it.id }, contentType = { "counter" }) { counter ->
                val dragging = drag.id == counter.id
                val background = if (dragging || drag.target == counter.id) MaterialTheme.colorScheme.surfaceVariant
                    else MaterialTheme.colorScheme.background
                Column(
                    Modifier.fillMaxWidth()
                        .zIndex(if (dragging) 1f else 0f)
                        .graphicsLayer { translationY = drag.offsetFor(counter.id) }
                        .background(background)
                        .padding(top = 12.dp),
                ) {
                    HorizontalDivider()
                    Row(Modifier.fillMaxWidth().padding(top = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (active.size > 1) {
                            val index = active.indexOf(counter)
                            val actions = buildList {
                                active.getOrNull(index - 1)?.let { target ->
                                    add(CustomAccessibilityAction("Subir") { move(counter.id, target.id); true })
                                }
                                active.getOrNull(index + 1)?.let { target ->
                                    add(CustomAccessibilityAction("Bajar") { move(counter.id, target.id); true })
                                }
                            }
                            Box(
                                Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp).semantics {
                                    contentDescription = "Reordenar ${counter.name}"
                                    customActions = actions
                                },
                                contentAlignment = Alignment.Center,
                            ) { DrawGlyph(Glyph.Handle) }
                        }
                        Text(
                            counter.name,
                            modifier = Modifier.weight(1f, fill = false),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        GlyphButton(Glyph.Pencil, "Editar valor de ${counter.name}", { editValue(counter.id) })
                        var menu by remember { mutableStateOf(false) }
                        Box {
                            TextButton(
                                onClick = { menu = true },
                                modifier = Modifier.semantics { contentDescription = "Opciones de ${counter.name}" },
                            ) { Text("⋯", fontSize = 23.sp) }
                            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                                DropdownMenuItem(text = { Text("Restablecer") }, onClick = {
                                    menu = false
                                    reset(counter.id)
                                })
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = { adjust(counter.id, -1) },
                            modifier = Modifier.sizeIn(minWidth = 54.dp, minHeight = 52.dp)
                                .semantics { contentDescription = "Restar uno a ${counter.name}" },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(0.dp),
                        ) { Text("−", fontSize = 28.sp) }
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            NumberText(counter.value.toString(), numbers, 38)
                        }
                        Button(
                            onClick = { adjust(counter.id, 1) },
                            modifier = Modifier.sizeIn(minWidth = 76.dp, minHeight = 52.dp)
                                .semantics { contentDescription = "Sumar uno a ${counter.name}" },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(0.dp),
                        ) { Text("+", fontSize = 28.sp) }
                    }
                    if (hasVolume && counter.id == active.first().id) {
                        Text("Botones de volumen", modifier = Modifier.padding(top = 5.dp), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
        HorizontalDivider()
        Button(
            onClick = export,
            enabled = session.counters.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp).heightIn(min = 48.dp),
            shape = RoundedCornerShape(10.dp),
        ) { Text("Exportar") }
    }
}

@Composable
private fun NumberText(value: String, family: FontFamily, size: Int) {
    val fittedSize = if (value.length > 8) size * 8f / value.length else size.toFloat()
    Text(
        value,
        maxLines = 1,
        style = TextStyle(
            fontFamily = family,
            fontSize = fittedSize.sp,
            lineHeight = (fittedSize + 4).sp,
            fontWeight = FontWeight.Bold,
            fontFeatureSettings = "tnum",
        ),
    )
}

@Composable
internal fun Confirm(
    title: String,
    message: String,
    action: String,
    dismiss: () -> Unit,
    commit: () -> Boolean,
) {
    var failed by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text(title) },
        text = { Text(if (failed) "$message\nNo se pudo guardar el cambio." else message) },
        dismissButton = { TextButton(onClick = dismiss) { Text("Cancelar") } },
        confirmButton = { TextButton(onClick = { failed = !commit() }) { Text(action) } },
    )
}
