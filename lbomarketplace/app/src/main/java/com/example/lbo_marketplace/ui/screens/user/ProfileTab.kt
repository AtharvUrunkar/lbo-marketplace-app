package com.example.lbo_marketplace.ui.screens.user

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.lbo_marketplace.R
import com.example.lbo_marketplace.auth.AuthViewModel
import com.example.lbo_marketplace.auth.AuthState
import com.example.lbo_marketplace.data.repository.CloudinaryRepository
import com.example.lbo_marketplace.utils.compressImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

/**
 * Profile Screen merging New Backend (Cloudinary) & Premium UI (Sharable ID Card).
 */
@Composable
fun ProfileTab(
    authViewModel: AuthViewModel,
    onApplyClick: () -> Unit,
    header: @Composable () -> Unit = {}
) {
    val user = FirebaseAuth.getInstance().currentUser
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val cloudinaryRepo = remember { CloudinaryRepository() }

    var isSharing by remember { mutableStateOf(false) }
    var profileImageUrl by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("") }
    var userAddress by remember { mutableStateOf("") }
    var userPhone by remember { mutableStateOf("") }
    var lastProfileUpdate by remember { mutableStateOf(0L) }
    var showEditDialog by remember { mutableStateOf(false) }

    val name = userName.ifBlank { user?.displayName?.ifBlank { null } ?: "User Name" }
    val email = user?.email ?: "user@mail.com"
    val address = userAddress.ifBlank { "No address added" }
    val contact = userPhone.ifBlank { "No phone added" }

    // 🔥 FETCH USER PROFILE IMAGE & DETAILS
    LaunchedEffect(Unit) {
        user?.uid?.let { uid ->
            FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    profileImageUrl = document.getString("profileImageUrl") ?: ""
                    userName = document.getString("name") ?: ""
                    userAddress = document.getString("address") ?: ""
                    userPhone = document.getString("phone") ?: ""
                    lastProfileUpdate = document.getLong("lastProfileUpdate") ?: 0L
                }
        }
    }

    // 🔥 IMAGE PICKER & CLOUDINARY UPLOAD
    val imagePickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
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

    // 🔥 SHARING LOGIC
    val shareProfileTextOnly = {
        val shareText = "🌟 Check out my LBO Profile!\n\n👤 Name: $name\n📍 Address: $address\n📞 Contact: $contact\n📧 Email: $email\n\nLBO Marketplace – Together We Grow 🤝"
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share Profile"))
    }

    val shareProfileImage = {
        isSharing = true
        try {
            val bitmap = createProfileBitmap(context, name, address, contact, email, profileImageUrl)
            shareBitmap(context, bitmap)
        } catch (e: Exception) {
            Toast.makeText(context, "Image sharing failed. Falling back to text.", Toast.LENGTH_LONG).show()
            shareProfileTextOnly()
        } finally {
            isSharing = false
        }
    }

    // 🔥 UI START
    AnimatedVisibility(visible = true, enter = fadeIn() + expandVertically()) {
        Column(
            modifier = Modifier.fillMaxSize().background(Color.White).verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            header()

            Spacer(modifier = Modifier.height(24.dp))

            // 1. Profile Image & Share Button
            Box(contentAlignment = Alignment.TopEnd, modifier = Modifier.padding(horizontal = 24.dp)) {
                Box(modifier = Modifier.size(140.dp).clip(CircleShape).background(Color(0xFFF8F8F8)), contentAlignment = Alignment.Center) {
                    if (profileImageUrl.isNotEmpty()) {
                        Image(painter = rememberAsyncImagePainter(profileImageUrl), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else if (user?.photoUrl != null) {
                        AsyncImage(model = user.photoUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Text(text = name.take(1).uppercase(), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = Color.LightGray)
                    }
                }
                
                FloatingActionButton(
                    onClick = { if (!isSharing) shareProfileImage() },
                    modifier = Modifier.offset(x = 12.dp, y = (-12).dp).size(40.dp),
                    containerColor = Color.Black,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    if (isSharing) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    else Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Upload Photo Button
            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                enabled = !isUploading,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Set Profile Picture", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Color.Black)

            Spacer(modifier = Modifier.height(32.dp))

            // 3. Information Section
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                    TextButton(onClick = { showEditDialog = true }) {
                        Text("Edit", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                ProfileInfoItem(icon = Icons.Default.LocationOn, label = "ADDRESS", value = address)
                Spacer(modifier = Modifier.height(24.dp))
                ProfileInfoItem(icon = Icons.Default.Phone, label = "CONTACT NO", value = contact)
                Spacer(modifier = Modifier.height(24.dp))
                ProfileInfoItem(icon = Icons.Default.Email, label = "EMAIL", value = email)
            }

            Spacer(modifier = Modifier.height(48.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                val authState = authViewModel.authState.value
                val isProvider = authState is AuthState.Authenticated && authState.role == "SERVICE_PROVIDER"

                if (isProvider) {
                    Button(
                        onClick = { authViewModel.isProviderInCustomerMode.value = false },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                    ) {
                        Text("Switch to Provider Dashboard", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onApplyClick,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                    ) {
                        Text("Become a Service Provider", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { authViewModel.logout() }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) { Text("Logout", fontWeight = FontWeight.Bold) }
            }
            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    if (showEditDialog) {
        var editNameInput by remember { mutableStateOf(userName) }
        var editPhoneInput by remember { mutableStateOf(userPhone) }
        var editAddressInput by remember { mutableStateOf(userAddress) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profile Info", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editNameInput,
                        onValueChange = { editNameInput = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Black, unfocusedBorderColor = Color.LightGray, focusedLabelColor = Color.Black)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editPhoneInput,
                        onValueChange = { editPhoneInput = it },
                        label = { Text("Contact No") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Black, unfocusedBorderColor = Color.LightGray, focusedLabelColor = Color.Black)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editAddressInput,
                        onValueChange = { editAddressInput = it },
                        label = { Text("Address") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Black, unfocusedBorderColor = Color.LightGray, focusedLabelColor = Color.Black)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Cooldown Check
                        val cooldownMs = 72 * 60 * 60 * 1000L
                        val now = System.currentTimeMillis()
                        if (now - lastProfileUpdate < cooldownMs) {
                            val remainingMs = cooldownMs - (now - lastProfileUpdate)
                            val hours = remainingMs / (1000 * 60 * 60)
                            val minutes = (remainingMs % (1000 * 60 * 60)) / (1000 * 60)
                            val tryAfterTime = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(java.util.Date(lastProfileUpdate + cooldownMs))
                            Toast.makeText(context, "Try after $tryAfterTime ($hours hrs, $minutes mins remaining)", Toast.LENGTH_LONG).show()
                            return@Button
                        }

                        user?.uid?.let { uid ->
                            val updateTime = System.currentTimeMillis()
                            val data = mapOf(
                                "name" to editNameInput,
                                "phone" to editPhoneInput,
                                "address" to editAddressInput,
                                "lastProfileUpdate" to updateTime
                            )
                            FirebaseFirestore.getInstance().collection("users").document(uid).update(data)
                                .addOnSuccessListener {
                                    // Soft-update provider_requests if this user is a provider
                                    FirebaseFirestore.getInstance().collection("provider_requests").document(uid)
                                        .update(mapOf("name" to editNameInput, "lastProfileUpdate" to updateTime))
                                        .addOnFailureListener { /* Ignore if document doesn't exist for this user */ }

                                    userName = editNameInput
                                    userPhone = editPhoneInput
                                    userAddress = editAddressInput
                                    lastProfileUpdate = updateTime
                                    showEditDialog = false
                                    Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Update failed: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }, colors = ButtonDefaults.textButtonColors(contentColor = Color.Black)) {
                    Text("Cancel")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun ProfileInfoItem(icon: ImageVector, label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = value, fontSize = 16.sp, color = Color.DarkGray)
        }
    }
}

private fun createProfileBitmap(context: Context, name: String, address: String, contact: String, email: String, profileImageUrl: String): Bitmap {
    val width = 1080
    val height = 1380
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.WHITE)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    // 1. Profile Avatar Placeholder (Circular)
    paint.color = android.graphics.Color.parseColor("#F4F4F4")
    canvas.drawCircle(width / 2f, 200f, 150f, paint)
    
    paint.color = android.graphics.Color.LTGRAY
    paint.textSize = 130f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    val initial = name.take(1).uppercase()
    canvas.drawText(initial, (width - paint.measureText(initial))/2, 245f, paint)
    
    // 2. Motto Text
    paint.color = android.graphics.Color.BLACK
    paint.textSize = 55f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    val motto = "Together We Grow 🤝"
    canvas.drawText(motto, (width - paint.measureText(motto))/2, 420f, paint)
    
    // 3. LBO Logo (Circular)
    val logo = try { BitmapFactory.decodeResource(context.resources, R.drawable.logo) } catch (e: Exception) { null }
    if (logo != null) {
        val logoSize = 120
        val circularLogo = getCircularBitmap(logo, logoSize)
        canvas.drawBitmap(circularLogo, (width - logoSize) / 2f, 470f, paint)
    }
    
    // 4. User Name
    paint.color = android.graphics.Color.BLACK
    paint.textSize = 80f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText(name, (width - paint.measureText(name))/2, 670f, paint)
    
    // 5. Details Section (Rounded Box)
    paint.color = android.graphics.Color.parseColor("#F8F8F8")
    canvas.drawRoundRect(150f, 750f, 930f, 1100f, 40f, 40f, paint)
    
    paint.textSize = 40f
    paint.color = android.graphics.Color.GRAY
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("ADDRESS", 200f, 820f, paint)
    paint.color = android.graphics.Color.BLACK
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText(address, 200f, 870f, paint)
    
    paint.color = android.graphics.Color.GRAY
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("CONTACT", 200f, 970f, paint)
    paint.color = android.graphics.Color.BLACK
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText(contact, 200f, 1020f, paint)
    
    // 6. Footer Text
    paint.color = android.graphics.Color.BLACK
    paint.textSize = 35f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
    val footer = "LBO Marketplace - Empowering Local Experts"
    canvas.drawText(footer, (width - paint.measureText(footer))/2, 1300f, paint)
    
    return bitmap
}

private fun getCircularBitmap(srcBitmap: Bitmap, size: Int): Bitmap {
    val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val rect = Rect(0, 0, size, size)
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(srcBitmap, null, rect, paint)
    return output
}

private fun shareBitmap(context: Context, bitmap: Bitmap) {
    try {
        val cachePath = File(context.cacheDir, "images")
        if (!cachePath.exists()) cachePath.mkdirs()
        val imageFile = File(cachePath, "lbo_profile.png")
        val stream = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()

        val authority = "${context.packageName}.fileprovider"
        val contentUri = FileProvider.getUriForFile(context, authority, imageFile)
        
        if (contentUri != null) {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(Intent.EXTRA_STREAM, contentUri)
                type = "image/png"
            }
            
            val chooser = Intent.createChooser(shareIntent, "Share Profile ID Card")
            val resInfoList = context.packageManager.queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                context.grantUriPermission(packageName, contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(chooser)
        }
    } catch (e: Exception) {
        throw e
    }
}