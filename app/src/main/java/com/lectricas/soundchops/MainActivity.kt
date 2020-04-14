package com.lectricas.soundchops

import android.Manifest
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.AudioTrack.Builder
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.os.PowerManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.squti.androidwaverecorder.WaveRecorder
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_main.filePathTextView
import kotlinx.android.synthetic.main.activity_main.startPlaying
import kotlinx.android.synthetic.main.activity_main.startRecording
import kotlinx.android.synthetic.main.activity_main.stopRecording
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Arrays

class MainActivity : AppCompatActivity() {

    private lateinit var rxPermissions: RxPermissions
    private lateinit var player: MediaPlayer
    private lateinit var filePath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rxPermissions = RxPermissions(this)

        filePath = externalCacheDir?.absolutePath + "/audioFile.wav"
        val waveRecorder = WaveRecorder(filePath)
        filePathTextView.text = filePath
        waveRecorder.onAmplitudeListener = {
            Log.d("MainActivity", "Amplitude : $it")
        }


        startRecording.setOnClickListener {
            rxPermissions
                .request(Manifest.permission.RECORD_AUDIO)
                .subscribe { granted: Boolean ->
                    if (granted) { // Always true pre-M
                        waveRecorder.startRecording()
                    } else { // Oups permission denied
                    }
                }
        }

        stopRecording.setOnClickListener {
            waveRecorder.stopRecording()
        }

        player = MediaPlayer().apply {
            setWakeMode(this@MainActivity, PowerManager.PARTIAL_WAKE_LOCK)
            setAudioStreamType(AudioManager.STREAM_MUSIC)
            setOnPreparedListener {
                player.start()
            }
        }

        startPlaying.setOnClickListener {
            playRecording()
        }

        val minBufSize = AudioTrack.getMinBufferSize(
            16000,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val track = Builder().setAudioFormat(
            AudioFormat.Builder()
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(16000)
                .build()
        )
            .setTransferMode(AudioTrack.MODE_STREAM)
            .setBufferSizeInBytes(minBufSize)
            .build()
        track.play()

        val input = BufferedInputStream(FileInputStream(filePath))
        val buff = ByteArray(1024)
        var read = input.read(buff)
        val file = File(externalCacheDir?.absolutePath + "/bytes1.txt")
        val outStream = FileOutputStream(file);

        Log.d("Bytes", buff.contentToString())
        val array = AudioIOUtils.decodeBytes(buff, 5, 16000 * 2, false)
        while (read > 0) {
//            Log.d("MainActivity", buff[0].toString())
            track.write(buff, 0, read)
            outStream.write(buff, 0, read)
            read = input.read(buff)
        }
        outStream.close();
    }

    private fun adjustVolume(bytes: ByteArray) {
        for(x in bytes.indices) {

        }
        val middle = bytes.size / 2
        Log.d("Short", (bytes[middle] as Short).toString())
    }

    fun playRecording() {
        player.apply {
            reset()
            setDataSource(filePath)
            prepareAsync()
        }
    }

    fun canWriteOnExternalStorage(): Boolean { // get the state of your external storage
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == state) { // if storage is mounted return true
            Log.v("sTag", "Yes, can write to external storage.")
            return true
        }
        return false
    }
}
