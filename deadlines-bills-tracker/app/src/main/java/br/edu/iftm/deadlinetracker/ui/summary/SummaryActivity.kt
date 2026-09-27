package br.edu.iftm.deadlinetracker.ui.summary

import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import br.edu.iftm.deadlinetracker.R
import br.edu.iftm.deadlinetracker.databinding.ActivitySummaryBinding
import br.edu.iftm.deadlinetracker.databinding.ItemObligationBinding
import br.edu.iftm.deadlinetracker.databinding.ItemStatBinding
import br.edu.iftm.deadlinetracker.ui.components.ObligationRow
import br.edu.iftm.deadlinetracker.ui.components.applySystemBarsPadding
import br.edu.iftm.deadlinetracker.ui.components.enableFullScreen
import br.edu.iftm.deadlinetracker.ui.components.setTabSelected
import br.edu.iftm.deadlinetracker.ui.detail.DetailActivity
import br.edu.iftm.deadlinetracker.util.Formats
import kotlinx.coroutines.launch

class SummaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySummaryBinding

    private val viewModel: SummaryViewModel by viewModels { SummaryViewModel.Factory }

    /**
     * Monta a tela de resumo e passa a observar o estado do ViewModel.
     *
     * @param savedInstanceState estado salvo pelo sistema.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableFullScreen()
        binding = ActivitySummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()

        binding.backButton.setOnClickListener { finish() }
        tabs().forEach { (period, tab) ->
            tab.setOnClickListener { viewModel.selectPeriod(period) }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { render(it) }
            }
        }
    }

    /**
     * Relaciona cada período com a aba correspondente.
     *
     * @return mapa de período para a view da aba.
     */
    private fun tabs() = mapOf(
        Period.WEEK to binding.tabWeek,
        Period.MONTH to binding.tabMonth,
        Period.YEAR to binding.tabYear
    )

    /**
     * Preenche a tela seguindo o frame Desempenho do Figma, trocando páginas lidas por valores pagos.
     *
     * @param state estado atual do resumo.
     */
    private fun render(state: SummaryUiState) {
        tabs().forEach { (period, tab) -> tab.setTabSelected(period == state.period) }
        binding.totalLabel.setText(
            when (state.period) {
                Period.WEEK -> R.string.summary_paid_week
                Period.MONTH -> R.string.summary_paid_month
                Period.YEAR -> R.string.summary_paid_year
            }
        )
        binding.total.text = Formats.currency(state.totalPaid)
        binding.received.text = getString(R.string.summary_received, Formats.currency(state.totalReceived))
        binding.chart.setBars(state.bars)

        bindStat(binding.statCompleted, state.completed, R.string.stat_completed)
        bindStat(binding.statPending, state.pending, R.string.stat_pending)
        bindStat(binding.statOverdue, state.overdue, R.string.stat_overdue)

        binding.recentList.removeAllViews()
        state.recent.forEach { obligation ->
            val row = ItemObligationBinding.inflate(layoutInflater, binding.recentList, true)
            ObligationRow.bind(row, obligation, state.now)
            row.root.setOnClickListener {
                startActivity(DetailActivity.intent(this, obligation.id))
            }
        }
        binding.noRecent.isVisible = state.recent.isEmpty()
    }

    /**
     * Preenche um cartão do componente Stat Tile.
     *
     * @param card binding do cartão.
     * @param value número exibido.
     * @param label recurso de texto do rótulo.
     */
    private fun bindStat(card: ItemStatBinding, value: Int, @StringRes label: Int) {
        card.number.text = Formats.integer(value)
        card.label.setText(label)
    }
}
