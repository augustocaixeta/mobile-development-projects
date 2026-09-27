package br.edu.iftm.deadlinetracker.ui.components

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import br.edu.iftm.deadlinetracker.R

/**
 * Desenha a tela atrás das barras do sistema com ícones claros, já que o tema é sempre escuro.
 *
 * @receiver activity que vai exibir o conteúdo.
 */
fun ComponentActivity.enableFullScreen() {
    enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
    )
}

/**
 * Soma ao padding da view o espaço das barras do sistema. Na base também considera o teclado aberto.
 *
 * @receiver view raiz da tela.
 */
fun View.applySystemBarsPadding() {
    val initial = Insets.of(paddingLeft, paddingTop, paddingRight, paddingBottom)
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val bars = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        val keyboard = insets.getInsets(WindowInsetsCompat.Type.ime())
        view.updatePadding(
            left = initial.left + bars.left,
            top = initial.top + bars.top,
            right = initial.right + bars.right,
            bottom = initial.bottom + maxOf(bars.bottom, keyboard.bottom)
        )
        insets
    }
}

/**
 * Aplica o estado do componente Aba filtro do Figma. A aba ativa fica clara, sublinhada e em peso médio.
 *
 * @receiver texto que representa a aba.
 * @param active true para a aba selecionada.
 */
fun TextView.setTabSelected(active: Boolean) {
    isSelected = active
    val font = if (active) R.font.schibsted_grotesk_medium else R.font.schibsted_grotesk_regular
    typeface = ResourcesCompat.getFont(context, font)
}

/**
 * Lê uma cor dos recursos.
 *
 * @receiver contexto usado para resolver o recurso.
 * @param id recurso de cor.
 * @return cor em ARGB.
 */
@ColorInt
fun Context.colorOf(@ColorRes id: Int): Int = ContextCompat.getColor(this, id)
