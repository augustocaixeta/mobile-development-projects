package br.edu.iftm.deadlinetracker.scheduling

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import br.edu.iftm.deadlinetracker.MainActivity
import br.edu.iftm.deadlinetracker.R
import br.edu.iftm.deadlinetracker.data.Obligation
import br.edu.iftm.deadlinetracker.ui.components.Labels
import br.edu.iftm.deadlinetracker.ui.detail.DetailActivity
import br.edu.iftm.deadlinetracker.util.Formats
import br.edu.iftm.deadlinetracker.util.daysUntil
import br.edu.iftm.deadlinetracker.util.toLocalDateTime
import java.time.LocalDate

object Notifications {

    private const val CHANNEL_REMINDERS = "reminders"
    private const val CHANNEL_OVERDUE = "overdue"
    private const val OVERDUE_ID = 0
    private const val SEPARATOR = " · "
    private const val FLAGS = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

    /**
     * Define o id da notificação de uma obrigação, assim um lembrete novo substitui o anterior.
     *
     * @param obligationId identificador da obrigação.
     * @return id usado no NotificationManager.
     */
    fun idFor(obligationId: Long): Int = obligationId.toInt()

    /**
     * Cria os canais de notificação do app, exibidos nas configurações do Android 8 ou superior.
     *
     * @param context contexto da aplicação.
     */
    fun createChannels(context: Context) {
        val reminders = NotificationChannelCompat.Builder(CHANNEL_REMINDERS, NotificationManagerCompat.IMPORTANCE_HIGH)
            .setName(context.getString(R.string.channel_reminders))
            .setDescription(context.getString(R.string.channel_reminders_description))
            .build()
        val overdue = NotificationChannelCompat.Builder(CHANNEL_OVERDUE, NotificationManagerCompat.IMPORTANCE_DEFAULT)
            .setName(context.getString(R.string.channel_overdue))
            .setDescription(context.getString(R.string.channel_overdue_description))
            .build()
        NotificationManagerCompat.from(context).createNotificationChannelsCompat(listOf(reminders, overdue))
    }

    /**
     * Verifica se o app pode exibir notificações. No Android 13 ou superior depende da permissão POST_NOTIFICATIONS.
     *
     * @param context qualquer contexto do app.
     * @return true quando as notificações estão liberadas.
     */
    fun areAllowed(context: Context): Boolean {
        val enabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return enabled
        }
        val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
        return enabled && permission == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Exibe a notificação preventiva de uma obrigação, com toque para abrir o detalhe
     * e um botão para marcar como paga, recebida ou entregue.
     *
     * @param context contexto da aplicação.
     * @param obligation obrigação que está perto do vencimento.
     */
    fun showReminder(context: Context, obligation: Obligation) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val id = idFor(obligation.id)
        val open = TaskStackBuilder.create(context)
            .addNextIntentWithParentStack(DetailActivity.intent(context, obligation.id))
            .getPendingIntent(id, FLAGS)
        val settle = PendingIntent.getBroadcast(
            context,
            id,
            SettleReceiver.intent(context, obligation.id),
            FLAGS
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_event_available)
            .setColor(ContextCompat.getColor(context, R.color.accent))
            .setContentTitle(title(context, obligation))
            .setContentText(text(context, obligation))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(open)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_check, context.getString(Labels.settleAction(obligation.type)), settle)
            .build()
        NotificationManagerCompat.from(context).notify(id, notification)
    }

    /**
     * Exibe o resumo diário com a quantidade de registros atrasados.
     *
     * @param context contexto da aplicação.
     * @param overdue obrigações pendentes que já venceram.
     */
    fun showOverdue(context: Context, overdue: List<Obligation>) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val open = PendingIntent.getActivity(
            context,
            OVERDUE_ID,
            Intent(context, MainActivity::class.java),
            FLAGS
        )
        val count = overdue.size
        val notification = NotificationCompat.Builder(context, CHANNEL_OVERDUE)
            .setSmallIcon(R.drawable.ic_event_available)
            .setColor(ContextCompat.getColor(context, R.color.accent))
            .setContentTitle(
                context.resources.getQuantityString(R.plurals.notification_overdue_title, count, count)
            )
            .setContentText(overdue.take(3).joinToString(", ") { it.title })
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(OVERDUE_ID, notification)
    }

    /**
     * Monta o título da notificação conforme a distância até o vencimento.
     *
     * @param context contexto para ler as strings.
     * @param obligation obrigação notificada.
     * @return texto como Conta de energia vence amanhã.
     */
    private fun title(context: Context, obligation: Obligation): String {
        val days = LocalDate.now().daysUntil(obligation.dueAt.toLocalDateTime().toLocalDate())
        return when {
            days <= 0 -> context.getString(R.string.notification_due_today, obligation.title)
            days == 1 -> context.getString(R.string.notification_due_tomorrow, obligation.title)
            else -> context.resources.getQuantityString(R.plurals.notification_due_in, days, obligation.title, days)
        }
    }

    /**
     * Monta o corpo da notificação com valor, favorecido e horário.
     *
     * @param context contexto para ler as strings.
     * @param obligation obrigação notificada.
     * @return texto como R$ 312,00 · CEMIG · 30/09 às 09:30.
     */
    private fun text(context: Context, obligation: Obligation): String {
        val due = obligation.dueAt.toLocalDateTime()
        val whenText = context.getString(
            R.string.notification_when,
            Formats.dayMonth(due.toLocalDate()),
            Formats.time(due.toLocalTime())
        )
        return listOfNotNull(
            obligation.amountCents?.let { Formats.currency(it) },
            obligation.payee.takeIf { it.isNotBlank() },
            whenText
        ).joinToString(SEPARATOR)
    }
}
