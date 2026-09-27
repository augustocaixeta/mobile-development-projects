package br.edu.iftm.deadlinetracker.ui.detail

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import br.edu.iftm.deadlinetracker.R
import br.edu.iftm.deadlinetracker.data.Obligation
import br.edu.iftm.deadlinetracker.data.ObligationType
import br.edu.iftm.deadlinetracker.data.ObligationWithReminders
import br.edu.iftm.deadlinetracker.data.Reminder
import br.edu.iftm.deadlinetracker.data.ReminderState
import br.edu.iftm.deadlinetracker.data.isCompleted
import br.edu.iftm.deadlinetracker.databinding.ActivityDetailBinding
import br.edu.iftm.deadlinetracker.databinding.DetailRowBinding
import br.edu.iftm.deadlinetracker.databinding.ItemReminderBinding
import br.edu.iftm.deadlinetracker.ui.components.Labels
import br.edu.iftm.deadlinetracker.ui.components.applySystemBarsPadding
import br.edu.iftm.deadlinetracker.ui.components.colorOf
import br.edu.iftm.deadlinetracker.ui.components.enableFullScreen
import br.edu.iftm.deadlinetracker.ui.form.FormActivity
import br.edu.iftm.deadlinetracker.util.Formats
import br.edu.iftm.deadlinetracker.util.daysUntil
import br.edu.iftm.deadlinetracker.util.toLocalDateTime
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.time.Duration
import java.time.LocalDateTime
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    private val viewModel: DetailViewModel by viewModels { DetailViewModel.Factory }

    /**
     * Monta a tela de detalhe e liga as ações de voltar, menu, liquidar e editar.
     *
     * @param savedInstanceState estado salvo pelo sistema.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableFullScreen()
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()

        binding.backButton.setOnClickListener { finish() }
        binding.moreButton.setOnClickListener { showMenu(it) }
        binding.primaryButton.setOnClickListener { viewModel.toggleCompletion() }
        binding.secondaryButton.setOnClickListener { openEditor() }
        observe()
    }

    /**
     * Coleta estado e eventos do ViewModel enquanto a tela está visível.
     */
    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { state ->
                        when (state) {
                            DetailState.Loading -> Unit
                            DetailState.Removed -> finish()
                            is DetailState.Ready -> render(state.data)
                        }
                    }
                }
                launch {
                    viewModel.events.collect { event ->
                        val message = when (event) {
                            DetailEvent.COMPLETED -> R.string.notice_completed
                            DetailEvent.REOPENED -> R.string.notice_reopened
                        }
                        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
                            .setAnchorView(binding.footer)
                            .show()
                    }
                }
            }
        }
    }

    /**
     * Preenche a tela seguindo o frame Detalhe do Figma: sobrelinha, título, métrica,
     * linhas de detalhe, lembretes e botões de ação.
     *
     * @param data obrigação com os lembretes.
     */
    private fun render(data: ObligationWithReminders) {
        val obligation = data.obligation
        val now = LocalDateTime.now()
        val due = obligation.dueAt.toLocalDateTime()
        val overdue = !obligation.isCompleted && due.isBefore(now)

        binding.content.isVisible = true
        binding.overline.text = getString(
            R.string.pair_format,
            getString(Labels.type(obligation.type)),
            getString(statusOf(obligation, overdue))
        )
        binding.title.text = obligation.title
        if (obligation.type == ObligationType.DEADLINE) {
            bindDeadlineMetric(obligation, due, now, overdue)
        } else {
            bindAmountMetric(obligation, due, now, overdue)
        }

        bindRow(
            binding.rowPayee,
            Labels.payee(obligation.type),
            obligation.payee.ifBlank { getString(R.string.not_informed) }
        )
        bindRow(
            binding.rowDue,
            if (obligation.type == ObligationType.DEADLINE) R.string.label_delivery else R.string.label_due,
            getString(
                R.string.pair_format,
                Formats.fullDate(due.toLocalDate()),
                Formats.time(due.toLocalTime())
            )
        )
        bindRow(
            binding.rowRepeat,
            R.string.label_repeat,
            getString(if (obligation.repeatMonthly) R.string.repeat_monthly else R.string.repeat_none)
        )
        bindRow(
            binding.rowCreated,
            R.string.label_created,
            Formats.fullDate(obligation.createdAt.toLocalDateTime().toLocalDate())
        )
        bindReminders(data.reminders)

        binding.primaryButton.setText(
            if (obligation.isCompleted) R.string.action_reopen else Labels.settleAction(obligation.type)
        )
    }

    /**
     * Para faturas a métrica é o valor, com a situação do vencimento logo abaixo.
     *
     * @param obligation fatura exibida.
     * @param due data e hora do vencimento.
     * @param now instante de referência.
     * @param overdue true quando a fatura está pendente e vencida.
     */
    private fun bindAmountMetric(
        obligation: Obligation,
        due: LocalDateTime,
        now: LocalDateTime,
        overdue: Boolean
    ) {
        binding.metricLabel.setText(R.string.metric_amount)
        binding.metric.text = Formats.currency(obligation.amountCents ?: 0L)
        val time = Formats.time(due.toLocalTime())
        val days = now.toLocalDate().daysUntil(due.toLocalDate())
        val completedAt = obligation.completedAt
        val (text, color) = when {
            completedAt != null -> getString(
                R.string.completed_on,
                getString(Labels.completedStatus(obligation.type)),
                Formats.shortDate(completedAt.toLocalDateTime().toLocalDate())
            ) to R.color.positive
            overdue && days == 0 -> getString(R.string.overdue_today, time) to R.color.accent
            overdue -> resources.getQuantityString(
                R.plurals.overdue_since,
                -days,
                -days,
                Formats.shortDate(due.toLocalDate())
            ) to R.color.accent
            days == 0 -> getString(R.string.due_today, time) to R.color.accent
            days == 1 -> getString(R.string.due_tomorrow, time) to R.color.positive
            else -> resources.getQuantityString(
                R.plurals.due_in,
                days,
                days,
                Formats.shortDate(due.toLocalDate())
            ) to R.color.positive
        }
        showMetricDetail(text, color)
    }

    /**
     * Para prazos a métrica é o tempo que falta ou o atraso. Depois de entregue mostra a data da entrega.
     *
     * @param obligation prazo exibido.
     * @param due data e hora da entrega.
     * @param now instante de referência.
     * @param overdue true quando o prazo está pendente e vencido.
     */
    private fun bindDeadlineMetric(
        obligation: Obligation,
        due: LocalDateTime,
        now: LocalDateTime,
        overdue: Boolean
    ) {
        val date = Formats.shortDate(due.toLocalDate())
        val time = Formats.time(due.toLocalTime())
        val completedAt = obligation.completedAt
        when {
            completedAt != null -> {
                binding.metricLabel.setText(R.string.metric_delivered)
                binding.metric.text = Formats.shortDate(completedAt.toLocalDateTime().toLocalDate())
                showMetricDetail(getString(R.string.deadline_was, date, time), R.color.positive)
            }
            overdue -> {
                binding.metricLabel.setText(R.string.metric_late)
                binding.metric.text = duration(due, now)
                showMetricDetail(getString(R.string.deadline_was, date, time), R.color.accent)
            }
            else -> {
                val today = due.toLocalDate() == now.toLocalDate()
                binding.metricLabel.setText(R.string.metric_remaining)
                binding.metric.text = duration(now, due)
                showMetricDetail(
                    getString(R.string.delivery_on, date, time),
                    if (today) R.color.accent else R.color.positive
                )
            }
        }
    }

    /**
     * Exibe o texto abaixo da métrica com a cor indicada.
     *
     * @param text texto da linha.
     * @param color recurso de cor do texto.
     */
    private fun showMetricDetail(text: String, @ColorRes color: Int) {
        binding.metricDetail.text = text
        binding.metricDetail.setTextColor(colorOf(color))
    }

    /**
     * Calcula o tempo entre dois instantes em dias corridos. No mesmo dia usa horas ou minutos.
     *
     * @param start instante inicial.
     * @param end instante final.
     * @return texto como 3 dias, 5 h ou 40 min.
     */
    private fun duration(start: LocalDateTime, end: LocalDateTime): String {
        val days = start.toLocalDate().daysUntil(end.toLocalDate())
        if (days > 0) {
            return resources.getQuantityString(R.plurals.duration_days, days, days)
        }
        val minutes = Duration.between(start, end).toMinutes().coerceAtLeast(0)
        return if (minutes >= 60) {
            getString(R.string.duration_hours, minutes / 60)
        } else {
            getString(R.string.duration_minutes, minutes)
        }
    }

    /**
     * Preenche uma linha do componente Linha de detalhe.
     *
     * @param row binding da linha.
     * @param label recurso de texto do rótulo.
     * @param value valor exibido à direita.
     */
    private fun bindRow(row: DetailRowBinding, @StringRes label: Int, value: String) {
        row.label.setText(label)
        row.value.text = value
    }

    /**
     * Lista os lembretes em ordem de disparo. O ícone indica se já disparou, se está agendado
     * ou se foi cancelado pela liquidação.
     *
     * @param reminders lembretes da obrigação.
     */
    private fun bindReminders(reminders: List<Reminder>) {
        val scheduled = reminders.count { it.state == ReminderState.SCHEDULED }
        binding.reminderCount.text = resources.getQuantityString(
            R.plurals.reminders_scheduled,
            scheduled,
            scheduled
        )
        binding.noReminders.isVisible = reminders.isEmpty()
        binding.reminderList.removeAllViews()
        reminders.sortedBy { it.triggerAt }.forEach { reminder ->
            val item = ItemReminderBinding.inflate(layoutInflater, binding.reminderList, true)
            val (icon, iconColor) = when (reminder.state) {
                ReminderState.FIRED -> R.drawable.ic_check_circle to R.color.positive
                ReminderState.SCHEDULED -> R.drawable.ic_schedule to R.color.text_3
                ReminderState.CANCELED -> R.drawable.ic_notifications_off to R.color.text_3
            }
            item.icon.setImageResource(icon)
            ImageViewCompat.setImageTintList(item.icon, ColorStateList.valueOf(colorOf(iconColor)))
            item.label.setText(Labels.offset(reminder.offset))
            item.label.setTextColor(
                colorOf(if (reminder.state == ReminderState.CANCELED) R.color.text_3 else R.color.text)
            )
            item.time.text = Formats.dayMonthTime(reminder.triggerAt.toLocalDateTime())
        }
    }

    /**
     * Abre o menu com as opções de editar e excluir.
     *
     * @param anchor view onde o menu aparece.
     */
    private fun showMenu(anchor: View) {
        val menu = PopupMenu(this, anchor)
        menu.menuInflater.inflate(R.menu.menu_detail, menu.menu)
        menu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_edit -> openEditor()
                R.id.menu_delete -> confirmDelete()
            }
            true
        }
        menu.show()
    }

    /**
     * Abre o formulário em modo de edição.
     */
    private fun openEditor() {
        startActivity(FormActivity.intent(this, viewModel.id))
    }

    /**
     * Pede confirmação antes de excluir o registro.
     */
    private fun confirmDelete() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_title)
            .setMessage(R.string.delete_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete_confirm) { _, _ -> viewModel.delete() }
            .show()
    }

    /**
     * Define a situação exibida na sobrelinha.
     *
     * @param obligation obrigação exibida.
     * @param overdue true quando está pendente e vencida.
     * @return recurso de texto como pendente, atrasado ou paga.
     */
    @StringRes
    private fun statusOf(obligation: Obligation, overdue: Boolean): Int = when {
        obligation.isCompleted -> Labels.completedStatus(obligation.type)
        overdue -> R.string.status_overdue
        else -> R.string.status_pending
    }

    companion object {
        const val EXTRA_ID = "obligation_id"

        /**
         * Cria o intent que abre o detalhe de uma obrigação, usado pela lista e pela notificação.
         *
         * @param context contexto de origem.
         * @param id identificador da obrigação.
         * @return intent explícito para esta activity.
         */
        fun intent(context: Context, id: Long): Intent =
            Intent(context, DetailActivity::class.java).putExtra(EXTRA_ID, id)
    }
}
