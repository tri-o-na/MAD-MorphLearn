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
    learnerType: String
): String = withContext(Dispatchers.IO){
    val apiKey = BuildConfig.GEMINI_API_KEY
    val aiModel = "gemini-2.5-flash-lite"

    val userQuestionStyle = questionStyleType(learnerType)

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
                                        "options": [<array of strings based on the style rules>],
                                        "correctIndex": <number>
                                    }
                                ]
                            }
                            
                           This is the type of question you should generate:
                           $userQuestionStyle 
                            
                           Rules:
                           1. Do NOT include markdown
                           2. Do NOT include explanations
                           3. Do Not wrap the JSON in backticks
                           4. Return ONLY the JSON object
                           5. Do NOT add or modify the JSON keys
                           6. Ensure correctIndex must match one option index
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

suspend fun generateNewQuiz(weakQuestions: List<String>, notes: String, learnerType: String): String = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.GEMINI_API_KEY
    val aiModel = "gemini-2.5-flash-lite"

    val userQuestionStyle = questionStyleType(learnerType)

    val userWeaknessContext =
        "User had attempted a previous quiz based on the same notes but had gotten the following wrong questions: ${
            weakQuestions.joinToString(", ")
        }"

    val prompt = """
        Generate a quiz based on the following material:
        $notes
        
        Context:
        $userWeaknessContext
        
        Return the result in the following JSON format:
        {
            "questions": [
                {
                    "question": "<question text>",
                    "options": [<array of strings based on the style rules>],
                    "correctIndex": <number>
                }
            ]
        }
        
        This is the type of question you should generate:
        $userQuestionStyle
        
        Rules:
        1. Do NOT include markdown
        2. Do NOT include explanations
        3. Do Not wrap the JSON in backticks
        4. Return ONLY the JSON object
        5. Do NOT add or modify the JSON keys
        6. Ensure correctIndex must match one option index
        
        
    """.trimIndent()

    val requestBodyJson = JSONObject().apply {
        val contentsArray = JSONArray().apply {
            put(JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", prompt) })
                })
            })
        }
        put("contents", contentsArray)

        put("generationConfig", JSONObject().apply {
            put("response_mime_type", "application/json")

        })
    }

    val request = Request.Builder()
        .url("https://generativelanguage.googleapis.com/v1beta/models/$aiModel:generateContent?key=$apiKey")
        .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
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

fun questionStyleType(learnerType: String): String{
    val questionStyleTypePrompt = when (learnerType) {
        "KINESTHETIC" -> """
            - FORMAT: Match Question to Answer.
            - RULE: The 'options' array MUST contain exactly ONE (1) string. 
            - RULE: ONLY PROVIDE 1 OPTION Per question. 
            - RULE: Provide only the correct answer in the options array.
            - RULE: Set 'correctIndex' to 0.
            - RULE: Ensure there is 12 generated questions
        """.trimIndent()
        "AUDITORY" -> """
            - FORMAT: MCQ Questions
            - RULE: Standard MCQ with exactly 4 options.
        """.trimIndent()
        "READ_WRITE" -> """
            - FORMAT: Fill-in-the-blank.
            - RULE: The 'question' string must contain '____'.
            - RULE: The 'options' array must contain ONLY 1 string (the correct answer).
            - RULE: Set 'correctIndex' to 0.
            - RULE: Avoid using special characters in the answer.
        """.trimIndent()
        "VISUAL" -> """
            - Format: Flash card style question.
            - RULE: Ensure the questions does not starts with "which of the following" type of question.
            - RULE: The 'options' array must contain ONLY 1 string (the correct answer).
            - RULE: Set 'correctIndex' to 0.
            - RULE: Avoid using special characters in the answer.
        """.trimIndent()
        else -> "Standard MCQ with exactly 4 options"
    }

    return questionStyleTypePrompt
}