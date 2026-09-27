package br.edu.iftm.deadlinetracker.ui.components

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Checkable
import android.widget.Switch
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import br.edu.iftm.deadlinetracker.R

class ToggleSwitch @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs), Checkable {

    private var on = false

    var onChange: ((Boolean) -> Unit)? = null

    init {
        setBackgroundResource(R.drawable.switch_toggle)
        isClickable = true
        isFocusable = true
    }

    /**
     * Informa se o interruptor está ligado.
     *
     * @return true quando ligado.
     */
    override fun isChecked(): Boolean = on

    /**
     * Liga ou desliga o interruptor e avisa o ouvinte apenas quando o estado muda.
     *
     * @param checked novo estado.
     */
    override fun setChecked(checked: Boolean) {
        if (on == checked) {
            return
        }
        on = checked
        refreshDrawableState()
        onChange?.invoke(checked)
    }

    /**
     * Inverte o estado atual.
     */
    override fun toggle() {
        setChecked(!on)
    }

    /**
     * Inverte o estado a cada toque antes de repassar o clique.
     *
     * @return true quando algum ouvinte de clique tratou o evento.
     */
    override fun performClick(): Boolean {
        toggle()
        return super.performClick()
    }

    /**
     * Acrescenta o estado checked para o seletor de fundo trocar a trilha e o botão.
     *
     * @param extraSpace espaço extra pedido pela classe filha.
     * @return estados atuais do drawable.
     */
    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val state = super.onCreateDrawableState(extraSpace + 1)
        if (on) {
            mergeDrawableStates(state, intArrayOf(android.R.attr.state_checked))
        }
        return state
    }

    /**
     * Nome de classe anunciado pelos leitores de tela.
     *
     * @return nome da classe Switch do Android.
     */
    override fun getAccessibilityClassName(): CharSequence = Switch::class.java.name

    /**
     * Informa aos leitores de tela que o componente é um interruptor e se está ligado.
     *
     * @param info nó de acessibilidade preenchido pelo sistema.
     */
    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        val compat = AccessibilityNodeInfoCompat.wrap(info)
        compat.isCheckable = true
        compat.stateDescription = context.getString(if (on) R.string.state_on else R.string.state_off)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            info.checked = if (on) {
                AccessibilityNodeInfo.CHECKED_STATE_TRUE
            } else {
                AccessibilityNodeInfo.CHECKED_STATE_FALSE
            }
        } else {
            setLegacyChecked(info)
        }
    }

    /**
     * Usa a API antiga de estado marcado nas versões anteriores ao Android 16.
     *
     * @param info nó de acessibilidade preenchido pelo sistema.
     */
    @Suppress("DEPRECATION")
    private fun setLegacyChecked(info: AccessibilityNodeInfo) {
        info.isChecked = on
    }
}
