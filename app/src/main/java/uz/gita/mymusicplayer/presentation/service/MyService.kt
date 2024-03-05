package uz.gita.mymusicplayer.presentation.service

import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import uz.gita.mymusicplayer.R
import uz.gita.mymusicplayer.data.ActionEnum
import uz.gita.mymusicplayer.data.MusicData
import uz.gita.mymusicplayer.utils.MyAppManager
import uz.gita.mymusicplayer.utils.getMusicDataByPosition
import java.io.File

class MyService : Service() {
    override fun onBind(p0: Intent?): IBinder? = null
    private val CHANNEL = "DEMO"
    private var _mediaPlayer: MediaPlayer? = null
    private val mediaPlayer get() = _mediaPlayer!!
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var job : Job? =null

    override fun onCreate() {
        super.onCreate()
        _mediaPlayer = MediaPlayer()
        createChannel()
        startMyService()
    }

    fun startMyService() {
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Music player")
            .setCustomContentView(createRemoteView())
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .build()
        startForeground(1, notification)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel("DEMO", CHANNEL, NotificationManager.IMPORTANCE_DEFAULT)
            channel.setSound(null, null)
            val service = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(channel)
        }
    }

    private fun createRemoteView(): RemoteViews {
        val view = RemoteViews(this.packageName, R.layout.remote_view)
        val musicData = MyAppManager.cursor?.getMusicDataByPosition(MyAppManager.selectMusicPos)!!
        view.setTextViewText(R.id.textMusicName, musicData.title)
        view.setTextViewText(R.id.textArtistName, musicData.artist)
        if (mediaPlayer.isPlaying) {
            view.setImageViewResource(R.id.buttonManage, R.drawable.stop_dark)
        } else view.setImageViewResource(R.id.buttonManage, R.drawable.play_dark)

        view.setOnClickPendingIntent(R.id.buttonPrev, createPendingIntent(ActionEnum.PREV))
        view.setOnClickPendingIntent(R.id.buttonManage, createPendingIntent(ActionEnum.MANAGE))
        view.setOnClickPendingIntent(R.id.buttonNext, createPendingIntent(ActionEnum.NEXT))
        view.setOnClickPendingIntent(R.id.buttonCancel, createPendingIntent(ActionEnum.CANCEL))
        return view
    }

    private fun createPendingIntent(action : ActionEnum) : PendingIntent {
        val intent = Intent(this, MyService::class.java)
        intent.putExtra("COMMAND", action)
        return PendingIntent.getService(this, action.pos, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startMyService()
        val command = intent!!.extras?.getSerializable("COMMAND") as ActionEnum
        doneCommand(command)
        return START_NOT_STICKY
    }

    private fun doneCommand(command: ActionEnum, ) {
        val data : MusicData = MyAppManager.cursor?.getMusicDataByPosition(MyAppManager.selectMusicPos)!!
        when (command) {
            ActionEnum.MANAGE -> {
                if (mediaPlayer.isPlaying) doneCommand(ActionEnum.PAUSE)
                else doneCommand(ActionEnum.PLAY)
            }

            ActionEnum.PLAY -> {
                if (mediaPlayer.isPlaying)
                    mediaPlayer.stop()
                _mediaPlayer = MediaPlayer.create(this, Uri.fromFile(File(data.data ?:"")))
                mediaPlayer.start()
                mediaPlayer.setOnCompletionListener { doneCommand(ActionEnum.NEXT) }
                MyAppManager.fullTime = data.duration
                mediaPlayer.seekTo(MyAppManager.currentTime.toInt())
                job?.let { it.cancel() }
                job = scope.launch {
                    changeProgress().collectLatest {
                    }
                }

                MyAppManager.isPlayingLiveData.value = true
                MyAppManager.playMusicLiveData.value = data
                startMyService()
            }

            ActionEnum.PAUSE -> {
                mediaPlayer.stop()
                job?.let { it.cancel() }
                MyAppManager.isPlayingLiveData.value = false
                startMyService()
            }

            ActionEnum.NEXT -> {
                MyAppManager.currentTime = 0
                if (MyAppManager.selectMusicPos +1 == MyAppManager.cursor!!.count) MyAppManager.selectMusicPos = 0
                else MyAppManager.selectMusicPos ++
                doneCommand(ActionEnum.PLAY)
            }

            ActionEnum.PREV -> {
                MyAppManager.currentTime = 0
                if (MyAppManager.selectMusicPos == 0) MyAppManager.selectMusicPos = MyAppManager.cursor!!.count-1
                else MyAppManager.selectMusicPos --
                doneCommand(ActionEnum.PLAY)
            }

            ActionEnum.CANCEL -> {
                mediaPlayer.stop()
                stopSelf()
            }
        }
    }

    private fun changeProgress() : Flow<Long> = flow {
        for (i in MyAppManager.currentTime until MyAppManager.fullTime step 1000){
            delay(1000)
            if (MyAppManager.isChanged){
                MyAppManager.currentTime += 1000
                emit(MyAppManager.currentTime)
                MyAppManager.currentTimeLiveData.postValue(MyAppManager.currentTime)
                mediaPlayer.seekTo(MyAppManager.currentTime.toInt())
            }
            emit(i)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }
}
