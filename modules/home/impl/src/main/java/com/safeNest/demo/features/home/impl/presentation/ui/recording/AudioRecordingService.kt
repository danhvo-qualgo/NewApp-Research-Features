package com.safeNest.demo.features.home.impl.presentation.ui.recording

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaRecorder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.io.File

class AudioRecordingService : Service() {

    companion object {
        private const val ACTION_STOP = "ACTION_STOP"
        private const val CHANNEL_ID = "audio_record_channel"
        private const val CHANNEL_NAME = "Audio Recording"
        private const val NOTIFICATION_ID = 1435

        fun start(context: Context) {
            val intent = Intent(context, AudioRecordingService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            Log.d("AudioRecordingService", "stopRecording is trigger")
            val intent = Intent(context, AudioRecordingService::class.java).apply {
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
            Log.d("AudioRecording", "stopRecording")
            recorder?.apply {
                try {
                    stop()
                } catch (e: RuntimeException) {
                    Log.e("AudioRecording", "Stop failed", e)
                }
                reset()
                release()
                Log.d("AudioRecording", "success")
            }
        } catch (e: Exception) {
            Log.d("AudioRecording", "failure")
            e.printStackTrace()
        }
        recorder = null
    }

    // ------------------------------------------------
    // Notification
    // ------------------------------------------------

    private fun createNotification(): Notification {

        val stopIntent = Intent(this, AudioRecordingService::class.java).apply {
            action = "STOP"
        }

        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Audio recording")
            .setContentText("Recording in progress...")
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

    // ------------------------------------------------
    // File path
    // ------------------------------------------------

    private fun getFilePath(): String {
        val dir = File(getExternalFilesDir(null), "audioRecording")
        if (!dir.exists()) dir.mkdirs()

        val fileName = "audio_${System.currentTimeMillis()}.mp3"
        Log.d("AudioRecording", "File path: $dir/$fileName")
        return File(dir, fileName).absolutePath
    }
}