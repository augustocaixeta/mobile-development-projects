package br.edu.iftm.workouttracker.ui.corrida

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.edu.iftm.workouttracker.data.Corrida
import br.edu.iftm.workouttracker.databinding.ItemCorridaBinding
import br.edu.iftm.workouttracker.util.Formatadores

class CorridaAdapter : ListAdapter<Corrida, CorridaAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCorridaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemCorridaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Corrida) {
            binding.tvData.text = Formatadores.data(item.dataHora)
            binding.tvDistancia.text = "${Formatadores.numero(item.distanciaKm)} km"
            binding.tvTempo.text = Formatadores.tempo(item.tempoSegundos)
            binding.tvPace.text = Formatadores.pace(item.distanciaKm, item.tempoSegundos)
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Corrida>() {
            override fun areItemsTheSame(oldItem: Corrida, newItem: Corrida) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Corrida, newItem: Corrida) = oldItem == newItem
        }
    }
}
