package mad.team9.morphlearn.ai

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import mad.team9.morphlearn.login.FirebaseAuthManager
import org.json.JSONObject
import mad.team9.morphlearn.ui.theme.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIFloatingActionButton(
    navController: NavController,
    aiNotesViewModel: AINotesViewModel
) {
    var showUploadModal by remember { mutableStateOf(false) }
    var showConfigureModal by remember {mutableStateOf(false)}

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf<String?>("") }

    FloatingActionButton(
        onClick = {
            showUploadModal = true
            showConfigureModal = false
        },
        containerColor = MorphTeal,
        contentColor = Color.White
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Upload PDF to AI"
        )
    }

    if (showUploadModal) {
        ModalBottomSheet(
            onDismissRequest = {
                showUploadModal = false
                showConfigureModal = false
            },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle()}
        ) {
            if (showConfigureModal){
               AIConfigureModal(
                   fileName = fileName,
                   onBack = { showConfigureModal = false},
                   onDismiss = {
                       showUploadModal = false
                       showConfigureModal = false
                   },
                   onUploadClick = { subject, topic ->
                       showUploadModal = false
                       showConfigureModal = false

                       // Insert Upload logic
                       scope.launch {
                           aiNotesViewModel.startLoading()
                           try{
                               // Get Subject Id
                               val subjectId = aiNotesViewModel.getOrCreateSubject(subject)

                               // Get AI response
                               val responseJson = uploadPDFToAI(context,selectedUri!!, FirebaseAuthManager.getLearningStyle())

                               // Inject Subject id into response json
                               val editResponse = JSONObject(responseJson).apply {
                                   put("subjectId", subjectId)
                                   if (topic.isNotEmpty()) put("title", topic)
                               }

                               // Update viewModel
                               aiNotesViewModel.setResponse(editResponse.toString())
                               navController.navigate("ai-response-PDF")

                           } catch (e: Exception){
                               Toast.makeText(context,"Error: ${e.message}", Toast.LENGTH_LONG).show()
                           } finally {
                               aiNotesViewModel.endLoading()
                           }
                       }
                   }
               )
            }
            else {
                AIUploadPDFModal(
                    onDismiss = { showUploadModal = false},
                    onContinue = {
                        uri, name ->
                        selectedUri = uri
                        fileName = name
                        showConfigureModal = true}
                )
            }
        }
    }
}

@Composable
fun AIUploadPDFModal(
    onDismiss: () -> Unit,
    onContinue: (Uri, String?) -> Unit,
){
    val context = LocalContext.current
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember {mutableStateOf<String?>(null)}

    val pickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            selectedUri = it
            selectedFileName = getFileName(context, it)
        }
    }

    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()){
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Upload PDF", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close"
                )
            }
        }

        PDFSelectionArea(
            fileName = selectedFileName,
            onClearPDF = {
            selectedFileName= null
            selectedUri = null
            },
            onSelectPDF = {pickerLauncher.launch(arrayOf("application/pdf"))}
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {selectedUri?.let {onContinue(it,selectedFileName)}},
            enabled = selectedUri != null,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MorphTeal) // teal color
        ) {
            Text("Continue", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(20.dp))

        Text("What happens next?", fontWeight = FontWeight.Bold)
        Text("• PDF analyzed to extract key content", style = MaterialTheme.typography.bodySmall, color= Color.Gray)
        Text("• Interactive exercises based on your learning style", style = MaterialTheme.typography.bodySmall, color= Color.Gray)
    }
}

@Composable
fun AIConfigureModal(
    fileName: String?,
    onBack: () -> Unit,
    onDismiss: () -> Unit,
    onUploadClick: (subject: String, topic: String)  -> Unit,
){
    var subject by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(24.dp).fillMaxWidth()){
        Row(verticalAlignment = Alignment.CenterVertically){
            Text(
                "Categorize Content",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)

            Spacer(Modifier.weight(1f))
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp), tint = MorphTeal)
            Spacer(Modifier.width(4.dp))
            Text("Back", color = MorphTeal)
        }

        Surface(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            color = Color(0xFFF3F0FF),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFE0D9F7))
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically){
                Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF7C4DFF))
                Spacer(Modifier.width(8.dp))
                Text(fileName?:"PDF", style = MaterialTheme.typography.bodyMedium, maxLines = 1)
            }
        }

        Spacer(Modifier.height(16.dp))

        Text("Subject", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = subject,
            onValueChange = {subject = it},
            placeholder = {Text("Enter Subject Name")},
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = MorphTeal,
                unfocusedBorderColor = MorphTeal,
                cursorColor = MorphTeal
            )
        )

        Spacer(Modifier.height(16.dp))

        Text("Topic (optional)", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = topic,
            onValueChange = {topic = it},
            placeholder = {Text("Enter Note Title")},
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = MorphTeal,
                unfocusedBorderColor = MorphTeal,
                cursorColor = MorphTeal
            )
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {onUploadClick(subject,topic)},
            enabled = subject.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MorphTeal,
                disabledContentColor = Color.LightGray
            )
        ) {
            Text("Upload and Generate", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PDFSelectionArea(
    fileName: String?,
    onClearPDF: () -> Unit,
    onSelectPDF: () -> Unit
) {
    val stroke = Stroke(width = 2f, pathEffect= PathEffect.dashPathEffect(floatArrayOf(10f,10f), 0f))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .drawBehind{
                drawRoundRect(color = Color.LightGray, style = stroke, cornerRadius = CornerRadius(12.dp.toPx()))
            }
            .clickable {onSelectPDF() },
        contentAlignment = Alignment.Center
    ){
        if (fileName!=null){
            Surface(
                color = Color(0xFFF3F0FF), // Light purple background
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE0D9F7))
            ) {
                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically){
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = Color(0xFF7C4DFF),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(8.dp))

                    Text(fileName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)

                    IconButton(onClick = onClearPDF) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove file",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        } else{
            Text("Tap to select a PDF", color = Color.Gray)
        }
    }
}

fun getFileName(context: Context, uri: Uri): String? {
    var result: String? = null

    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri,null,null,null,null)
        try{
            if (cursor!=null && cursor.moveToFirst()){
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1){
                    result = cursor.getString(nameIndex)
                }
            }
        } finally {
            cursor?.close()
        }
    }

    if (result == null){
        result = "PDF"
    }

    return result
}