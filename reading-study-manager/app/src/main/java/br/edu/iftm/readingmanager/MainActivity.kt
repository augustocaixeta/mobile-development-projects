package br.edu.iftm.readingmanager

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import br.edu.iftm.readingmanager.ui.theme.ReadingTheme

class MainActivity : ComponentActivity() {

    /**
     * Desenha o app de ponta a ponta com ícones claros nas barras do sistema, já que o tema é escuro.
     *
     * @param savedInstanceState estado salvo pelo sistema, se houver.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            ReadingTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ReadingTheme.colors.bg)
                )
            }
        }
    }
}
