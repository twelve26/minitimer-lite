package app.contadores

import androidx.compose.ui.window.ComposeUIViewController
import platform.Foundation.NSUUID
import platform.UIKit.UIViewController

fun miniTimerViewController(
    initialJson: String?,
    readFailed: Boolean,
    writeJson: (String) -> String?,
    copyText: (String) -> Unit,
): UIViewController {
    val initial = runCatching { initialJson?.let { notebookJson.decodeFromString<Notebook>(it) } ?: Notebook() }
    val store = NotebookStore(
        initial = initial.getOrDefault(Notebook()),
        write = { notebook ->
            val text = notebookJson.encodeToString(Notebook.serializer(), notebook)
            writeJson(text)?.let { error(it) }
        },
        readFailed = readFailed || initial.isFailure,
    )
    return ComposeUIViewController {
        ContadoresApp(
            store,
            newId = { NSUUID().UUIDString },
            copyText = copyText,
            supportsTwoPane = true,
        )
    }
}
