package br.edu.iftm.workouttracker.ui.rotina

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.edu.iftm.workouttracker.R
import br.edu.iftm.workouttracker.data.Exercicio
import br.edu.iftm.workouttracker.databinding.ItemExercicioSelecaoBinding

data class ItemSelecao(
    val exercicio: Exercicio,
    val selecionado: Boolean
)

class ValoresCarga(
    var carga: String = "",
    var series: String = "",
    var repeticoes: String = ""
)

class ExercicioSelecaoAdapter(
    private val aoAlternar: (Exercicio) -> Unit,
    private val valoresDe: (Long) -> ValoresCarga
) : ListAdapter<ItemSelecao, ExercicioSelecaoAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExercicioSelecaoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), aoAlternar, valoresDe)
    }

    class ViewHolder(private val binding: ItemExercicioSelecaoBinding) : RecyclerView.ViewHolder(binding.root) {

        private var valoresAtuais: ValoresCarga? = null

        init {
            observar(binding.etCarga) { valoresAtuais?.carga = it }
            observar(binding.etSeries) { valoresAtuais?.series = it }
            observar(binding.etRepeticoes) { valoresAtuais?.repeticoes = it }
        }

        fun bind(item: ItemSelecao, aoAlternar: (Exercicio) -> Unit, valoresDe: (Long) -> ValoresCarga) {
            binding.tvNome.text = item.exercicio.nome
            binding.tvGrupo.text = item.exercicio.grupoMuscular
            binding.ivSelecao.setImageResource(
                if (item.selecionado) R.drawable.ic_selecionado else R.drawable.ic_nao_selecionado
            )
            binding.cabecalho.setOnClickListener { aoAlternar(item.exercicio) }

            valoresAtuais = null
            if (item.selecionado) {
                val valores = valoresDe(item.exercicio.id)
                binding.etCarga.setText(valores.carga)
                binding.etSeries.setText(valores.series)
                binding.etRepeticoes.setText(valores.repeticoes)
                valoresAtuais = valores
                binding.grupoValores.visibility = View.VISIBLE
            } else {
                binding.grupoValores.visibility = View.GONE
            }
        }

        private fun observar(campo: EditText, aoMudar: (String) -> Unit) {
            campo.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
                override fun afterTextChanged(s: Editable?) {
                    aoMudar(s?.toString().orEmpty())
                }
            })
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ItemSelecao>() {
            override fun areItemsTheSame(oldItem: ItemSelecao, newItem: ItemSelecao) =
                oldItem.exercicio.id == newItem.exercicio.id

            override fun areContentsTheSame(oldItem: ItemSelecao, newItem: ItemSelecao) =
                oldItem == newItem
        }
    }
}
