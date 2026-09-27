package br.edu.iftm.workouttracker.ui.exercicio

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.edu.iftm.workouttracker.R
import br.edu.iftm.workouttracker.data.Exercicio
import br.edu.iftm.workouttracker.data.ExercicioResumo
import br.edu.iftm.workouttracker.databinding.ItemExercicioListaBinding
import br.edu.iftm.workouttracker.util.Formatadores

class ExercicioAdapter(
    private val aoClicar: (Exercicio) -> Unit
) : ListAdapter<ExercicioResumo, ExercicioAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExercicioListaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), aoClicar)
    }

    class ViewHolder(private val binding: ItemExercicioListaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ExercicioResumo, aoClicar: (Exercicio) -> Unit) {
            val contexto = binding.root.context
            val grupo = item.exercicio.grupoMuscular
            val ultimaCarga = item.ultimaCarga
            binding.tvNome.text = item.exercicio.nome
            binding.tvMeta.text = if (item.totalRegistros > 0 && ultimaCarga != null) {
                contexto.getString(
                    R.string.label_resumo_exercicio_format,
                    grupo,
                    contexto.resources.getQuantityString(
                        R.plurals.quantidade_treinos,
                        item.totalRegistros,
                        item.totalRegistros
                    ),
                    Formatadores.numero(ultimaCarga)
                )
            } else {
                contexto.getString(R.string.label_resumo_exercicio_vazio_format, grupo)
            }
            binding.root.setOnClickListener { aoClicar(item.exercicio) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ExercicioResumo>() {
            override fun areItemsTheSame(oldItem: ExercicioResumo, newItem: ExercicioResumo) =
                oldItem.exercicio.id == newItem.exercicio.id

            override fun areContentsTheSame(oldItem: ExercicioResumo, newItem: ExercicioResumo) =
                oldItem == newItem
        }
    }
}
