package app.contadores

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
internal fun WelcomeScreen(dark: Boolean, toggleTheme: () -> Unit, themeEnabled: Boolean, start: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(start = 24.dp, end = 24.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            ThemeButton(dark, toggleTheme, enabled = themeEnabled)
        }
        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier.widthIn(max = 320.dp).fillMaxWidth()
                    .verticalScroll(rememberScrollState()).padding(top = 40.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(28.dp),
            ) {
                Surface(
                    modifier = Modifier.size(84.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        DrawGlyph(Glyph.Tally, Modifier.size(50.dp))
                    }
                }
                Text(
                    "MiniTimer lite",
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Button(
            onClick = start,
            modifier = Modifier.widthIn(max = 320.dp).fillMaxWidth().heightIn(min = 48.dp),
            shape = RoundedCornerShape(10.dp),
        ) { Text("Comenzar") }
    }
}
