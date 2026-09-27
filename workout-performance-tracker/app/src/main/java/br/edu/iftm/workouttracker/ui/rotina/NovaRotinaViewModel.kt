package br.edu.iftm.workouttracker.ui.rotina

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.iftm.workouttracker.data.RegistroCarga
import br.edu.iftm.workouttracker.data.Rotina
import br.edu.iftm.workouttracker.repository.WorkoutRepository
import br.edu.iftm.workouttracker.util.Formatadores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ResultadoValidacao {
    data object Valido : ResultadoValidacao
    data object SemExercicios : ResultadoValidacao
    data class ValoresInvalidos(val nomeExercicio: String, val posicao: Int) : ResultadoValidacao
}

class NovaRotinaViewModel(
    private val repository: WorkoutRepository,
    private val rotinaId: Long
) : ViewModel() {

    private val selecionados = MutableStateFlow<Set<Long>>(emptySet())
    private val valores = mutableMapOf<Long, ValoresCarga>()
    private var ultimosRegistros: Map<Long, RegistroCarga> = emptyMap()

    private val _rotinaOriginal = MutableStateFlow<Rotina?>(null)
    val rotinaOriginal: StateFlow<Rotina?> = _rotinaOriginal.asStateFlow()

    val itens: StateFlow<List<ItemSelecao>?> = combine(
        repository.observarExercicios(),
        selecionados
    ) { exercicios, ids ->
        exercicios.map { ItemSelecao(it, it.id in ids) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            ultimosRegistros = repository.ultimosRegistrosPorExercicio()
            if (rotinaId != 0L) {
                val rotina = repository.obterRotina(rotinaId) ?: return@launch
                rotina.exercicios.forEach { item ->
                    valores[item.exercicio.id] = ValoresCarga(
                        carga = Formatadores.numero(item.registro.cargaKg),
                        series = item.registro.series.toString(),
                        repeticoes = item.registro.repeticoes.toString()
                    )
                }
                selecionados.value = rotina.exercicios.map { it.exercicio.id }.toSet()
                _rotinaOriginal.value = rotina.rotina
            }
        }
    }

    fun valoresDe(exercicioId: Long): ValoresCarga = valores.getOrPut(exercicioId) {
        val ultimo = ultimosRegistros[exercicioId]
        if (ultimo == null) {
            ValoresCarga()
        } else {
            ValoresCarga(
                carga = Formatadores.numero(ultimo.cargaKg),
                series = ultimo.series.toString(),
                repeticoes = ultimo.repeticoes.toString()
            )
        }
    }

    fun alternar(exercicioId: Long) {
        val atual = selecionados.value
        selecionados.value = if (exercicioId in atual) atual - exercicioId else atual + exercicioId
    }

    fun salvar(nome: String, grupoMuscular: String, aoConcluir: () -> Unit): ResultadoValidacao {
        val lista = itens.value.orEmpty()
        val marcados = lista.withIndex().filter { it.value.selecionado }
        if (marcados.isEmpty()) return ResultadoValidacao.SemExercicios

        val registros = mutableListOf<RegistroCarga>()
        for ((posicao, item) in marcados) {
            val valoresItem = valoresDe(item.exercicio.id)
            val carga = valoresItem.carga.trim().replace(",", ".").toDoubleOrNull()
            val series = valoresItem.series.trim().toIntOrNull()
            val repeticoes = valoresItem.repeticoes.trim().toIntOrNull()
            if (carga == null || carga < 0 || series == null || series <= 0 || repeticoes == null || repeticoes <= 0) {
                return ResultadoValidacao.ValoresInvalidos(item.exercicio.nome, posicao)
            }
            registros.add(
                RegistroCarga(
                    rotinaId = 0,
                    exercicioId = item.exercicio.id,
                    cargaKg = carga,
                    series = series,
                    repeticoes = repeticoes
                )
            )
        }

        val rotina = _rotinaOriginal.value?.copy(nome = nome, grupoMuscular = grupoMuscular)
            ?: Rotina(nome = nome, grupoMuscular = grupoMuscular)
        viewModelScope.launch {
            repository.salvarRotina(rotina, registros)
            aoConcluir()
        }
        return ResultadoValidacao.Valido
    }

    fun excluir(aoConcluir: () -> Unit) {
        val rotina = _rotinaOriginal.value ?: return
        viewModelScope.launch {
            repository.excluirRotina(rotina)
            aoConcluir()
        }
    }
}
