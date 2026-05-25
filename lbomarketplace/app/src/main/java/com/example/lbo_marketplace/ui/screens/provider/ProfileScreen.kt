package com.example.lbo_marketplace.ui.screens.provider

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.lbo_marketplace.R
import com.example.lbo_marketplace.auth.AuthViewModel
import com.example.lbo_marketplace.auth.ProviderViewModel
import com.example.lbo_marketplace.data.repository.CloudinaryRepository
import com.example.lbo_marketplace.utils.compressImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

/**
 * Provider Profile Screen.
 *
 * Mirrors the exact UI of the user ProfileTab:
 *  ✅ Circular profile photo with Cloudinary upload
 *  ✅ Share / Shareable ID-card FAB
 *  ✅ Provider-specific details card (editable)
 *  ❌ NO "Become a Service Provider" button
 *  ✅ Logout button (full black, same style)
 */
@Composable
fun ProviderProfileScreen(
    header: @Composable () -> Unit,
    authViewModel: AuthViewModel,
    providerViewModel: ProviderViewModel = viewModel()
) {
    val user = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val cloudinaryRepo = remember { CloudinaryRepository() }

    var profileImageUrl by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    var isSharing by remember { mutableStateOf(false) }

    // Edit mode
    var isEditMode by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editServiceType by remember { mutableStateOf("") }
    var editDescription by remember { mutableStateOf("") }
    var editExperience by remember { mutableStateOf("") }
    var editLat by remember { mutableStateOf("") }
    var editLng by remember { mutableStateOf("") }

    val currentProfile = providerViewModel.currentProviderProfile
    val name = currentProfile?.name ?: user?.displayName ?: "Provider"
    val email = user?.email ?: "N/A"
    val serviceType = currentProfile?.serviceType ?: ""
    val experience = currentProfile?.experience ?: ""

    // Fetch profile image + provider details
    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    profileImageUrl = doc.getString("profileImageUrl") ?: ""
                }
            providerViewModel.fetchProviderProfile(uid)
        }
    }

    // Populate edit fields when profile loads
    LaunchedEffect(currentProfile) {
        if (currentProfile != null && !isEditMode) {
            editName        = currentProfile.name
            editServiceType = currentProfile.serviceType
            editDescription = currentProfile.description
            editExperience  = currentProfile.experience
            editLat         = currentProfile.latitude.toString()
            editLng         = currentProfile.longitude.toString()
        }
    }

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    isUploading = true
                    val sizeInMB = context.contentResolver.openFileDescriptor(uri, "r")
                        ?.statSize?.toDouble()?.div(1024 * 1024) ?: 0.0
                    if (sizeInMB > 2) {
                        Toast.makeText(context, "Image must be below 2 MB", Toast.LENGTH_SHORT).show()
                        isUploading = false
                        return@launch
                    }
                    val compressedFile = compressImage(context, uri)
                    val uploadResult  = cloudinaryRepo.uploadFile(compressedFile)
                    if (uploadResult.isFailure) {
                        Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show()
                        isUploading = false
                        return@launch
                    }
                    val imageUrl = uploadResult.getOrNull() ?: ""
                    user?.uid?.let { uid ->
                        FirebaseFirestore.getInstance()
                            .collection("users").document(uid)
                            .update("profileImageUrl", imageUrl)
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

    // Share as text
    val shareProfileText = {
        val text = "🌟 Check out my LBO Provider Profile!\n\n" +
                "👤 Name: $name\n🔧 Service: $serviceType\n" +
                "⏳ Experience: $experience\n📧 Email: $email\n\n" +
                "LBO Marketplace – Together We Grow 🤝"
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(intent, "Share Provider Profile"))
    }

    // Share as image card
    val shareProfileImage = {
        isSharing = true
        try {
            val bitmap = createProviderProfileBitmap(context, name, serviceType, experience, email)
            shareProviderBitmap(context, bitmap)
        } catch (e: Exception) {
            Toast.makeText(context, "Image share failed. Using text.", Toast.LENGTH_SHORT).show()
            shareProfileText()
        } finally {
            isSharing = false
        }
    }

    // ── UI ──
    AnimatedVisibility(visible = true, enter = fadeIn() + expandVertically()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Render Header
            header()

            Spacer(modifier = Modifier.height(24.dp))

            // ── Profile photo + share FAB ──
            Box(
                contentAlignment = Alignment.TopEnd,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF8F8F8)),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        profileImageUrl.isNotEmpty() -> Image(
                            painter = rememberAsyncImagePainter(profileImageUrl),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        user?.photoUrl != null -> AsyncImage(
                            model = user.photoUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        else -> Text(
                            text = name.take(1).uppercase(),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray
                        )
                    }
                }

                // Share FAB (black, circular – same as user)
                FloatingActionButton(
                    onClick = { if (!isSharing) shareProfileImage() },
                    modifier = Modifier
                        .offset(x = 12.dp, y = (-12).dp)
                        .size(40.dp),
                    containerColor = Color.Black,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    if (isSharing)
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    else
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share Profile",
                            modifier = Modifier.size(20.dp)
                        )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Name is placed DIRECTLY under the profile photo
            Text(
                text = name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )
            if (serviceType.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = serviceType,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Set profile picture button is placed below the Name and Service Type
            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                enabled = !isUploading,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isUploading)
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                else
                    Text("Set Profile Picture", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Info rows (same style as user ProfileInfoItem) ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                ProviderProfileInfoItem(
                    icon = Icons.Default.Build,
                    label = "SERVICE TYPE",
                    value = serviceType.ifBlank { "N/A" }
                )
                Spacer(modifier = Modifier.height(24.dp))
                ProviderProfileInfoItem(
                    icon = Icons.Default.DateRange,
                    label = "EXPERIENCE",
                    value = experience.ifBlank { "N/A" }
                )
                Spacer(modifier = Modifier.height(24.dp))
                ProviderProfileInfoItem(
                    icon = Icons.Default.Email,
                    label = "EMAIL",
                    value = email
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Provider Details Card (editable) ──
            if (currentProfile != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Provider Details",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            TextButton(
                                onClick = {
                                    if (isEditMode) {
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
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color.Black)
                            ) {
                                Text(if (isEditMode) "Save" else "Edit", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (isEditMode) {
                            listOf(
                                Triple(editName,        "Name")        { v: String -> editName = v },
                                Triple(editServiceType, "Service Type") { v: String -> editServiceType = v },
                                Triple(editExperience,  "Experience")  { v: String -> editExperience = v },
                                Triple(editDescription, "Description") { v: String -> editDescription = v }
                            ).forEach { (value, label, onValueChange) ->
                                OutlinedTextField(
                                    value = value,
                                    onValueChange = onValueChange,
                                    label = { Text(label) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.Black,
                                        unfocusedBorderColor = Color.LightGray,
                                        focusedLabelColor = Color.Black
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = editLat,
                                    onValueChange = { editLat = it },
                                    label = { Text("Latitude") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.Black,
                                        unfocusedBorderColor = Color.LightGray
                                    )
                                )
                                OutlinedTextField(
                                    value = editLng,
                                    onValueChange = { editLng = it },
                                    label = { Text("Longitude") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.Black,
                                        unfocusedBorderColor = Color.LightGray
                                    )
                                )
                            }
                        } else {
                            ProviderDetailRow("Name",         currentProfile.name)
                            ProviderDetailRow("Service Type", currentProfile.serviceType)
                            ProviderDetailRow("Experience",   currentProfile.experience)
                            ProviderDetailRow("Description",  currentProfile.description)
                            ProviderDetailRow("Location",     "Lat: ${currentProfile.latitude}, Lng: ${currentProfile.longitude}")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // ── Logout (no "Become Provider" button for providers) ──
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Button(
                    onClick = { authViewModel.logout() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("Logout", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Small helper composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ProviderProfileInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = value, fontSize = 16.sp, color = Color.DarkGray)
        }
    }
}

@Composable
fun ProviderDetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Black
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Share helpers (provider-specific bitmap)
// ─────────────────────────────────────────────────────────────────────────────

private fun createProviderProfileBitmap(
    context: Context,
    name: String,
    serviceType: String,
    experience: String,
    email: String
): Bitmap {
    val width = 1080; val height = 1380
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.WHITE)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Avatar circle at y = 200f (center), radius = 150f, bottom is at 350f
    paint.color = android.graphics.Color.parseColor("#F4F4F4")
    canvas.drawCircle(width / 2f, 200f, 150f, paint)
    paint.color = android.graphics.Color.LTGRAY
    paint.textSize = 130f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    val initial = name.take(1).uppercase()
    canvas.drawText(initial, (width - paint.measureText(initial)) / 2, 245f, paint)

    // Name is drawn DIRECTLY under the avatar circle (e.g. at y = 430f)
    paint.color = android.graphics.Color.BLACK
    paint.textSize = 80f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText(name, (width - paint.measureText(name)) / 2, 430f, paint)

    // Motto / Verified text drawn under the name
    paint.color = android.graphics.Color.parseColor("#6C63FF")
    paint.textSize = 45f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    val motto = "LBO Verified Service Provider 🛠️"
    canvas.drawText(motto, (width - paint.measureText(motto)) / 2, 510f, paint)

    // Logo drawn under the motto
    val logo = try { BitmapFactory.decodeResource(context.resources, R.drawable.logo) } catch (e: Exception) { null }
    if (logo != null) {
        val s = 120; val out = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888)
        val c2 = Canvas(out); val p2 = Paint(Paint.ANTI_ALIAS_FLAG)
        c2.drawCircle(s / 2f, s / 2f, s / 2f, p2)
        p2.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        c2.drawBitmap(logo, null, Rect(0, 0, s, s), p2)
        canvas.drawBitmap(out, (width - s) / 2f, 570f, paint)
    }

    // Details box starting slightly lower
    paint.color = android.graphics.Color.parseColor("#F8F8F8")
    canvas.drawRoundRect(150f, 760f, 930f, 1180f, 40f, 40f, paint)
    paint.textSize = 40f
    paint.color = android.graphics.Color.GRAY
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("SERVICE", 200f, 830f, paint)
    paint.color = android.graphics.Color.BLACK
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText(serviceType, 200f, 880f, paint)
    
    paint.color = android.graphics.Color.GRAY
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("EXPERIENCE", 200f, 950f, paint)
    paint.color = android.graphics.Color.BLACK
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText(experience, 200f, 1000f, paint)
    
    paint.color = android.graphics.Color.GRAY
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("EMAIL", 200f, 1070f, paint)
    paint.color = android.graphics.Color.BLACK
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText(email, 200f, 1120f, paint)

    // Footer
    paint.color = android.graphics.Color.BLACK
    paint.textSize = 35f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
    val footer = "LBO Marketplace – Empowering Local Experts"
    canvas.drawText(footer, (width - paint.measureText(footer)) / 2, 1320f, paint)

    return bitmap
}

private fun shareProviderBitmap(context: Context, bitmap: Bitmap) {
    val cachePath = File(context.cacheDir, "images").also { it.mkdirs() }
    val file = File(cachePath, "lbo_provider_profile.png")
    FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        putExtra(Intent.EXTRA_STREAM, uri)
        type = "image/png"
    }
    val chooser = Intent.createChooser(intent, "Share Provider Profile")
    context.packageManager
        .queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY)
        .forEach { ri ->
            context.grantUriPermission(
                ri.activityInfo.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    context.startActivity(chooser)
}