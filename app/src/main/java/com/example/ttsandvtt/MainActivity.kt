package com.example.ttsandvtt

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ttsandvtt.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val requestCodePermission = 123
    private val permissionList = arrayOf(
        Manifest.permission.RECORD_AUDIO
    )

    private lateinit var binding: ActivityMainBinding
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tts = TextToSpeech(this@MainActivity, object : TextToSpeech.OnInitListener {
            override fun onInit(status: Int) {
                if (status == TextToSpeech.SUCCESS) {
                    Log.d("mySpeechInit", "SUCCESS ${status}")
                } else {
                    Log.d("mySpeechInit", "not SUCCESS${status}")
                }
            }

        })

        val speechRecognizerLauncher = registerForActivityResult(
            SpeechRecognizerContract()
        ) { result ->
            if (result.toString() == "null") {
                Log.d("SpeechRecognizer", "resutl is null")
            } else {
                Log.d("SpeechRecognizer", result.toString().replace("[", "").replace("]", ""))
                binding.textView.text = result.toString()
            }
        }

        binding.button.setOnClickListener {
            val text = binding.textView.text
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
        }

        binding.button2.setOnClickListener {
            if (checkRationale() == 1) {
                speechRecognizerLauncher.launch(Unit)
            } else {
                Log.d("myFileTag", "checkPermissions not allowed")
            }
        }

    }

    override fun onDestroy() {
        // Shutdown TTS
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }

    //Check Status of Activity Permission
    //1. Permission Access allowed GRANTED
    //2. Permission Access not Allowed
    //3. Permission Access not Allowed permanently DENIED
    var checkStatus: Int = 2

    //This method calling after the CheckPermissions
    fun checkRationale(): Int {
        return if (checkPermissions()) {
            1
        } else {
            checkStatus
        }
    }

    private fun checkPermissions(): Boolean {
        // Check if all required permissions are granted
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            true
        } else if (!allPermissionsGranted()) {
            // Request permissions
            ActivityCompat.requestPermissions(this, permissionList, requestCodePermission)
            false
        } else {
            true
        }
    }

    // Check if all required permissions are granted
    private fun allPermissionsGranted() = permissionList.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermissionRationale(): Boolean {
        var permissionDenied = false
        permissionList.forEachIndexed { _, permission ->
            if (shouldShowRequestPermissionRationale(permission)) {
                permissionDenied = true
            }
            Log.d("myPermissionRationale", "${shouldShowRequestPermissionRationale(permission)}")
        }
        return permissionDenied
    }

    // Handle permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCodePermission) {
            if (allPermissionsGranted()) {
                // All permissions granted
                Log.d("myRequestPer", "All permissions granted")
                checkStatus = 1
            } else {
                // Permission(s) denied
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    checkStatus = if (checkPermissionRationale()) {
                        2
                    } else {
                        3
                    }
                    Log.d("myRequestPer", "Some permissions denied ${checkPermissionRationale()}")
                }
            }
        }
    }

}