package br.edu.iftm.deadlinetracker.ui.form

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import br.edu.iftm.deadlinetracker.util.Formats

class CurrencyMask(private val input: EditText) : TextWatcher {

    private var formatting = false

    override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) = Unit

    /**
     * Reescreve o texto como valor em reais a cada tecla, tratando os dígitos como centavos.
     * Digitar 3, 1, 2, 0 e 0 resulta em 312,00.
     *
     * @param text conteúdo editável do campo.
     */
    override fun afterTextChanged(text: Editable) {
        if (formatting) {
            return
        }
        formatting = true
        val digits = text.filter { it.isDigit() }.take(MAX_DIGITS).toString().trimStart('0')
        val formatted = if (digits.isEmpty()) "" else Formats.amount(digits.toLong())
        text.replace(0, text.length, formatted)
        input.setSelection(formatted.length)
        formatting = false
    }

    /**
     * Lê o valor digitado.
     *
     * @return valor em centavos, zero quando o campo está vazio.
     */
    fun cents(): Long = input.text.filter { it.isDigit() }.toString().toLongOrNull() ?: 0L

    companion object {
        private const val MAX_DIGITS = 11
    }
}
