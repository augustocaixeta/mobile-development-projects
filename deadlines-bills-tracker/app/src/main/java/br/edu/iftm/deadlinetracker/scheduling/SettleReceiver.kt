package br.edu.iftm.deadlinetracker.scheduling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import br.edu.iftm.deadlinetracker.DeadlineTrackerApp
import kotlinx.coroutines.launch

class SettleReceiver : BroadcastReceiver() {

    /**
     * Recebe o toque no botão da notificação e liquida a obrigação sem abrir o app.
     *
     * @param context contexto entregue pelo sistema.
     * @param intent intent com o id da obrigação.
     */
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(EXTRA_OBLIGATION_ID, -1L)
        if (id <= 0) {
            return
        }
        val app = context.applicationContext as DeadlineTrackerApp
        val pending = goAsync()
        app.scope.launch {
            try {
                app.repository.settle(id)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        private const val EXTRA_OBLIGATION_ID = "obligation_id"

        /**
         * Monta o intent disparado pela ação da notificação.
         *
         * @param context contexto usado para criar o intent.
         * @param obligationId identificador da obrigação.
         * @return intent explícito para este receiver.
         */
        fun intent(context: Context, obligationId: Long): Intent =
            Intent(context, SettleReceiver::class.java).putExtra(EXTRA_OBLIGATION_ID, obligationId)
    }
}
