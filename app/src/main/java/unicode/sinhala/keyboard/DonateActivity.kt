package unicode.sinhala.keyboard

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import unicode.sinhala.keyboard.ui.DonateScreen
import unicode.sinhala.keyboard.ui.theme.UnicodeSinhalaTheme

class DonateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UnicodeSinhalaTheme {
                DonateScreen(
                    onBackClick = { onBackPressedDispatcher.onBackPressed() }
                )
            }
        }
    }
}
