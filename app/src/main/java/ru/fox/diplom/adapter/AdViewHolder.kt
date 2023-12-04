package ru.fox.diplom.adapter

import androidx.recyclerview.widget.RecyclerView
import ru.fox.diplom.BuildConfig
import ru.fox.diplom.databinding.CardAdBinding
import ru.fox.diplom.dto.Ad
import ru.fox.diplom.util.load

class AdViewHolder(
    private val binding: CardAdBinding,
    private val onInteractionListener: OnInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(ad: Ad) {
        binding.apply {
            image.load("${BuildConfig.BASE_URL}/media/${ad.image}")
            image.setOnClickListener {
                onInteractionListener.onAdClick(ad)
            }
        }
    }
}