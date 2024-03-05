package uz.gita.mymusicplayer.utils

import android.database.Cursor
import androidx.lifecycle.MutableLiveData
import uz.gita.mymusicplayer.data.ActionEnum
import uz.gita.mymusicplayer.data.MusicData

object MyAppManager {
    var selectMusicPos: Int = -1
    var cursor: Cursor? = null
    var lastCommand: ActionEnum = ActionEnum.PLAY

    var currentTime : Long = 0L
    var fullTime : Long = 0L

    val currentTimeLiveData = MutableLiveData<Long>()

    val playMusicLiveData = MutableLiveData<MusicData>()
    val isPlayingLiveData = MutableLiveData<Boolean>()
    var isChanged = false
}