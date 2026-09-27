package br.edu.iftm.workouttracker.ui.exercicio

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.edu.iftm.workouttracker.R
import br.edu.iftm.workouttracker.data.RegistroComRotina
import br.edu.iftm.workouttracker.databinding.ItemRegistroCargaBinding
import br.edu.iftm.workouttracker.util.Formatadores

class RegistroCargaAdapter : ListAdapter<RegistroComRotina, RegistroCargaAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRegistroCargaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemRegistroCargaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RegistroComRotina) {
            binding.tvTreino.text = item.rotina.nome
            binding.tvData.text = Formatadores.data(item.registro.dataHora)
            binding.tvCargaSeries.text = binding.root.context.getString(
                R.string.label_carga_series_format,
                Formatadores.numero(item.registro.cargaKg),
                item.registro.series,
                item.registro.repeticoes
            )
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<RegistroComRotina>() {
            override fun areItemsTheSame(oldItem: RegistroComRotina, newItem: RegistroComRotina) =
                oldItem.registro.id == newItem.registro.id

            override fun areContentsTheSame(oldItem: RegistroComRotina, newItem: RegistroComRotina) =
                oldItem == newItem
        }
    }
}
