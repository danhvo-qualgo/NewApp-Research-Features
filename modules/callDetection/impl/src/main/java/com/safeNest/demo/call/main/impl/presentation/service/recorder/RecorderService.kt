package com.safeNest.demo.call.main.impl.presentation.service.recorder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.io.File

class RecorderService : Service() {

    companion object {
        private const val ACTION_STOP = "ACTION_STOP"
        private const val CHANNEL_ID = "call_record_channel"
        private const val CHANNEL_NAME = "Call Recording"
        private const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, RecorderService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
//            val intent = Intent(context, RecorderService::class.java)
//            context.stopService(intent)
            Log.d("stopRecording", "stopRecording is trigger")
            val intent = Intent(context, RecorderService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private var recorder: MediaRecorder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("stopRecording", "stopRecording ${intent?.action}")
        if (intent?.action == ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        configureAudioForCall()
        startForeground(NOTIFICATION_ID, createNotification())

        startRecording()

        return START_NOT_STICKY
    }
    private fun configureAudioForCall() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.isSpeakerphoneOn = false
    }

    override fun onDestroy() {
        stopRecording()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    // ------------------------------------------------
    // Recording
    // ------------------------------------------------

    private fun startRecording() {

        Log.d("DialerCheck", "startRecording ")
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128000)
            setAudioSamplingRate(44100)
            setOutputFile(getFilePath())
            prepare()
            start()
        }
    }

    private fun stopRecording() {
        try {
            Log.d("stopRecording", "stopRecording")
            recorder?.apply {
                try {
                    stop()
                } catch (e: RuntimeException) {
                    Log.e("Recorder", "Stop failed", e)
                }
                reset()
                release()
                Log.d("stopRecording", "success")
            }
        } catch (e: Exception) {
            Log.d("stopRecording", "failure")
            e.printStackTrace()
        }
        recorder = null
    }

    // ------------------------------------------------
    // Notification
    // ------------------------------------------------

    private fun createNotification(): Notification {

        val stopIntent = Intent(this, RecorderService::class.java).apply {
            action = "STOP"
        }

        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Recording call")
            .setContentText("Call recording in progress")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_media_pause,
                "Stop",
                stopPendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Channel for call recording service"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    // ------------------------------------------------
    // File path
    // ------------------------------------------------

    private fun getFilePath(): String {
        val dir = File(getExternalFilesDir(null), "callRecordings")
        if (!dir.exists()) dir.mkdirs()

        val fileName = "call_${System.currentTimeMillis()}.mp4"
        Log.d("DialerCheck", "File path: $dir/$fileName")
        return File(dir, fileName).absolutePath
    }
}