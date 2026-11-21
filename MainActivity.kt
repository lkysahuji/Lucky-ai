package com.luckyai.assistant

import android.Manifest
import android.app.Activity
import android.app.AlarmClock
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.concurrent.Executors

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private lateinit var status: TextView
    private lateinit var conversation: TextView
    private lateinit var input: EditText
    private lateinit var apiKey: EditText
    private lateinit var speakButton: Button
    private var tts: TextToSpeech? = null
    private var recognizer: SpeechRecognizer? = null
    private val io = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        buildUi()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 41)
        }
        status.text = "Ready • Offline commands available"
        append("LUCKY", "Hello Boss! Main LUCKY hoon. Mic dabakar command bolo ya neeche type karo.")
    }

    private fun buildUi() {
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 36, 28, 24)
            gravity = Gravity.CENTER_HORIZONTAL
            setBackgroundColor(0xFF071321.toInt())
        }
        val title = TextView(this).apply {
            text = "LUCKY AI"
            textSize = 30f
            setTextColor(0xFF65DFFF.toInt())
            gravity = Gravity.CENTER
        }
        status = TextView(this).apply {
            textSize = 13f; setTextColor(0xFF9CB8C8.toInt()); gravity = Gravity.CENTER
        }
        conversation = TextView(this).apply {
            textSize = 16f; setTextColor(0xFFEAF7FF.toInt())
            setPadding(14, 18, 14, 18)
            setBackgroundColor(0xFF102438.toInt())
        }
        val scroll = ScrollView(this).apply { addView(conversation) }
        input = EditText(this).apply {
            hint = "Type a command…"; setHintTextColor(0xFF8CA8B8.toInt())
            setTextColor(0xFFFFFFFF.toInt()); setSingleLine(true)
        }
        apiKey = EditText(this).apply {
            hint = "Gemini API key (free-tier testing)"; setHintTextColor(0xFF8CA8B8.toInt())
            setTextColor(0xFFFFFFFF.toInt()); setSingleLine(true)
            inputType = 129
        }
        val mic = Button(this).apply { text = "🎙  Speak to LUCKY"; setOnClickListener { startListening() } }
        speakButton = Button(this).apply { text = "Send"; setOnClickListener {
            val q = input.text.toString().trim()
            if (q.isNotEmpty()) { input.setText(""); handle(q) }
        } }
        val settingsButton = Button(this).apply { text = "Open Android Settings"; setOnClickListener {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        } }
        page.addView(title)
        page.addView(status)
        page.addView(space(12))
        page.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        page.addView(input, LinearLayout.LayoutParams(-1, -2))
        page.addView(mic, LinearLayout.LayoutParams(-1, -2))
        page.addView(speakButton, LinearLayout.LayoutParams(-1, -2))
        page.addView(apiKey, LinearLayout.LayoutParams(-1, -2))
        page.addView(settingsButton, LinearLayout.LayoutParams(-1, -2))
        setContentView(page)
    }

    private fun space(h: Int) = View(this).apply { layoutParams = LinearLayout.LayoutParams(1, h) }

    override fun onInit(result: Int) {
        if (result == TextToSpeech.SUCCESS) tts?.language = Locale("en", "IN")
    }

    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            reply("Is device par speech recognition service available nahi hai.")
            return
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 41)
            return
        }
        recognizer?.destroy()
        recognizer = if (Build.VERSION.SDK_INT >= 31 && SpeechRecognizer.isOnDeviceRecognitionAvailable(this))
            SpeechRecognizer.createOnDeviceSpeechRecognizer(this)
        else SpeechRecognizer.createSpeechRecognizer(this)

        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { status.text = "Listening…" }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { status.text = "Processing…" }
            override fun onError(error: Int) { status.text = "Speech error ($error). Try again." }
            override fun onResults(results: Bundle?) {
                val words = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                status.text = "Ready"
                if (!words.isNullOrBlank()) handle(words)
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
        recognizer?.startListening(intent)
    }

    private fun handle(raw: String) {
        append("You", raw)
        val q = raw.lowercase(Locale.ROOT)
        when {
            q.contains("torch") || q.contains("flashlight") || q.contains("टॉर्च") -> reply("Torch control ke liye is starter build mein device-specific permission integration abhi add karni hogi.")
            q.contains("time") || q.contains("समय") || q.contains("kitne baje") -> reply("Abhi " + java.text.SimpleDateFormat("hh:mm a", Locale.getDefault()).format(java.util.Date()) + " baj rahe hain.")
            q.contains("settings") || q.contains("सेटिंग") -> { startActivity(Intent(Settings.ACTION_SETTINGS)); reply("Opening Android Settings, Boss.") }
            q.contains("alarm") || q.contains("अलार्म") -> {
                val i = Intent(AlarmClock.ACTION_SET_ALARM).putExtra(AlarmClock.EXTRA_MESSAGE, "LUCKY reminder")
                if (i.resolveActivity(packageManager) != null) startActivity(i) else reply("Alarm app nahi mili.")
            }
            q.contains("open youtube") || q.contains("youtube kholo") -> launchPackage("com.google.android.youtube", "YouTube")
            q.contains("open whatsapp") || q.contains("whatsapp kholo") -> launchPackage("com.whatsapp", "WhatsApp")
            q.contains("open camera") || q.contains("camera kholo") -> {
                val i = Intent("android.media.action.IMAGE_CAPTURE")
                if (i.resolveActivity(packageManager) != null) startActivity(i) else reply("Camera app nahi mili.")
            }
            else -> {
                val key = apiKey.text.toString().trim()
                if (key.isNotEmpty()) askOnline(raw, key) else reply("Ye command abhi offline mode mein samajh nahi aayi. Online AI ke liye API key add karein, ya supported offline command try karein.")
            }
        }
    }

    private fun launchPackage(pkg: String, label: String) {
        val i = packageManager.getLaunchIntentForPackage(pkg)
        if (i != null) { startActivity(i); reply("Opening $label, Boss.") } else reply("$label installed nahi hai.")
    }

    private fun askOnline(question: String, key: String) {
        status.text = "Online AI thinking…"
        io.execute {
            try {
                val conn = (URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent").openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"; connectTimeout = 20000; readTimeout = 30000
                    setRequestProperty("x-goog-api-key", key)
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                }
                val body = JSONObject().put("systemInstruction",
                    JSONObject().put("parts", JSONArray().put(JSONObject().put("text",
                        "You are LUCKY, a helpful Hindi-English personal assistant. Be concise and call the user Boss."))))
                    .put("contents", JSONArray().put(JSONObject()
                        .put("role", "user")
                        .put("parts", JSONArray().put(JSONObject().put("text", question)))))
                conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
                val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
                val response = stream.bufferedReader().use { it.readText() }
                if (conn.responseCode !in 200..299) throw Exception("API error ${conn.responseCode}: $response")
                val answer = JSONObject(response).getJSONArray("candidates").getJSONObject(0)
                    .getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
                runOnUiThread { status.text = "Online mode"; reply(answer) }
                conn.disconnect()
            } catch (e: Exception) {
                runOnUiThread { status.text = "Offline / connection error"; reply("Online AI connect nahi hua: ${e.message}. Offline commands abhi bhi available hain.") }
            }
        }
    }

    private fun append(who: String, text: String) {
        conversation.append("\n$who: $text\n")
    }

    private fun reply(text: String) {
        append("LUCKY", text)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "lucky_reply")
    }

    override fun onDestroy() {
        recognizer?.destroy()
        tts?.shutdown()
        io.shutdownNow()
        super.onDestroy()
    }
}
