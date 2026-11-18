package com.example.hostelmanager.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.hostelmanager.util.ImageStore
import java.io.File

@Composable
fun ImagePickerField(
    label: String,
    imagePath: String?,
    isRequired: Boolean,
    onImageCaptured: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }
    var showChooser by remember { mutableStateOf(false) }

    // Camera: writes into pendingCaptureUri, reports success
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingCaptureUri?.let { uri ->
                val savedPath = ImageStore.saveImage(context, uri)
                onImageCaptured(savedPath)
            }
        }
        pendingCaptureUri = null
    }

    // Gallery: returns the picked image's Uri
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val savedPath = ImageStore.saveImage(context, it)
            onImageCaptured(savedPath)
        }
    }

    // Camera permission
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = ImageStore.createTempCaptureUri(context)
            pendingCaptureUri = uri
            takePictureLauncher.launch(uri)
        }
    }

    fun launchCamera() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            val uri = ImageStore.createTempCaptureUri(context)
            pendingCaptureUri = uri
            takePictureLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = if (isRequired) "$label *" else "$label (optional)",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(4.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showChooser = true },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.Center
            ) {
                val path = imagePath
                if (path.isNullOrBlank()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Tap to add photo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    AsyncImage(
                        model = File(path),
                        contentDescription = label,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }

    // Chooser: Camera or Gallery
    if (showChooser) {
        AlertDialog(
            onDismissRequest = { showChooser = false },
            title = { Text(label) },
            text = {
                Column {
                    TextButton(onClick = {
                        showChooser = false
                        launchCamera()
                    }) { Text("📷  Take Photo") }

                    TextButton(onClick = {
                        showChooser = false
                        pickImageLauncher.launch("image/*")
                    }) { Text("🖼️  Choose from Gallery") }

                    TextButton(onClick = { showChooser = false }) { Text("Cancel") }
                }
            },
            confirmButton = {}
        )
    }
}