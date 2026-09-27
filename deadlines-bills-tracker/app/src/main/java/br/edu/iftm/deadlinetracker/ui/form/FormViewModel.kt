package br.edu.iftm.deadlinetracker.ui.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.edu.iftm.deadlinetracker.DeadlineTrackerApp
import br.edu.iftm.deadlinetracker.data.Obligation
import br.edu.iftm.deadlinetracker.data.ObligationRepository
import br.edu.iftm.deadlinetracker.data.ObligationType
import br.edu.iftm.deadlinetracker.data.ReminderOffset
import br.edu.iftm.deadlinetracker.util.toEpochMillis
import br.edu.iftm.deadlinetracker.util.toLocalDateTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Field {
    AMOUNT,
    TITLE,
    PAYEE
}

data class FormErrors(
    val amount: Boolean = false,
    val title: Boolean = false,
    val payee: Boolean = false
) {
    val hasAny: Boolean
        get() = amount || title || payee
}

data class FormState(
    val type: ObligationType = ObligationType.PAYABLE,
    val date: LocalDate = LocalDate.now().plusDays(1),
    val time: LocalTime = LocalTime.of(9, 0),
    val offsets: Set<ReminderOffset> = setOf(ReminderOffset.ON_DUE_DATE, ReminderOffset.ONE_DAY),
    val repeatMonthly: Boolean = false,
    val errors: FormErrors = FormErrors(),
    val saving: Boolean = false
)

sealed interface FormEvent {
    data class Loaded(val obligation: Obligation) : FormEvent
    data object Saved : FormEvent
}

class FormViewModel(
    private val repository: ObligationRepository,
    private val editingId: Long
) : ViewModel() {

    private val _state = MutableStateFlow(FormState())
    val state: StateFlow<FormState> = _state.asStateFlow()

    private val eventChannel = Channel<FormEvent>(Channel.BUFFERED)
    val events: Flow<FormEvent> = eventChannel.receiveAsFlow()

    private var original: Obligation? = null

    val isEditing: Boolean
        get() = editingId > 0

    init {
        if (isEditing) {
            load()
        }
    }

    /**
     * Troca entre fatura a pagar, a receber ou prazo. Os erros somem porque os campos obrigatórios mudam.
     *
     * @param type tipo escolhido no Segmentado.
     */
    fun selectType(type: ObligationType) {
        _state.update { it.copy(type = type, errors = FormErrors()) }
    }

    /**
     * Guarda a data escolhida no DatePicker.
     *
     * @param date nova data de vencimento.
     */
    fun setDate(date: LocalDate) {
        _state.update { it.copy(date = date) }
    }

    /**
     * Guarda o horário escolhido no TimePicker.
     *
     * @param time novo horário de vencimento.
     */
    fun setTime(time: LocalTime) {
        _state.update { it.copy(time = time) }
    }

    /**
     * Liga ou desliga uma antecedência de lembrete.
     *
     * @param offset antecedência da pílula tocada.
     * @param selected true quando a pílula ficou marcada.
     */
    fun toggleOffset(offset: ReminderOffset, selected: Boolean) {
        _state.update {
            val offsets = if (selected) it.offsets + offset else it.offsets - offset
            it.copy(offsets = offsets)
        }
    }

    /**
     * Define se a obrigação volta no mês seguinte depois de liquidada.
     *
     * @param enabled estado do interruptor.
     */
    fun setRepeatMonthly(enabled: Boolean) {
        _state.update { it.copy(repeatMonthly = enabled) }
    }

    /**
     * Remove o aviso de erro de um campo assim que o usuário volta a digitar nele.
     *
     * @param field campo editado.
     */
    fun clearError(field: Field) {
        _state.update {
            val errors = when (field) {
                Field.AMOUNT -> it.errors.copy(amount = false)
                Field.TITLE -> it.errors.copy(title = false)
                Field.PAYEE -> it.errors.copy(payee = false)
            }
            it.copy(errors = errors)
        }
    }

    /**
     * Valida os campos e grava a obrigação. Valor e favorecido só são exigidos para faturas.
     *
     * @param amountCents valor digitado em centavos.
     * @param title descrição digitada.
     * @param payee favorecido, pagador ou referência digitada.
     */
    fun save(amountCents: Long, title: String, payee: String) {
        val current = _state.value
        if (current.saving) {
            return
        }
        val isBill = current.type != ObligationType.DEADLINE
        val errors = FormErrors(
            amount = isBill && amountCents <= 0,
            title = title.isBlank(),
            payee = isBill && payee.isBlank()
        )
        if (errors.hasAny) {
            _state.update { it.copy(errors = errors) }
            return
        }
        _state.update { it.copy(errors = errors, saving = true) }

        val dueAt = LocalDateTime.of(current.date, current.time).toEpochMillis()
        val base = original ?: Obligation(
            type = current.type,
            title = title,
            payee = payee,
            amountCents = null,
            dueAt = dueAt
        )
        val obligation = base.copy(
            type = current.type,
            title = title.trim(),
            payee = payee.trim(),
            amountCents = if (isBill) amountCents else null,
            dueAt = dueAt,
            repeatMonthly = current.repeatMonthly
        )
        viewModelScope.launch {
            repository.save(obligation, current.offsets)
            eventChannel.send(FormEvent.Saved)
        }
    }

    /**
     * Carrega a obrigação em edição e envia os textos para a tela preencher os campos uma única vez.
     */
    private fun load() {
        viewModelScope.launch {
            val data = repository.findWithReminders(editingId) ?: return@launch
            original = data.obligation
            val due = data.obligation.dueAt.toLocalDateTime()
            _state.value = FormState(
                type = data.obligation.type,
                date = due.toLocalDate(),
                time = due.toLocalTime(),
                offsets = data.reminders.map { it.offset }.toSet(),
                repeatMonthly = data.obligation.repeatMonthly
            )
            eventChannel.send(FormEvent.Loaded(data.obligation))
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as DeadlineTrackerApp
                val id = createSavedStateHandle().get<Long>(FormActivity.EXTRA_ID) ?: -1L
                FormViewModel(app.repository, id)
            }
        }
    }
}
