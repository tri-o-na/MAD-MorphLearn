package mad.team9.morphlearn.ai

import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import android.content.Context
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.setValue
import mad.team9.morphlearn.BuildConfig
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

@Composable
fun AIUploadPDF(navController: NavController){
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }

    val apiKey = BuildConfig.GEMINI_API_KEY
    val aiModel = "gemini-2.5-flash-lite"

    val pickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri ?: return@rememberLauncherForActivityResult

            scope.launch {
                isLoading = true

                try{
                    val notes = uploadPDFToAI(context, uri, apiKey, aiModel)
                    val encoded = URLEncoder.encode(notes, "UTF-8")
                    navController.navigate("notes/$encoded")

                } catch (e: Exception) {
                    Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                }

                isLoading = false
            }
        }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = {pickerLauncher.launch(arrayOf("application/pdf"))}
            ) {
                Text("Upload PDF")
            }

            if (isLoading){
                Spacer(Modifier.height(16.dp))
                CircularProgressIndicator()
            }
        }
    }
}

suspend fun uploadPDFToAI(
    context: Context,
    uri: Uri,
    apiKey: String,
    aiModel: String,
): String = withContext(Dispatchers.IO){
    val inputStream = context.contentResolver.openInputStream(uri)
    val pdfBytes = inputStream!!.readBytes()

    val base64pdf = Base64.encodeToString(pdfBytes, Base64.NO_WRAP)

    val json = JSONObject().apply {
        put("contents", JSONArray().put(
            JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text","Generate structured study notes from this PDF.")
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