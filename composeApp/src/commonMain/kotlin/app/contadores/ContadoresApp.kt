package app.contadores

import app.contadores.resources.*
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
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.stringResource

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
    var deleteId by rememberSaveable { mutableStateOf<String?>(null) }
    val notebook = store.state
    val session = notebook.sessions.firstOrNull { it.id == sessionId }
    val first = session?.activeCounters?.firstOrNull()
    val canCount = started && editor == null &&
        valueId == null && deleteId == null
    DisposableEffect(session?.id, first?.id, canCount) {
        bindVolume(if (canCount && session != null && first != null) {
            { delta -> store.adjust(session.id, first.id, delta) }
        } else null)
        onDispose { bindVolume(null) }
    }
    backHandler(started && editor == null) {
        if (sessionId != null) sessionId = null else started = false
    }
    val sessionPrefix = stringResource(Res.string.session_prefix)
    val counterPrefix = stringResource(Res.string.counter_prefix)
    val dark = usesDarkTheme(notebook.appearance)
    val toggleTheme: () -> Unit = {
        store.save(store.state.copy(appearance = if (dark) Appearance.Light else Appearance.Dark))
    }
    NotebookTheme(dark) {
        Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                if (store.readFailed || store.saveFailed) {
                    Text(
                        if (store.readFailed) stringResource(Res.string.read_error)
                        else stringResource(Res.string.retry_save_error),
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
                        create = { store.startSession(sessionPrefix, counterPrefix, newId) },
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
            ValueDialog(
                counter = valueCounter,
                allowNegative = session.allowNegative,
                onClose = { valueId = null },
            ) { value ->
                val changed = session.copy(counters = session.counters.map {
                    if (it.id == valueCounter.id) it.copy(value = value) else it
                })
                store.save(notebook.upsert(changed)).also { if (it) valueId = null }
            }
        }
        val toDelete = notebook.sessions.firstOrNull { it.id == deleteId }
        if (toDelete != null) {
            Confirm(stringResource(Res.string.delete_session), stringResource(Res.string.delete_session_message, toDelete.name), stringResource(Res.string.delete), { deleteId = null }) {
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
                Text(stringResource(Res.string.sessions), style = MaterialTheme.typography.headlineLarge, modifier = Modifier.weight(1f))
                ThemeButton(dark, toggleTheme, enabled = enabled)
            }
        }
        item { Button(onClick = create, enabled = enabled) { Text(stringResource(Res.string.new_session)) } }
        if (sessions.isEmpty() && enabled) item { Text(stringResource(Res.string.empty_sessions)) }
        items(sessions, key = { it.id }) { session ->
            val deleteLabel = stringResource(Res.string.delete_session_a11y, session.name)
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
                    modifier = Modifier.semantics { contentDescription = deleteLabel },
                ) { Text(stringResource(Res.string.delete)) }
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
    hasVolume: Boolean,
    export: () -> Unit,
) {
    val active = session.activeCounters
    var selectedId by rememberSaveable(session.id) { mutableStateOf<String?>(null) }
    val explicitSelection = active.firstOrNull { it.id == selectedId }
    val selected = explicitSelection ?: active.firstOrNull()
    LaunchedEffect(selectedId, explicitSelection?.id) {
        if (selectedId != null && explicitSelection == null) selectedId = null
    }
    val listState = rememberLazyListState()
    val drag = remember(session.id, listState) { CounterDrag(listState) }
    val reorder = if (active.size > 1) Modifier.reorderCounters(drag, move) else Modifier
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = back) { Text(stringResource(Res.string.back_sessions)) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = export, enabled = session.counters.isNotEmpty()) {
                    Text(stringResource(Res.string.export))
                }
                ThemeButton(dark, toggleTheme)
            }
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
                    GlyphButton(Glyph.Pencil, stringResource(Res.string.edit_session), edit)
                }
                if (session.description.isNotBlank()) Text(session.description, modifier = Modifier.padding(top = 5.dp))
                if (session.showTotal) {
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(10.dp)) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(stringResource(Res.string.total), modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                            NumberText(session.total.toString(), numbers, 28)
                        }
                    }
                }
                if (active.isEmpty()) Text(stringResource(Res.string.enable_counter), modifier = Modifier.padding(top = 16.dp))
            }
            items(active, key = { it.id }, contentType = { "counter" }) { counter ->
                val reorderLabel = stringResource(Res.string.reorder_counter, counter.name)
                val isSelected = counter.id == selected?.id
                val dragging = drag.id == counter.id
                val background = if (dragging || drag.target == counter.id || isSelected) MaterialTheme.colorScheme.surfaceVariant
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
                            val moveUp = stringResource(Res.string.move_up)
                            val moveDown = stringResource(Res.string.move_down)
                            val actions = buildList {
                                active.getOrNull(index - 1)?.let { target ->
                                    add(CustomAccessibilityAction(moveUp) { move(counter.id, target.id); true })
                                }
                                active.getOrNull(index + 1)?.let { target ->
                                    add(CustomAccessibilityAction(moveDown) { move(counter.id, target.id); true })
                                }
                            }
                            Box(
                                Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp).semantics {
                                    contentDescription = reorderLabel
                                    customActions = actions
                                },
                                contentAlignment = Alignment.Center,
                            ) { DrawGlyph(Glyph.Handle) }
                        }
                        Column(
                            Modifier.weight(1f)
                                .heightIn(min = 48.dp)
                                .selectable(selected = isSelected, role = Role.RadioButton) { selectedId = counter.id }
                                .padding(horizontal = 4.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                counter.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            if (hasVolume && counter.id == active.first().id) {
                                Text(stringResource(Res.string.volume_buttons), style = MaterialTheme.typography.labelMedium)
                            }
                        }
                        NumberText(counter.value.toString(), numbers, 22)
                        GlyphButton(Glyph.Pencil, stringResource(Res.string.edit_value, counter.name), { editValue(counter.id) })
                    }

                }
            }
        }
        if (selected != null) {
            CountBar(
                counter = selected,
                active = active,
                numbers = numbers,
                select = { selectedId = it },
                adjust = { delta -> adjust(selected.id, delta) },
            )
        }
    }
}

