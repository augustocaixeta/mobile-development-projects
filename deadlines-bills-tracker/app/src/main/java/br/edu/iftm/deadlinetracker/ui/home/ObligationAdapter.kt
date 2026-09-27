package br.edu.iftm.deadlinetracker.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.edu.iftm.deadlinetracker.databinding.ItemObligationBinding
import br.edu.iftm.deadlinetracker.databinding.ItemSectionBinding
import br.edu.iftm.deadlinetracker.ui.components.ObligationRow

class ObligationAdapter(
    private val onClick: (Long) -> Unit
) : ListAdapter<ListItem, RecyclerView.ViewHolder>(Diff) {

    /**
     * Diferencia cabeçalhos de seção das linhas de obrigação.
     *
     * @param position posição do item na lista.
     * @return tipo de view usado pelo RecyclerView.
     */
    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is ListItem.Section -> TYPE_SECTION
        is ListItem.Entry -> TYPE_ENTRY
    }

    /**
     * Infla o layout de acordo com o tipo do item.
     *
     * @param parent lista que vai receber a view.
     * @param viewType tipo retornado por getItemViewType.
     * @return holder com o binding correspondente.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_SECTION) {
            SectionHolder(ItemSectionBinding.inflate(inflater, parent, false))
        } else {
            EntryHolder(ItemObligationBinding.inflate(inflater, parent, false))
        }
    }

    /**
     * Preenche o holder com o item da posição.
     *
     * @param holder holder reaproveitado pelo RecyclerView.
     * @param position posição do item na lista.
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ListItem.Section -> (holder as SectionHolder).binding.root.setText(item.title)
            is ListItem.Entry -> {
                val row = (holder as EntryHolder).binding
                ObligationRow.bind(row, item.obligation, item.now)
                row.root.setOnClickListener { onClick(item.obligation.id) }
            }
        }
    }

    class SectionHolder(val binding: ItemSectionBinding) : RecyclerView.ViewHolder(binding.root)

    class EntryHolder(val binding: ItemObligationBinding) : RecyclerView.ViewHolder(binding.root)

    private object Diff : DiffUtil.ItemCallback<ListItem>() {

        /**
         * Compara a identidade dos itens.
         *
         * @param oldItem item da lista anterior.
         * @param newItem item da lista nova.
         * @return true quando representam a mesma seção ou a mesma obrigação.
         */
        override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean = when {
            oldItem is ListItem.Section && newItem is ListItem.Section -> oldItem.title == newItem.title
            oldItem is ListItem.Entry && newItem is ListItem.Entry ->
                oldItem.obligation.id == newItem.obligation.id
            else -> false
        }

        /**
         * Compara o conteúdo dos itens.
         *
         * @param oldItem item da lista anterior.
         * @param newItem item da lista nova.
         * @return true quando nada mudou.
         */
        override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean = oldItem == newItem
    }

    companion object {
        private const val TYPE_SECTION = 0
        private const val TYPE_ENTRY = 1
    }
}
