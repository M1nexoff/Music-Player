package uz.gita.mymusicplayer.presentation.ui.adapter

import android.database.Cursor
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import uz.gita.mymusicplayer.data.MusicData
import uz.gita.mymusicplayer.databinding.ItemMusicBinding
import uz.gita.mymusicplayer.utils.getMusicDataByPosition

class MyCursorAdapter() : RecyclerView.Adapter<MyCursorAdapter.MyCursorViewHolder>() {
    var cursor : Cursor ?= null
    private var selectMusicPositionListener : ((Int) -> Unit)?= null

    inner class MyCursorViewHolder(private val binding : ItemMusicBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                selectMusicPositionListener?.invoke(absoluteAdapterPosition)
            }
        }

        fun bind() {
            cursor?.getMusicDataByPosition(absoluteAdapterPosition)?.let {
                binding.musicName.text = it.title
                binding.author.text = it.artist
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)=
        MyCursorViewHolder(ItemMusicBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: MyCursorViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int = cursor?.count ?: 0

    fun setSelectMusicPositionListener(block : (Int) -> Unit) {
        selectMusicPositionListener = block
    }
}