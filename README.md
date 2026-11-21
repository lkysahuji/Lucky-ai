# LUCKY AI — Android starter project (Gemini free-tier test)

This starter project is configured for an iQOO Z9x 5G (6GB/128GB) running Android 16.

## Included
- Hindi speech input using Android SpeechRecognizer. It prefers on-device recognition on Android 12+ when the device reports that it is available; otherwise Android's recognition service may use the network.
- Spoken replies using Android TextToSpeech (Hindi voice availability depends on installed speech data).
- Local command handlers: tell the time, open Android Settings, launch YouTube/WhatsApp if installed, open Camera, and open the alarm UI.
- Optional online AI chat through Google's Gemini Developer API using the `gemini-3.8-flash` model. This model is listed with free-tier text input/output in Google's current pricing page; free quotas and model availability can change.

## Get a free-tier Gemini API key
1. Open https://aistudio.google.com/app/apikey
2. Sign in with your Google account.
3. Create an API key in Google AI Studio.
4. In LUCKY, paste the key into the **Gemini API key (free-tier testing)** field.
5. Keep the key private. Do not send it in chat or publish it in an APK.

Google's free tier has usage limits. Google says content sent through the free tier may be used to improve its products, so do not send private, sensitive, or confidential information. Check current pricing and quotas: https://ai.google.dev/gemini-api/docs/pricing

## Build and install
1. Install Android Studio (latest stable) and Android SDK 35.
2. Open this folder in Android Studio and let Gradle sync.
3. Build > Build Bundle(s) / APK(s) > Build APK(s).
4. Install the debug APK on your phone and grant microphone permission.

## Important limitations
- This is a starter prototype, not a complete Siri/JARVIS replacement.
- Offline commands are limited to the local commands listed above. Full offline conversational AI is not bundled.
- On-device speech recognition depends on the phone's installed speech recognition service and Hindi language pack.
- Always-on "Hey Lucky" background wake-word detection, flashlight control, WhatsApp message sending, advanced phone automation, laptop/smart-home control, conversation memory, and polished JARVIS animations are not implemented yet.
- The Gemini key is entered at runtime and kept only in the current screen memory in this prototype. This is suitable only for personal testing; a public app should call Gemini through a secure backend so the key is not exposed.
- Android will show system UI for actions that require it. The app does not bypass Android permissions or security controls.
