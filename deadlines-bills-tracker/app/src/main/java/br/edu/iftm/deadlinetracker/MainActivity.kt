package br.edu.iftm.deadlinetracker

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import br.edu.iftm.deadlinetracker.databinding.ActivityMainBinding
import br.edu.iftm.deadlinetracker.ui.components.applySystemBarsPadding
import br.edu.iftm.deadlinetracker.ui.components.enableFullScreen
import br.edu.iftm.deadlinetracker.ui.components.setTabSelected
import br.edu.iftm.deadlinetracker.ui.detail.DetailActivity
import br.edu.iftm.deadlinetracker.ui.form.FormActivity
import br.edu.iftm.deadlinetracker.ui.home.Filter
import br.edu.iftm.deadlinetracker.ui.home.HomeUiState
import br.edu.iftm.deadlinetracker.ui.home.HomeViewModel
import br.edu.iftm.deadlinetracker.ui.home.ObligationAdapter
import br.edu.iftm.deadlinetracker.util.Formats
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: HomeViewModel by viewModels { HomeViewModel.Factory }

    private val adapter = ObligationAdapter { id ->
        startActivity(DetailActivity.intent(this, id))
    }

    /**
     * Monta a tela inicial e liga as abas de filtro e o botão de novo registro.
     *
     * @param savedInstanceState estado salvo pelo sistema, null na primeira abertura.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableFullScreen()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()

        binding.list.adapter = adapter
        binding.addButton.setOnClickListener {
            startActivity(FormActivity.intent(this))
        }
        tabs().forEach { (filter, tab) ->
            tab.setOnClickListener { viewModel.selectFilter(filter) }
        }
        observeState()
    }

    /**
     * Atualiza o relógio de referência sempre que a tela volta ao primeiro plano.
     */
    override fun onResume() {
        super.onResume()
        viewModel.refreshClock()
    }

    /**
     * Relaciona cada filtro com a aba correspondente.
     *
     * @return mapa de filtro para a view da aba.
     */
    private fun tabs() = mapOf(
        Filter.ALL to binding.tabAll,
        Filter.PAYABLE to binding.tabPayable,
        Filter.RECEIVABLE to binding.tabReceivable,
        Filter.DEADLINES to binding.tabDeadlines
    )

    /**
     * Coleta o estado do ViewModel enquanto a tela está visível.
     */
    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { render(it) }
            }
        }
    }

    /**
     * Atualiza cabeçalho, resumo do mês, abas e lista.
     *
     * @param state estado atual da tela.
     */
    private fun render(state: HomeUiState) {
        binding.dateText.text = Formats.header(state.today)
        binding.summaryLabel.text = getString(R.string.home_summary_label, Formats.monthName(state.today))
        binding.summaryAmount.text = Formats.currency(state.totalPayable)
        binding.completedCount.text = resources.getQuantityString(
            R.plurals.home_completed_count,
            state.completedThisMonth,
            state.completedThisMonth
        )
        binding.completedLabel.text = resources.getQuantityString(
            R.plurals.home_completed_suffix,
            state.completedThisMonth
        )
        binding.progress.setProgress(state.percent, true)
        binding.progressText.text = getString(R.string.home_progress, state.percent)
        tabs().forEach { (filter, tab) -> tab.setTabSelected(filter == state.filter) }
        adapter.submitList(state.items)
        binding.empty.isVisible = state.loaded && state.items.isEmpty()
    }
}
