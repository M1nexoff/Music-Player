package uz.gita.mymusicplayer.presentation.ui.screens

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import uz.gita.mymusicplayer.R
import uz.gita.mymusicplayer.data.ActionEnum
import uz.gita.mymusicplayer.data.MusicData
import uz.gita.mymusicplayer.databinding.ScreenMusicListBinding
import uz.gita.mymusicplayer.presentation.service.MyService
import uz.gita.mymusicplayer.presentation.ui.adapter.MyCursorAdapter
import uz.gita.mymusicplayer.utils.MyAppManager

class MusicListScreen : Fragment(R.layout.screen_music_list) {
    private val binding by viewBinding(ScreenMusicListBinding::bind)
    private val adapter = MyCursorAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.musicList.layoutManager = LinearLayoutManager(requireContext())
        binding.musicList.adapter = adapter
        adapter.cursor = MyAppManager.cursor

        binding.bottomPart.setOnClickListener {
            findNavController().navigate(R.id.action_musicListScreen_to_playScreen)
        }
        binding.buttonNextScreen.setOnClickListener { startMyService(ActionEnum.NEXT) }
        binding.buttonPrevScreen.setOnClickListener { startMyService(ActionEnum.PREV) }
        binding.buttonManageScreen.setOnClickListener { startMyService(ActionEnum.MANAGE) }
        adapter.setSelectMusicPositionListener {
            binding.bottomPart.visibility = View.VISIBLE
            MyAppManager.selectMusicPos = it
            startMyService(ActionEnum.PLAY)
        }

        MyAppManager.playMusicLiveData.observe(viewLifecycleOwner, playMusicObserver)
        MyAppManager.isPlayingLiveData.observe(viewLifecycleOwner, isPlayingObserver)
    }

    private fun startMyService(action  :ActionEnum) {
        val intent = Intent(requireContext(), MyService::class.java)
        intent.putExtra("COMMAND", action)
        if (Build.VERSION.SDK_INT >= 26) {
            requireActivity().startForegroundService(intent)
        } else requireActivity().startService(intent)
    }

    private val playMusicObserver = Observer<MusicData> { data ->
        binding.apply {
            textMusicNameScreen.text= data.title
            textArtistNameScreen.text = data.artist
        }
    }
    private val isPlayingObserver = Observer<Boolean> {
        if (it) binding.buttonManageScreen.setImageResource(R.drawable.stop)
        else binding.buttonManageScreen.setImageResource(R.drawable.play)
    }
}
