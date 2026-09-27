package br.edu.iftm.deadlinetracker.ui.components

import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import br.edu.iftm.deadlinetracker.R
import br.edu.iftm.deadlinetracker.data.Obligation
import br.edu.iftm.deadlinetracker.data.ObligationType
import br.edu.iftm.deadlinetracker.data.ReminderOffset
import br.edu.iftm.deadlinetracker.util.daysUntil
import java.time.LocalDate

object Labels {

    /**
     * Nome curto do tipo em minúsculas.
     *
     * @param type tipo da obrigação.
     * @return recurso de texto como a pagar.
     */
    @StringRes
    fun type(type: ObligationType): Int = when (type) {
        ObligationType.PAYABLE -> R.string.type_payable
        ObligationType.RECEIVABLE -> R.string.type_receivable
        ObligationType.DEADLINE -> R.string.type_deadline
    }

    /**
     * Texto da pílula verde exibida em registros concluídos.
     *
     * @param type tipo da obrigação.
     * @return recurso de texto como Paga.
     */
    @StringRes
    fun completedPill(type: ObligationType): Int = when (type) {
        ObligationType.PAYABLE -> R.string.pill_paid
        ObligationType.RECEIVABLE -> R.string.pill_received
        ObligationType.DEADLINE -> R.string.pill_delivered
    }

    /**
     * Situação de uma obrigação concluída em minúsculas.
     *
     * @param type tipo da obrigação.
     * @return recurso de texto como paga.
     */
    @StringRes
    fun completedStatus(type: ObligationType): Int = when (type) {
        ObligationType.PAYABLE -> R.string.status_paid
        ObligationType.RECEIVABLE -> R.string.status_received
        ObligationType.DEADLINE -> R.string.status_delivered
    }

    /**
     * Texto do botão que liquida a obrigação.
     *
     * @param type tipo da obrigação.
     * @return recurso de texto como Marcar como paga.
     */
    @StringRes
    fun settleAction(type: ObligationType): Int = when (type) {
        ObligationType.PAYABLE -> R.string.action_pay
        ObligationType.RECEIVABLE -> R.string.action_receive
        ObligationType.DEADLINE -> R.string.action_deliver
    }

    /**
     * Rótulo de quem está do outro lado da obrigação na tela de detalhe.
     *
     * @param type tipo da obrigação.
     * @return recurso de texto como Favorecido.
     */
    @StringRes
    fun payee(type: ObligationType): Int = when (type) {
        ObligationType.PAYABLE -> R.string.label_payee
        ObligationType.RECEIVABLE -> R.string.label_payer
        ObligationType.DEADLINE -> R.string.label_reference
    }

    /**
     * Texto de cada antecedência, igual ao das pílulas do formulário.
     *
     * @param offset antecedência do lembrete.
     * @return recurso de texto como 1 dia antes.
     */
    @StringRes
    fun offset(offset: ReminderOffset): Int = when (offset) {
        ReminderOffset.ON_DUE_DATE -> R.string.remind_on_due_date
        ReminderOffset.ONE_DAY -> R.string.remind_one_day
        ReminderOffset.THREE_DAYS -> R.string.remind_three_days
        ReminderOffset.ONE_WEEK -> R.string.remind_one_week
    }

    /**
     * Cor do valor na linha. Segue a regra do Figma em que a cor reflete a natureza do valor.
     *
     * @param type tipo da obrigação.
     * @return recurso de cor neutra, positiva ou de tempo.
     */
    @ColorRes
    fun amountColor(type: ObligationType): Int = when (type) {
        ObligationType.PAYABLE -> R.color.text
        ObligationType.RECEIVABLE -> R.color.positive
        ObligationType.DEADLINE -> R.color.text_2
    }

    /**
     * Descreve a distância entre uma data e hoje.
     *
     * @param context contexto para ler as strings.
     * @param date data de referência.
     * @param today data atual.
     * @return texto como há 2 dias, hoje, amanhã ou em 5 dias.
     */
    fun relativeDay(context: Context, date: LocalDate, today: LocalDate): String {
        val days = today.daysUntil(date)
        val resources = context.resources
        return when {
            days == 0 -> context.getString(R.string.relative_today)
            days == 1 -> context.getString(R.string.relative_tomorrow)
            days == -1 -> context.getString(R.string.relative_yesterday)
            days > 1 -> resources.getQuantityString(R.plurals.relative_in_days, days, days)
            else -> resources.getQuantityString(R.plurals.relative_days_ago, -days, -days)
        }
    }

    /**
     * Linha secundária da lista, juntando favorecido e tipo quando há favorecido.
     *
     * @param context contexto para ler as strings.
     * @param obligation obrigação exibida.
     * @return texto como CEMIG · a pagar.
     */
    fun subtitle(context: Context, obligation: Obligation): String {
        val type = context.getString(type(obligation.type))
        if (obligation.payee.isBlank()) {
            return type
        }
        return context.getString(R.string.pair_format, obligation.payee, type)
    }
}
