package mad.team9.morphlearn.ai

import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import android.content.Context
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mad.team9.morphlearn.BuildConfig
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

suspend fun uploadPDFToAI(
    context: Context,
    uri: Uri,
): String = withContext(Dispatchers.IO){
    val apiKey = BuildConfig.GEMINI_API_KEY
    val aiModel = "gemini-2.5-flash-lite"

    val inputStream = context.contentResolver.openInputStream(uri)
    val pdfBytes = inputStream!!.readBytes()

    val base64pdf = Base64.encodeToString(pdfBytes, Base64.NO_WRAP)

    val json = JSONObject().apply {
        put("contents", JSONArray().put(
            JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text","""
                           Generate study notes from the provided PDF.
                           Return the result in the following JSON format:
                            {
                                "title": "<a title for the study notes>",
                                "generatedNotes": "<well-structured notes>"
                                "questions": [
                                    {
                                        "question": "<question text>",
                                        "options": ["option1", "option2", "option3", "option4"],
                                        "correctIndex": <number>
                                    }
                                ]
                            }
                            
                           Rules:
                           1. Do NOT include markdown
                           2. Do NOT include explanations
                           3. Do Not wrap the JSON in backticks
                           4. Return ONLY the JSON object
                           5. Do NOT add or modify the JSON keys
                           6. Ensure options array always has 4 items
                           7. Ensure correctIndex must match one option index
                        """)
                    })

                    put(JSONObject().apply {
                        put("inline_data", JSONObject().apply {
                            put("mime_type", "application/pdf")
                            put("data",base64pdf)
                        })
                    })
                })
            }
        ))
    }

    val request = Request.Builder()
        .url("https://generativelanguage.googleapis.com/v1beta/models/$aiModel:generateContent?key=$apiKey")
        .post(json.toString().toRequestBody("application/json".toMediaType()))
        .build()

    val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    val response = client.newCall(request).execute()
    val result = response.body?.string() ?: ""

    val parsed = JSONObject(result)
    parsed
        .getJSONArray("candidates")
        .getJSONObject(0)
        .getJSONObject("content")
        .getJSONArray("parts")
        .getJSONObject(0)
        .getString("text")
}