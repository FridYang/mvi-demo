package com.you.chuang.new_mvi.adapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.you.chuang.new_mvi.bean.Dynamic
import com.you.chuang.new_mvi.databinding.ItemDynamicBinding

class DynamicAdapter : RecyclerView.Adapter<DynamicAdapter.ViewHolder>() {

    private var items = listOf<Dynamic>()

    fun submitList(list: List<Dynamic>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDynamicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvContent.text = item.content
        holder.binding.tvTime.text = item.publishTime
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(val binding: ItemDynamicBinding) : RecyclerView.ViewHolder(binding.root)
}