package br.edu.iftm.deadlinetracker

import android.Manifest
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import br.edu.iftm.deadlinetracker.databinding.ActivityMainBinding
import br.edu.iftm.deadlinetracker.scheduling.Notifications
import br.edu.iftm.deadlinetracker.ui.components.applySystemBarsPadding
import br.edu.iftm.deadlinetracker.ui.components.colorOf
import br.edu.iftm.deadlinetracker.ui.components.enableFullScreen
import br.edu.iftm.deadlinetracker.ui.components.setTabSelected
import br.edu.iftm.deadlinetracker.ui.detail.DetailActivity
import br.edu.iftm.deadlinetracker.ui.form.FormActivity
import br.edu.iftm.deadlinetracker.ui.home.Filter
import br.edu.iftm.deadlinetracker.ui.home.HomeUiState
import br.edu.iftm.deadlinetracker.ui.home.HomeViewModel
import br.edu.iftm.deadlinetracker.ui.home.ObligationAdapter
import br.edu.iftm.deadlinetracker.ui.summary.SummaryActivity
import br.edu.iftm.deadlinetracker.util.Formats
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: HomeViewModel by viewModels { HomeViewModel.Factory }

    private val adapter = ObligationAdapter { id ->
        startActivity(DetailActivity.intent(this, id))
    }

    private val permissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        updateBell()
        if (!granted) {
            showPermissionNotice()
        }
    }

    /**
     * Monta a tela inicial, liga os cliques e pede a permissão de notificação na primeira abertura.
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
        binding.summaryBlock.setOnClickListener {
            startActivity(Intent(this, SummaryActivity::class.java))
        }
        binding.notificationsButton.setOnClickListener { onBellClick() }
        tabs().forEach { (filter, tab) ->
            tab.setOnClickListener { viewModel.selectFilter(filter) }
        }
        observeState()

        if (savedInstanceState == null) {
            requestPermissionIfNeeded()
        }
    }

    /**
     * Atualiza o relógio de referência e o ícone do sino sempre que a tela volta ao primeiro plano.
     */
    override fun onResume() {
        super.onResume()
        viewModel.refreshClock()
        updateBell()
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

    /**
     * Pede a permissão de notificação no Android 13 ou superior quando ela ainda não foi concedida.
     */
    private fun requestPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !Notifications.areAllowed(this)) {
            permissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    /**
     * Pinta o sino na cor de destaque e troca o ícone quando as notificações estão bloqueadas.
     */
    private fun updateBell() {
        val allowed = Notifications.areAllowed(this)
        binding.notificationsButton.setImageResource(
            if (allowed) R.drawable.ic_notifications else R.drawable.ic_notifications_off
        )
        binding.notificationsButton.imageTintList = ColorStateList.valueOf(
            colorOf(if (allowed) R.color.text_2 else R.color.accent)
        )
    }

    /**
     * Pede a permissão quando o sistema ainda permite. Caso contrário abre as configurações de notificação.
     */
    private fun onBellClick() {
        val canAsk = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !Notifications.areAllowed(this) &&
            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
        if (canAsk) {
            permissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            openNotificationSettings()
        }
    }

    /**
     * Avisa que os lembretes não vão aparecer e oferece o atalho para as configurações.
     */
    private fun showPermissionNotice() {
        Snackbar.make(binding.root, R.string.notice_permission, Snackbar.LENGTH_LONG)
            .setAnchorView(binding.addButton)
            .setAction(R.string.action_enable) { openNotificationSettings() }
            .show()
    }

    /**
     * Abre a tela de notificações do app nas configurações do sistema.
     */
    private fun openNotificationSettings() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.fromParts("package", packageName, null))
        }
        startActivity(intent)
    }
}
