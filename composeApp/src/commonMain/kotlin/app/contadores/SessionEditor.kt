package app.contadores

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
internal fun SessionEditor(
    editor: SessionDraft,
    newId: () -> String,
    onClose: () -> Unit,
    commit: (Session) -> Boolean,
) {
    val draft = editor.session
    var removeId by rememberSaveable { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    fun update(change: Session.() -> Session) {
        editor.session = editor.session.change()
    }

    fun editCounter(counter: Counter) = update {
        copy(counters = counters.map { if (it.id == counter.id) counter else it })
    }

    fun move(index: Int, offset: Int) = update {
        copy(counters = counters.toMutableList().apply {
            add(index + offset, removeAt(index))
        })
    }

    fun save() {
        val draft = editor.session
        error = when {
            draft.name.isBlank() -> "Escribe el nombre de la sesión."
            draft.counters.isEmpty() -> "Agrega al menos un contador."
            draft.counters.any { it.name.isBlank() } -> "Escribe un nombre para cada contador."
            !draft.allowNegative && draft.counters.any { it.value < 0 } ->
                "Corrige los valores negativos antes de desactivar esta opción."
            else -> null
        }
        if (error != null) return
        val clean = draft.copy(
            name = draft.name.trim(),
            description = draft.description.trim(),
            counters = draft.counters.map {
                it.copy(name = it.name.trim())
            },
        )
        if (!commit(clean)) error = "No se pudo guardar la sesión."
    }

    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            Modifier.fillMaxWidth(0.94f).fillMaxHeight(0.92f).imePadding(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text(
                    if (editor.isNew) "Nueva sesión" else "Editar sesión",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(vertical = 14.dp),
                )
                LazyColumn(
                    Modifier.weight(1f),
                    state = listState,
                    contentPadding = PaddingValues(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Field("Nombre de la sesión", draft.name) { update { copy(name = it) } }
                            Field("Descripción", draft.description, singleLine = false) { update { copy(description = it) } }
                            Toggle("Mostrar Total", draft.showTotal) { update { copy(showTotal = it) } }
                            Toggle("Permitir negativos", draft.allowNegative) { update { copy(allowNegative = it) } }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    if (editor.isNew) "Contadores"
                                    else "Activos · ${draft.activeCounters.size} de ${draft.counters.size}",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f),
                                )
                                TextButton(onClick = {
                                    val number = editor.session.counters.size + 1
                                    update { copy(counters = counters + Counter(newId(), "Contador $number")) }
                                    listState.requestScrollToItem(number)
                                }) { Text("Agregar contador") }
                            }
                        }
                    }
                    itemsIndexed(draft.counters, key = { _, counter -> counter.id }) { index, counter ->
                        HorizontalDivider()
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Field("Nombre del contador", counter.name) { editCounter(counter.copy(name = it)) }
                            if (!editor.isNew) {
                                Toggle("Activo", counter.active) { editCounter(counter.copy(active = it)) }
                            }
                            Toggle("Incluir en el total", counter.included) { editCounter(counter.copy(included = it)) }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(
                                    onClick = { move(index, -1) },
                                    enabled = index > 0,
                                    modifier = Modifier.semantics { contentDescription = "Subir ${counter.name}" },
                                ) { Text("↑") }
                                TextButton(
                                    onClick = { move(index, 1) },
                                    enabled = index < draft.counters.lastIndex,
                                    modifier = Modifier.semantics { contentDescription = "Bajar ${counter.name}" },
                                ) { Text("↓") }
                                TextButton(
                                    onClick = { removeId = counter.id },
                                    enabled = draft.counters.size > 1,
                                ) { Text("Borrar") }
                            }
                        }
                    }
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onClose) { Text("Cancelar") }
                    Button(onClick = ::save) { Text("Guardar") }
                }
            }
        }
    }

    val toRemove = draft.counters.firstOrNull { it.id == removeId }
    if (toRemove != null) {
        Confirm("Borrar contador", "Se borrará «${toRemove.name}» al guardar la sesión.", "Borrar", { removeId = null }) {
            update { copy(counters = counters.filterNot { it.id == toRemove.id }) }
            removeId = null
            true
        }
    }
}

@Composable
private fun Field(label: String, value: String, singleLine: Boolean = true, change: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = change,
        label = { Text(label) },
        singleLine = singleLine,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun Toggle(label: String, checked: Boolean, change: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked, onCheckedChange = change, modifier = Modifier.semantics { contentDescription = label })
    }
}

@Composable
internal fun ValueDialog(counter: Counter, allowNegative: Boolean, onClose: () -> Unit, commit: (Int) -> Boolean) {
    var input by rememberSaveable(counter.id) { mutableStateOf(counter.value.toString()) }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(counter.name) },
        text = {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it; error = null },
                label = { Text("Valor entero") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = if (allowNegative) KeyboardType.Text else KeyboardType.Number),
                isError = error != null,
                supportingText = error?.let { message -> { Text(message) } },
            )
        },
        dismissButton = { TextButton(onClick = onClose) { Text("Cancelar") } },
        confirmButton = {
            Button(onClick = {
                val value = input.trim().toIntOrNull()
                error = when {
                    value == null -> "Escribe un número entero válido."
                    value < 0 && !allowNegative -> "La sesión no permite valores negativos."
                    !commit(value) -> "No se pudo guardar el valor."
                    else -> null
                }
            }) { Text("Guardar") }
        },
    )
}
