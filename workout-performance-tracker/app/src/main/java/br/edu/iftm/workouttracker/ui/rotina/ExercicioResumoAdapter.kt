package br.edu.iftm.workouttracker.ui.rotina

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.edu.iftm.workouttracker.R
import br.edu.iftm.workouttracker.data.RotinaExercicio
import br.edu.iftm.workouttracker.databinding.ItemExercicioResumoBinding
import br.edu.iftm.workouttracker.util.Formatadores

class ExercicioResumoAdapter : ListAdapter<RotinaExercicio, ExercicioResumoAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExercicioResumoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemExercicioResumoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RotinaExercicio) {
            binding.tvNome.text = item.exercicio.nome
            binding.tvGrupo.text = item.exercicio.grupoMuscular
            binding.tvCargaSeries.text = binding.root.context.getString(
                R.string.label_carga_series_format,
                Formatadores.numero(item.registro.cargaKg),
                item.registro.series,
                item.registro.repeticoes
            )
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<RotinaExercicio>() {
            override fun areItemsTheSame(oldItem: RotinaExercicio, newItem: RotinaExercicio) =
                oldItem.registro.id == newItem.registro.id

            override fun areContentsTheSame(oldItem: RotinaExercicio, newItem: RotinaExercicio) =
                oldItem == newItem
        }
    }
}
