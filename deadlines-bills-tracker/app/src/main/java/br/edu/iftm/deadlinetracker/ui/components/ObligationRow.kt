package br.edu.iftm.deadlinetracker.ui.components

import androidx.core.view.isVisible
import br.edu.iftm.deadlinetracker.R
import br.edu.iftm.deadlinetracker.data.Obligation
import br.edu.iftm.deadlinetracker.data.ObligationType
import br.edu.iftm.deadlinetracker.data.isCompleted
import br.edu.iftm.deadlinetracker.databinding.ItemObligationBinding
import br.edu.iftm.deadlinetracker.util.Formats
import br.edu.iftm.deadlinetracker.util.toLocalDateTime
import java.time.LocalDateTime

object ObligationRow {

    /**
     * Preenche o componente Linha do Figma com os dados da obrigação.
     * Atrasadas ficam com o dia e o status na cor de destaque, concluídas mostram a pílula verde.
     *
     * @param row binding da linha já inflada.
     * @param obligation obrigação exibida.
     * @param now instante usado como referência para atraso e distância em dias.
     */
    fun bind(row: ItemObligationBinding, obligation: Obligation, now: LocalDateTime) {
        val context = row.root.context
        val due = obligation.dueAt.toLocalDateTime()
        val date = due.toLocalDate()
        val overdue = !obligation.isCompleted && due.isBefore(now)
        val urgent = overdue || (!obligation.isCompleted && date == now.toLocalDate())

        row.day.text = Formats.day(date)
        row.day.setTextColor(context.colorOf(if (overdue) R.color.accent else R.color.text))
        row.weekday.text = Formats.weekday(date)
        row.title.text = obligation.title
        row.subtitle.text = Labels.subtitle(context, obligation)

        row.completedPill.isVisible = obligation.isCompleted
        row.amountGroup.isVisible = !obligation.isCompleted
        if (obligation.isCompleted) {
            row.completedText.setText(Labels.completedPill(obligation.type))
            return
        }

        row.amount.text = if (obligation.type == ObligationType.DEADLINE || obligation.amountCents == null) {
            Formats.time(due.toLocalTime())
        } else {
            Formats.currency(obligation.amountCents)
        }
        row.amount.setTextColor(context.colorOf(Labels.amountColor(obligation.type)))
        row.status.text = Labels.relativeDay(context, date, now.toLocalDate())
        row.status.setTextColor(context.colorOf(if (urgent) R.color.accent else R.color.text_3))
    }
}
