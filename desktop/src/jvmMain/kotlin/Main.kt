import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import com.programmersbox.common.UIShow
import moe.tlaster.precompose.PreComposeWindow

fun main() = application {
    PreComposeWindow(onCloseRequest = ::exitApplication) {
        MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
            UIShow()
        }
    }
}
