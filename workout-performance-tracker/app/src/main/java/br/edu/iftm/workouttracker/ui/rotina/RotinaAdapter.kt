package br.edu.iftm.workouttracker.ui.rotina

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.edu.iftm.workouttracker.R
import br.edu.iftm.workouttracker.data.RotinaComExercicios
import br.edu.iftm.workouttracker.databinding.ItemRotinaBinding
import br.edu.iftm.workouttracker.util.Formatadores

class RotinaAdapter(
    private val aoClicar: (RotinaComExercicios) -> Unit
) : ListAdapter<RotinaComExercicios, RotinaAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRotinaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), aoClicar)
    }

    class ViewHolder(private val binding: ItemRotinaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RotinaComExercicios, aoClicar: (RotinaComExercicios) -> Unit) {
            val contexto = binding.root.context
            val quantidade = item.exercicios.size
            binding.tvData.text = Formatadores.data(item.rotina.realizadoEm)
            binding.tvNome.text = item.rotina.nome
            binding.tvResumo.text = contexto.getString(
                R.string.label_resumo_rotina_format,
                item.rotina.grupoMuscular,
                contexto.resources.getQuantityString(R.plurals.quantidade_exercicios, quantidade, quantidade)
            )
            binding.root.setOnClickListener { aoClicar(item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<RotinaComExercicios>() {
            override fun areItemsTheSame(oldItem: RotinaComExercicios, newItem: RotinaComExercicios) =
                oldItem.rotina.id == newItem.rotina.id

            override fun areContentsTheSame(oldItem: RotinaComExercicios, newItem: RotinaComExercicios) =
                oldItem == newItem
        }
    }
}
