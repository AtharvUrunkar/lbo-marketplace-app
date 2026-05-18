package com.example.lbo_marketplace.ui.screens.user

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.lbo_marketplace.R
import com.example.lbo_marketplace.auth.AuthViewModel
import com.example.lbo_marketplace.data.repository.CloudinaryRepository
import com.example.lbo_marketplace.utils.compressImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

import java.io.File
import java.io.FileOutputStream

/**
 * Profile Screen with finalized, ultra-tight Sharable ID Card layout.
 */
@Composable
fun ProfileTab(
    authViewModel: AuthViewModel,
    onApplyClick: () -> Unit,
    header: @Composable () -> Unit = {}
) {

    // =========================================================
    // 🔥 USER
    // =========================================================
    val user = FirebaseAuth.getInstance().currentUser

    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    val cloudinaryRepo = remember {
        CloudinaryRepository()
    }

    // =========================================================
    // 🔥 STATES
    // =========================================================

    var profileImageUrl by remember {
        mutableStateOf("")
    }

    var isUploading by remember {
        mutableStateOf(false)
    }

    // =========================================================
    // 🔥 FETCH USER PROFILE IMAGE
    // =========================================================

    LaunchedEffect(Unit) {

        user?.uid?.let { uid ->

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->

                    profileImageUrl =
                        document.getString(
                            "profileImageUrl"
                        ) ?: ""
                }
        }
    }

    // =========================================================
    // 🔥 IMAGE PICKER
    // =========================================================

    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->

            uri?.let {

                scope.launch {

                    try {

                        isUploading = true

                        // =====================================
                        // 🔥 FILE SIZE CHECK
                        // =====================================

                        val sizeInMB =
                            context.contentResolver
                                .openFileDescriptor(
                                    uri,
                                    "r"
                                )
                                ?.statSize
                                ?.toDouble()
                                ?.div(1024 * 1024)
                                ?: 0.0

                        if (sizeInMB > 2) {

                            Toast.makeText(
                                context,
                                "Image must be below 2 MB",
                                Toast.LENGTH_SHORT
                            ).show()

                            isUploading = false

                            return@launch
                        }

                        // =====================================
                        // 🔥 COMPRESS IMAGE
                        // =====================================

                        val compressedFile =
                            compressImage(
                                context,
                                uri
                            )

                        // =====================================
                        // 🔥 UPLOAD TO CLOUDINARY
                        // =====================================

                        val uploadResult =
                            cloudinaryRepo.uploadFile(
                                compressedFile
                            )

                        if (uploadResult.isFailure) {

                            Toast.makeText(
                                context,
                                "Upload failed",
                                Toast.LENGTH_SHORT
                            ).show()

                            isUploading = false

                            return@launch
                        }

                        val imageUrl =
                            uploadResult.getOrNull() ?: ""

                        // =====================================
                        // 🔥 SAVE TO FIRESTORE
                        // =====================================

                        user?.uid?.let { uid ->

                            FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(uid)
                                .update(
                                    "profileImageUrl",
                                    imageUrl
                                )
                        }

                        profileImageUrl = imageUrl

                        Toast.makeText(
                            context,
                            "Profile picture updated",
                            Toast.LENGTH_SHORT
                        ).show()

                    } catch (e: Exception) {

                        Toast.makeText(
                            context,
                            e.message ?: "Error",
                            Toast.LENGTH_SHORT
                        ).show()

                    } finally {

                        isUploading = false
                    }
                }
            }
        }

    // =========================================================
    // 🔥 UI
    // =========================================================

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // =====================================================
        // 🔥 TITLE
        // =====================================================

        Text(
            text = "Profile",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        // =====================================================
        // 🔥 PROFILE IMAGE
        // =====================================================

        if (profileImageUrl.isNotEmpty()) {

            Image(
                painter = rememberAsyncImagePainter(
                    profileImageUrl
                ),
                contentDescription = null,
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

        } else {

            Surface(
                modifier = Modifier.size(140.dp),
                shape = CircleShape,
                tonalElevation = 4.dp
            ) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {

                    Text("No Image")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // =====================================================
        // 🔥 SET PROFILE PHOTO
        // =====================================================

        Button(
            onClick = {
                imagePickerLauncher.launch("image/*")
            },
            enabled = !isUploading
        ) {

            if (isUploading) {

                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )

            } else {

                Text("Set Profile Picture")
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // =====================================================
        // 🔥 USER INFO CARD
        // =====================================================

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "Account Information",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Email",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = user?.email ?: "N/A"
                )
            }
        }

            Spacer(modifier = Modifier.height(24.dp))

        // =====================================================
        // 🔥 BECOME PROVIDER BUTTON
        // =====================================================

        Button(
            onClick = {
                onApplyClick()
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Become a Service Provider")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // =====================================================
        // 🔥 LOGOUT BUTTON
        // =====================================================

        OutlinedButton(
            onClick = {
                authViewModel.logout()
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Logout")
        }
    }
}