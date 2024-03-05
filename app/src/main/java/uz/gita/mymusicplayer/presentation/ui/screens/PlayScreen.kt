package uz.gita.mymusicplayer.presentation.ui.screens

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import by.kirich1409.viewbindingdelegate.viewBinding
import uz.gita.mymusicplayer.R
import uz.gita.mymusicplayer.data.ActionEnum
import uz.gita.mymusicplayer.data.MusicData
import uz.gita.mymusicplayer.databinding.ScreenPlayBinding
import uz.gita.mymusicplayer.presentation.service.MyService
import uz.gita.mymusicplayer.utils.MyAppManager
import uz.gita.mymusicplayer.utils.setChangeProgress

class PlayScreen : Fragment(R.layout.screen_play) {
    private val binding by viewBinding(ScreenPlayBinding::bind)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.buttonNext.setOnClickListener { startMyService(ActionEnum.NEXT) }
        binding.buttonPrev.setOnClickListener { startMyService(ActionEnum.PREV) }
        binding.buttonManage.setOnClickListener { startMyService(ActionEnum.MANAGE) }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    Log.d("TTT","seekbar")
                    val newPosition = progress * MyAppManager.fullTime / 100
                    MyAppManager.currentTime = newPosition
                    MyAppManager.isChanged = true
                    MyAppManager.currentTimeLiveData.postValue(newPosition)
                    binding.currentTime.text = newPosition.toString()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Not needed for this implementation
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Not needed for this implementation
            }
        })

        MyAppManager.playMusicLiveData.observe(viewLifecycleOwner, playMusicObserver)
        MyAppManager.isPlayingLiveData.observe(viewLifecycleOwner, isPlayingObserver)
        MyAppManager.currentTimeLiveData.observe(viewLifecycleOwner, currentTimeObserver)
    }

    private val playMusicObserver = Observer<MusicData> {
        binding.seekBar.progress = (MyAppManager.currentTime * 100 / MyAppManager.fullTime).toInt()
        binding.textMusicName.text = it.title
        binding.textArtistName.text = it.artist
        binding.currentTime.text = MyAppManager.currentTime.toString()
        binding.totalTime.text = it.duration.toString()
    }

    private val isPlayingObserver = Observer<Boolean> {
        if (it) binding.buttonManage.setImageResource(R.drawable.stop)
        else binding.buttonManage.setImageResource(R.drawable.play)
    }

    private val currentTimeObserver = Observer<Long> {
        binding.seekBar.progress = (MyAppManager.currentTime * 100 / MyAppManager.fullTime).toInt()
        binding.currentTime.text = MyAppManager.currentTime.toString()
    }

    private fun startMyService(action: ActionEnum) {
        val intent = Intent(requireContext(), MyService::class.java)
        intent.putExtra("COMMAND", action)
        if (Build.VERSION.SDK_INT >= 26) {
            requireActivity().startForegroundService(intent)
        } else requireActivity().startService(intent)
    }

}