package il.co.or.abicook.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import il.co.or.abicook.databinding.ItemRecipeFeedBinding

class RecipeFeedAdapter(
    private val onClick: (RecipeUiItem) -> Unit
) : ListAdapter<RecipeUiItem, RecipeFeedAdapter.VH>(Diff()) {

    class VH(val b: ItemRecipeFeedBinding) : RecyclerView.ViewHolder(b.root)

    class Diff : DiffUtil.ItemCallback<RecipeUiItem>() {
        override fun areItemsTheSame(oldItem: RecipeUiItem, newItem: RecipeUiItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: RecipeUiItem, newItem: RecipeUiItem) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemRecipeFeedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.b.tvTitle.text = item.title
        holder.b.tvMeta.text = "❤ ${item.likes}  •  Total ${item.totalTime} min  •  ${item.category}"
        holder.b.root.setOnClickListener { onClick(item) }
    }
}
