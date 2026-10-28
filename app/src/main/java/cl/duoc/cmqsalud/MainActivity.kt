package cl.duoc.cmqsalud

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cl.duoc.cmqsalud.navegacion.CMQSaludApp
import cl.duoc.cmqsalud.ui.theme.CMQsaludTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CMQsaludTheme {
                CMQSaludApp()
            }
        }
    }
}