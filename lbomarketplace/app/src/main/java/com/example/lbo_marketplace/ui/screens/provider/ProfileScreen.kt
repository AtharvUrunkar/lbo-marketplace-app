package com.example.lbo_marketplace.ui.screens.provider

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.lbo_marketplace.auth.AuthViewModel
import com.example.lbo_marketplace.auth.ProviderViewModel
import com.example.lbo_marketplace.data.repository.CloudinaryRepository
import com.example.lbo_marketplace.utils.compressImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    providerViewModel: ProviderViewModel = viewModel()
) {
    val user = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val cloudinaryRepo = remember { CloudinaryRepository() }

    var profileImageUrl by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }

    // Edit Mode States
    var isEditMode by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editServiceType by remember { mutableStateOf("") }
    var editDescription by remember { mutableStateOf("") }
    var editExperience by remember { mutableStateOf("") }
    var editLat by remember { mutableStateOf("") }
    var editLng by remember { mutableStateOf("") }

    val currentProfile = providerViewModel.currentProviderProfile

    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            // Fetch profile image from users collection
            FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    profileImageUrl = document.getString("profileImageUrl") ?: ""
                }
            
            // Fetch provider details
            providerViewModel.fetchProviderProfile(uid)
        }
    }

    // Populate edit fields when profile loads
    LaunchedEffect(currentProfile) {
        if (currentProfile != null && !isEditMode) {
            editName = currentProfile.name
            editServiceType = currentProfile.serviceType
            editDescription = currentProfile.description
            editExperience = currentProfile.experience
            editLat = currentProfile.latitude.toString()
            editLng = currentProfile.longitude.toString()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    isUploading = true
                    val sizeInMB = context.contentResolver.openFileDescriptor(uri, "r")?.statSize?.toDouble()?.div(1024 * 1024) ?: 0.0
                    if (sizeInMB > 2) {
                        Toast.makeText(context, "Image must be below 2 MB", Toast.LENGTH_SHORT).show()
                        isUploading = false
                        return@launch
                    }

                    val compressedFile = compressImage(context, uri)
                    val uploadResult = cloudinaryRepo.uploadFile(compressedFile)

                    if (uploadResult.isFailure) {
                        Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show()
                        isUploading = false
                        return@launch
                    }

                    val imageUrl = uploadResult.getOrNull() ?: ""

                    user?.uid?.let { uid ->
                        FirebaseFirestore.getInstance().collection("users").document(uid).update("profileImageUrl", imageUrl)
                    }

                    profileImageUrl = imageUrl
                    Toast.makeText(context, "Profile picture updated", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, e.message ?: "Error", Toast.LENGTH_SHORT).show()
                } finally {
                    isUploading = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Profile", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(24.dp))

        if (profileImageUrl.isNotEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(profileImageUrl),
                contentDescription = null,
                modifier = Modifier.size(140.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Surface(modifier = Modifier.size(140.dp), shape = CircleShape, tonalElevation = 4.dp) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No Image")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { imagePickerLauncher.launch("image/*") },
            enabled = !isUploading
        ) {
            if (isUploading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Set Profile Picture")
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        if (currentProfile == null) {
            CircularProgressIndicator()
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Provider Details", style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = { 
                            if (isEditMode) {
                                // Save
                                user?.uid?.let { uid ->
                                    providerViewModel.updateProviderProfile(
                                        userId = uid,
                                        name = editName,
                                        serviceType = editServiceType,
                                        description = editDescription,
                                        experience = editExperience,
                                        latitude = editLat.toDoubleOrNull() ?: 0.0,
                                        longitude = editLng.toDoubleOrNull() ?: 0.0
                                    ) { success, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        if (success) isEditMode = false
                                    }
                                }
                            } else {
                                isEditMode = true 
                            }
                        }) {
                            Text(if (isEditMode) "Save" else "Edit")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isEditMode) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editServiceType,
                            onValueChange = { editServiceType = it },
                            label = { Text("Service Type") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editExperience,
                            onValueChange = { editExperience = it },
                            label = { Text("Experience") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editDescription,
                            onValueChange = { editDescription = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = editLat,
                                onValueChange = { editLat = it },
                                label = { Text("Latitude") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = editLng,
                                onValueChange = { editLng = it },
                                label = { Text("Longitude") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    } else {
                        DetailRow("Name", currentProfile.name)
                        DetailRow("Service Type", currentProfile.serviceType)
                        DetailRow("Experience", currentProfile.experience)
                        DetailRow("Description", currentProfile.description)
                        DetailRow("Location", "Lat: ${currentProfile.latitude}, Lng: ${currentProfile.longitude}")
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailRow("Email", user?.email ?: "N/A")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = { authViewModel.logout() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Logout")
        }
        
        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}