@Composable
private fun CountBar(
    counter: Counter,
    active: List<Counter>,
    numbers: FontFamily,
    select: (String) -> Unit,
    adjust: (Int) -> Unit,
) {
    val caption = stringResource(Res.string.counting, counter.name)
    val decrementLabel = stringResource(Res.string.decrement, counter.name)
    val incrementLabel = stringResource(Res.string.increment, counter.name)
    var showCounters by remember { mutableStateOf(false) }
    HorizontalDivider()
    Column(Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) {
                if (active.size > 1) {
                    TextButton(onClick = { showCounters = true }) {
                        Text("$caption ▾", maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                    DropdownMenu(expanded = showCounters, onDismissRequest = { showCounters = false }) {
                        active.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(option.name, fontWeight = if (option.id == counter.id) FontWeight.Bold else FontWeight.Normal)
                                },
                                onClick = {
                                    select(option.id)
                                    showCounters = false
                                },
                            )
                        }
                    }
                } else {
                    Text(caption, modifier = Modifier.padding(vertical = 10.dp))
                }
            }
            NumberText(counter.value.toString(), numbers, 24)
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = { adjust(-1) },
                modifier = Modifier.weight(1f).heightIn(min = 58.dp)
                    .semantics { contentDescription = decrementLabel },
                shape = RoundedCornerShape(10.dp),
            ) { Text("−1", fontSize = 24.sp) }
            Button(
                onClick = { adjust(1) },
                modifier = Modifier.weight(2f).heightIn(min = 58.dp)
                    .semantics { contentDescription = incrementLabel },
                shape = RoundedCornerShape(10.dp),
            ) { Text("+1", fontSize = 24.sp) }
        }
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
        text = {
            val errorMessage = stringResource(Res.string.save_error)
            Text(if (failed) "$message\n$errorMessage" else message)
        },
        dismissButton = { TextButton(onClick = dismiss) { Text(stringResource(Res.string.cancel)) } },
        confirmButton = { TextButton(onClick = { failed = !commit() }) { Text(action) } },
    )
}
