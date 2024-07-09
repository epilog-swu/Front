package com.epi.epilog.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.speech.RecognizerIntent
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.epi.epilog.R

class SoundRecognizerActivity : AppCompatActivity() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechRecognizerIntent: Intent

    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // 권한이 허용된 경우
            } else {
                Toast.makeText(this, "녹음 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.speech_recognize) // Make sure to define this layout

        // RECORD_AUDIO 권한 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        // SpeechRecognizer 초기화
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Toast.makeText(this@SoundRecognizerActivity, "음성을 인식할 준비가 되었습니다.", Toast.LENGTH_SHORT).show()
            }

            override fun onBeginningOfSpeech() {
                Toast.makeText(this@SoundRecognizerActivity, "음성이 인식되고 있습니다.", Toast.LENGTH_SHORT).show()
            }

            override fun onRmsChanged(rmsdB: Float) {
                // 음성의 소음 레벨이 변경될 때 호출됩니다.
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                // 음성 데이터를 수신할 때 호출됩니다.
            }

            override fun onEndOfSpeech() {
                Toast.makeText(this@SoundRecognizerActivity, "음성 인식이 끝났습니다.", Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: Int) {
                Toast.makeText(this@SoundRecognizerActivity, "음성 인식 오류: $error", Toast.LENGTH_SHORT).show()
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.isNotEmpty()) {
                    val resultText = matches[0]
                    Toast.makeText(this@SoundRecognizerActivity, "인식된 텍스트: $resultText", Toast.LENGTH_LONG).show()
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                // 부분 결과가 있을 때 호출됩니다.
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                // 이벤트가 발생할 때 호출됩니다.
            }
        })

        // SpeechRecognizer 인텐트 설정
        speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        }

        // 버튼 클릭 시 음성 인식 시작
        findViewById<Button>(R.id.startButton).setOnClickListener {
            speechRecognizer.startListening(speechRecognizerIntent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::speechRecognizer.isInitialized) {
            speechRecognizer.destroy()
        }
    }
}
