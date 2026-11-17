package app.contadores

import android.app.Application
import android.app.KeyguardManager
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.UUID

class MainActivity : ComponentActivity() {
    private var volumeAction: ((Int) -> Unit)? = null
    private val consumedKeys = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val store = ViewModelProvider(this)[AndroidNotebook::class.java].store
        setContent {
            val dark = usesDarkTheme(store.state.appearance)
            SideEffect {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !dark
                    isAppearanceLightNavigationBars = !dark
                }
            }
            ContadoresApp(
                store,
                newId = { UUID.randomUUID().toString() },
                copyText = ::copyToClipboard,
                bindVolume = { volumeAction = it },
                backHandler = { enabled, back -> BackHandler(enabled, back) },
            )
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(ClipboardManager::class.java) ?: return
        clipboard.setPrimaryClip(ClipData.newPlainText("MiniTimer lite", text))
        if (Build.VERSION.SDK_INT < 33) {
            Toast.makeText(this, getString(R.string.clipboard_copied), Toast.LENGTH_SHORT).show()
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val delta = when (event.keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> 1
            KeyEvent.KEYCODE_VOLUME_DOWN -> -1
            else -> return super.dispatchKeyEvent(event)
        }
        if (event.action == KeyEvent.ACTION_UP && consumedKeys.remove(event.keyCode)) return true
        val action = volumeAction
        if (event.action == KeyEvent.ACTION_DOWN && action != null && hasWindowFocus() &&
            lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) &&
            getSystemService(PowerManager::class.java)?.isInteractive == true &&
            getSystemService(KeyguardManager::class.java)?.isKeyguardLocked == false
        ) {
            if (event.repeatCount == 0) {
                consumedKeys.add(event.keyCode)
                action(delta)
            }
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onPause() {
        super.onPause()
        consumedKeys.clear()
    }
}

internal class AndroidNotebook(application: Application) : AndroidViewModel(application) {
    private val file = File(application.filesDir, "notebook.json")
    private val pending = File(application.filesDir, "notebook.pending")
    private val initial = runCatching {
        try {
            notebookJson.decodeFromString<Notebook>(file.readText())
        } catch (failure: FileNotFoundException) {
            if (file.exists()) throw failure
            Notebook()
        }
    }
    val store = NotebookStore(
        initial = initial.getOrDefault(Notebook()),
        write = { notebook ->
            val bytes = notebookJson.encodeToString(Notebook.serializer(), notebook).encodeToByteArray()
            try {
                pending.outputStream().use { stream ->
                    stream.write(bytes)
                    stream.fd.sync()
                }
                Files.move(pending.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            } finally {
                pending.delete()
            }
        },
        readFailed = initial.isFailure,
    )
}
