package br.edu.iftm.deadlinetracker.ui.form

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import br.edu.iftm.deadlinetracker.R
import br.edu.iftm.deadlinetracker.data.Obligation
import br.edu.iftm.deadlinetracker.data.ObligationType
import br.edu.iftm.deadlinetracker.data.ReminderOffset
import br.edu.iftm.deadlinetracker.databinding.ActivityFormBinding
import br.edu.iftm.deadlinetracker.ui.components.applySystemBarsPadding
import br.edu.iftm.deadlinetracker.ui.components.colorOf
import br.edu.iftm.deadlinetracker.ui.components.enableFullScreen
import br.edu.iftm.deadlinetracker.util.Formats
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.launch

class FormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormBinding
    private lateinit var currencyMask: CurrencyMask

    private val viewModel: FormViewModel by viewModels { FormViewModel.Factory }

    /**
     * Monta o formulário de novo registro ou de edição.
     *
     * @param savedInstanceState estado salvo pelo sistema.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableFullScreen()
        binding = ActivityFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()

        binding.screenTitle.setText(if (viewModel.isEditing) R.string.form_edit else R.string.form_new)
        currencyMask = CurrencyMask(binding.amountInput)
        binding.amountInput.addTextChangedListener(currencyMask)
        bindListeners()
        observe()
    }

    /**
     * Relaciona cada tipo com o segmento correspondente.
     *
     * @return mapa de tipo para a view do segmento.
     */
    private fun segments() = mapOf(
        ObligationType.PAYABLE to binding.segmentPayable,
        ObligationType.RECEIVABLE to binding.segmentReceivable,
        ObligationType.DEADLINE to binding.segmentDeadline
    )

    /**
     * Relaciona cada antecedência com a pílula correspondente.
     *
     * @return mapa de antecedência para o chip.
     */
    private fun pills() = mapOf(
        ReminderOffset.ON_DUE_DATE to binding.pillOnDueDate,
        ReminderOffset.ONE_DAY to binding.pillOneDay,
        ReminderOffset.THREE_DAYS to binding.pillThreeDays,
        ReminderOffset.ONE_WEEK to binding.pillOneWeek
    )

    /**
     * Liga os cliques e ouvintes de texto aos métodos do ViewModel.
     */
    private fun bindListeners() {
        binding.closeButton.setOnClickListener { finish() }
        segments().forEach { (type, segment) ->
            segment.setOnClickListener { viewModel.selectType(type) }
        }
        pills().forEach { (offset, pill) ->
            pill.setOnCheckedChangeListener { _, selected -> viewModel.toggleOffset(offset, selected) }
        }
        binding.dateField.setOnClickListener { openDatePicker() }
        binding.timeField.setOnClickListener { openTimePicker() }
        binding.repeatRow.setOnClickListener { binding.repeatSwitch.toggle() }
        binding.repeatSwitch.onChange = { enabled -> viewModel.setRepeatMonthly(enabled) }
        binding.amountInput.doAfterTextChanged { viewModel.clearError(Field.AMOUNT) }
        binding.titleInput.doAfterTextChanged { viewModel.clearError(Field.TITLE) }
        binding.payeeInput.doAfterTextChanged { viewModel.clearError(Field.PAYEE) }
        binding.saveButton.setOnClickListener { save() }
    }

    /**
     * Coleta estado e eventos do ViewModel enquanto a tela está visível.
     */
    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.state.collect { render(it) } }
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is FormEvent.Loaded -> fillInputs(event.obligation)
                            FormEvent.Saved -> finish()
                        }
                    }
                }
            }
        }
    }

    /**
     * Ajusta o formulário ao tipo escolhido e mostra os erros de validação
     * pintando rótulo e linha do campo na cor de destaque.
     *
     * @param state estado atual do formulário.
     */
    private fun render(state: FormState) {
        val isDeadline = state.type == ObligationType.DEADLINE
        segments().forEach { (type, segment) -> segment.isSelected = type == state.type }
        binding.amountField.isVisible = !isDeadline
        binding.titleInput.setHint(
            if (isDeadline) R.string.hint_description_deadline else R.string.hint_description_bill
        )
        binding.payeeInput.setHint(
            when (state.type) {
                ObligationType.PAYABLE -> R.string.hint_payee
                ObligationType.RECEIVABLE -> R.string.hint_payer
                ObligationType.DEADLINE -> R.string.hint_reference
            }
        )
        binding.dateLabel.setText(if (isDeadline) R.string.field_delivery else R.string.field_due)
        binding.dateText.text = Formats.fullDate(state.date)
        binding.timeText.text = Formats.time(state.time)
        pills().forEach { (offset, pill) -> pill.isChecked = offset in state.offsets }
        binding.repeatSwitch.isChecked = state.repeatMonthly

        showError(binding.amountLabel, null, R.string.field_amount, R.string.error_amount, state.errors.amount)
        showError(
            binding.titleLabel,
            binding.titleField,
            R.string.field_description,
            R.string.error_description,
            state.errors.title
        )
        val (payeeLabel, payeeError) = when (state.type) {
            ObligationType.PAYABLE -> R.string.field_payee to R.string.error_payee
            ObligationType.RECEIVABLE -> R.string.field_payer to R.string.error_payer
            ObligationType.DEADLINE -> R.string.field_reference to R.string.field_reference
        }
        showError(binding.payeeLabel, binding.payeeField, payeeLabel, payeeError, state.errors.payee)
        binding.saveButton.isEnabled = !state.saving
    }

    /**
     * Alterna um campo entre o estado normal e o estado de erro.
     *
     * @param label rótulo do campo.
     * @param field container com a linha inferior, ou null quando o campo não tem linha.
     * @param normalText recurso de texto do rótulo normal.
     * @param errorText recurso de texto da mensagem de erro.
     * @param hasError true para exibir o erro.
     */
    private fun showError(
        label: TextView,
        field: View?,
        @StringRes normalText: Int,
        @StringRes errorText: Int,
        hasError: Boolean
    ) {
        label.setText(if (hasError) errorText else normalText)
        label.setTextColor(colorOf(if (hasError) R.color.accent else R.color.text_3))
        field?.setBackgroundResource(if (hasError) R.drawable.line_bottom_accent else R.drawable.line_bottom)
    }

    /**
     * Preenche os campos de texto quando a tela abre para edição.
     *
     * @param obligation obrigação carregada do banco.
     */
    private fun fillInputs(obligation: Obligation) {
        binding.amountInput.setText(obligation.amountCents?.let { Formats.amount(it) }.orEmpty())
        binding.titleInput.setText(obligation.title)
        binding.payeeInput.setText(obligation.payee)
        binding.titleInput.setSelection(obligation.title.length)
    }

    /**
     * Abre o DatePicker nativo do Android com a data atual do formulário.
     */
    private fun openDatePicker() {
        val date = viewModel.state.value.date
        DatePickerDialog(
            this,
            { _, year, month, day -> viewModel.setDate(LocalDate.of(year, month + 1, day)) },
            date.year,
            date.monthValue - 1,
            date.dayOfMonth
        ).show()
    }

    /**
     * Abre o TimePicker nativo do Android no formato de 24 horas.
     */
    private fun openTimePicker() {
        val time = viewModel.state.value.time
        TimePickerDialog(
            this,
            { _, hour, minute -> viewModel.setTime(LocalTime.of(hour, minute)) },
            time.hour,
            time.minute,
            true
        ).show()
    }

    /**
     * Envia os textos digitados para validação e gravação.
     */
    private fun save() {
        viewModel.save(
            amountCents = currencyMask.cents(),
            title = binding.titleInput.text.toString(),
            payee = binding.payeeInput.text.toString()
        )
    }

    companion object {
        const val EXTRA_ID = "obligation_id"

        /**
         * Cria o intent que abre o formulário.
         *
         * @param context contexto de origem.
         * @param id identificador para edição, ou null para um registro novo.
         * @return intent explícito para esta activity.
         */
        fun intent(context: Context, id: Long? = null): Intent {
            val intent = Intent(context, FormActivity::class.java)
            if (id != null) {
                intent.putExtra(EXTRA_ID, id)
            }
            return intent
        }
    }
}
