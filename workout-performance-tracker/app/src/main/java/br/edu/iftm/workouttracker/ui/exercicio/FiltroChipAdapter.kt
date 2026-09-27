package br.edu.iftm.workouttracker.ui.exercicio

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.edu.iftm.workouttracker.R
import br.edu.iftm.workouttracker.databinding.ItemFiltroChipBinding

class FiltroChipAdapter(
    private val aoClicar: (String) -> Unit
) : RecyclerView.Adapter<FiltroChipAdapter.ViewHolder>() {

    private var itens: List<String> = emptyList()
    private var selecionado: String = ""

    fun atualizar(itens: List<String>, selecionado: String) {
        this.itens = itens
        this.selecionado = selecionado
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFiltroChipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itens[position]
        holder.bind(item, item == selecionado, aoClicar)
    }

    override fun getItemCount(): Int = itens.size

    class ViewHolder(private val binding: ItemFiltroChipBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(texto: String, selecionado: Boolean, aoClicar: (String) -> Unit) {
            binding.tvChip.text = texto
            if (selecionado) {
                binding.tvChip.setBackgroundResource(R.drawable.bg_pill_selected)
                binding.tvChip.setTextColor(binding.root.context.getColor(R.color.on_fill))
            } else {
                binding.tvChip.setBackgroundResource(R.drawable.bg_pill_unselected)
                binding.tvChip.setTextColor(binding.root.context.getColor(R.color.text_secondary))
            }
            binding.tvChip.setOnClickListener { aoClicar(texto) }
        }
    }
}
