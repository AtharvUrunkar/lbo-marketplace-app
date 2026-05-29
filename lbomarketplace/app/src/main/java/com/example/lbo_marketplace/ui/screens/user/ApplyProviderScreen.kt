package com.example.lbo_marketplace.ui.screens.user

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.lbo_marketplace.utils.getAddressFromLocation
import com.example.lbo_marketplace.utils.fetchProviderLocation

/**
 * =========================================================================================
 * 🛠️ DETAILED IMPLEMENTATION OVERVIEW: PROVIDER REGISTRATION, PROFILE PHOTO & CLOUDINARY UPLOADS
 * =========================================================================================
 * 
 * WHAT WE HAVE DONE:
 * 1. Fully supported both PDF documents and standard Images (PNG, JPG, JPEG, etc.) for the 
 *    verification document picker, adhering to Cloudinary's free-tier ingestion capabilities.
 * 2. Added a highly requested, dedicated **Profile Photo Picker** inside the registration flow.
 * 3. Enforced strict 2.0 MB size limitations on both inputs (Profile Image and Verification File)
 *    to protect physical network transmission limits and match Cloudinary efficiency schemas.
 * 4. Designed beautiful success and oversized warning cards matching the strict black-and-white 
 *    premium LBO styling.
 * 5. Provided clear filename and size metrics inside highly polished Material 3 card sheets.
 * 6. Kept proper formatting and structured indentation to ensure readability and easy debugging.
 * =========================================================================================
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyProviderScreen(
    onSubmit: (
        String, // name
        String, // service type (Saved as "Category - SubCategory")
        String, // description
        String, // experience
        Double, // latitude
        Double, // longitude
        String, // city
        String, // area
        String, // fullAddress
        Uri,    // verification document (PDF or Image)
        Uri?    // optional profile picture URI
    ) -> Unit
) {
    val context = LocalContext.current

    // =====================================================================================
    // 📝 FORM STATE FIELDS
    // =====================================================================================
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }

    // =====================================================================================
    // 🩺 CATEGORIES & SUB-CATEGORIES MAPPING MATRIX
    // =====================================================================================
    val categoryMap = remember {
        mapOf(
            "Doctor" to listOf("Cardiologist", "Dentist", "Dermatologist", "General Physician", "Gynecologist", "Orthopedic", "Pediatrician").sorted(),
            "Education & Tuition" to listOf("Coding Instructor", "Languages", "Maths Tutor", "Music Teacher", "Science Tutor").sorted(),
            "Electrician" to listOf("Appliance Repair", "Generator Service", "Inverter Setup", "Lighting Installation", "Wiring").sorted(),
            "Home Services" to listOf("AC Repair", "Carpenter", "House Cleaning", "Painter", "Pest Control").sorted(),
            "Hotel" to listOf("Banquet Hall", "Boarding & Lodging", "Budget Stay", "Cafe", "Fine Dining", "Resort").sorted(),
            "Lawyer" to listOf("Civil Lawyer", "Corporate Lawyer", "Criminal Lawyer", "Family Lawyer", "Property Lawyer", "Tax Lawyer").sorted(),
            "Plumber" to listOf("Bathroom Fitting", "Drain Cleaning", "Leak Repair", "Pipe Installation", "Water Heater Service").sorted(),
            "Police" to listOf("CID", "Cyber Crime", "Local Police Station", "Traffic Police", "Women Safety").sorted()
        ).toSortedMap()
    }

    // Selection states for dropdown interaction
    var selectedCategory by remember { mutableStateOf("") }
    var selectedSubCategory by remember { mutableStateOf("") }
    var isCategoryExpanded by remember { mutableStateOf(false) }
    var isSubCategoryExpanded by remember { mutableStateOf(false) }

    // Custom fallback text inputs when "Other" is chosen
    var customCategory by remember { mutableStateOf("") }
    var customSubCategory by remember { mutableStateOf("") }

    // =====================================================================================
    // 📍 COORDINATES & ADDRESS DATA persistence
    // =====================================================================================
    var latitude by remember { mutableStateOf(0.0) }
    var longitude by remember { mutableStateOf(0.0) }
    var city by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var fullAddress by remember { mutableStateOf("") }

    // =====================================================================================
    // 📂 DOCUMENT & PROFILE PHOTO SELECTION STATES
    // =====================================================================================
    var verificationDocUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }
    var selectedFileSizeMB by remember { mutableStateOf(0.0) }

    var profilePhotoUri by remember { mutableStateOf<Uri?>(null) }
    var profilePhotoName by remember { mutableStateOf("") }
    var profilePhotoSizeMB by remember { mutableStateOf(0.0) }

    // =====================================================================================
    // ⏳ SUBMISSION LOADING STATE
    // =====================================================================================
    var isSubmitting by remember { mutableStateOf(false) }

    // =====================================================================================
    // 📸 CONTRACT REGISTER FOR PDF & IMAGE DOCUMENT SELECTION
    // =====================================================================================
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val mimeType = context.contentResolver.getType(uri)
            // Accepts PDF and standard images (PNG, JPG, JPEG, WEBP)
            if (mimeType != "application/pdf" && mimeType?.startsWith("image/") != true) {
                Toast.makeText(context, "Only PDF and standard image files are allowed", Toast.LENGTH_SHORT).show()
                return@rememberLauncherForActivityResult
            }
            
            try {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use { c ->
                    if (c.moveToFirst()) {
                        val nameIdx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        val sizeIdx = c.getColumnIndex(android.provider.OpenableColumns.SIZE)
                        selectedFileName = if (nameIdx >= 0) c.getString(nameIdx) else "document.pdf"
                        val sizeBytes = if (sizeIdx >= 0) c.getLong(sizeIdx) else 0L
                        selectedFileSizeMB = sizeBytes.toDouble() / (1024.0 * 1024.0)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                selectedFileName = "document.pdf"
                selectedFileSizeMB = 0.0
            }
            
            verificationDocUri = uri
        }
    }

    // =====================================================================================
    // 📸 CONTRACT REGISTER FOR PROFILE IMAGE SELECTION
    // =====================================================================================
    val profilePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val mimeType = context.contentResolver.getType(uri)
            if (mimeType?.startsWith("image/") != true) {
                Toast.makeText(context, "Only image files allowed for profile photo", Toast.LENGTH_SHORT).show()
                return@rememberLauncherForActivityResult
            }
            
            try {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use { c ->
                    if (c.moveToFirst()) {
                        val nameIdx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        val sizeIdx = c.getColumnIndex(android.provider.OpenableColumns.SIZE)
                        profilePhotoName = if (nameIdx >= 0) c.getString(nameIdx) else "profile.png"
                        val sizeBytes = if (sizeIdx >= 0) c.getLong(sizeIdx) else 0L
                        profilePhotoSizeMB = sizeBytes.toDouble() / (1024.0 * 1024.0)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                profilePhotoName = "profile.png"
                profilePhotoSizeMB = 0.0
            }
            
            profilePhotoUri = uri
        }
    }

    // =====================================================================================
    // 📍 GEOLOCATION RESOLUTION HANDLER
    // =====================================================================================
    fun resolveLocation(lat: Double, lng: Double) {
        latitude = lat
        longitude = lng
        val locationData = getAddressFromLocation(context, lat, lng)
        city = locationData.city
        area = locationData.area
        fullAddress = locationData.fullAddress
    }

    // =====================================================================================
    // 🔐 LOCATION PERMISSION REQUEST contract
    // =====================================================================================
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fetchProviderLocation(context) { lat, lng -> resolveLocation(lat, lng) }
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // =====================================================================================
    // 🎨 UI CONTAINER
    // =====================================================================================
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color.White)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Text(
            text = "Apply as Service Provider",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ── SECTION 1: PROFILE PICTURE ──
        Text("Profile Picture (Optional)", color = Color.Black, fontWeight = FontWeight.Bold)
        Text("Maximum allowed file size: 2.0 MB", color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF0F0F0))
                    .clickable { profilePhotoLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (profilePhotoUri != null) {
                    AsyncImage(
                        model = profilePhotoUri,
                        contentDescription = "Profile Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("+", color = Color.Black, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = { profilePhotoLauncher.launch("image/*") },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Select Photo", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        profilePhotoUri?.let {
            val isProfileOversized = profilePhotoSizeMB > 2.0
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isProfileOversized) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (isProfileOversized) {
                        Text("❌ Photo too large! Max 2 MB limit", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    } else {
                        Text("✅ Profile Photo Selected (${String.format("%.2f MB", profilePhotoSizeMB)})", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── SECTION 2: VERIFICATION DOCUMENT ──
        Text("Verification Document (PDF or Images)", color = Color.Black, fontWeight = FontWeight.Bold)
        Text("Maximum allowed file size: 2.0 MB", color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { documentPickerLauncher.launch("*/*") }, 
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("Upload Verification Document", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        verificationDocUri?.let {
            val isSizeOversized = selectedFileSizeMB > 2.0
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSizeOversized) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (isSizeOversized) {
                        Text(
                            text = "❌ Selected document is too large!",
                            color = Color(0xFFD32F2F),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Name: $selectedFileName",
                            color = Color.Black,
                            fontSize = 12.sp
                        )
                        Text(
                            text = String.format("Size: %.2f MB (Exceeds 2 MB limit)", selectedFileSizeMB),
                            color = Color(0xFFC62828),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    } else {
                        Text(
                            text = "✅ Verification Document Selected",
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Name: $selectedFileName",
                            color = Color.Black,
                            fontSize = 12.sp
                        )
                        Text(
                            text = String.format("Size: %.2f MB", selectedFileSizeMB),
                            color = Color.DarkGray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        // ── INPUT FIELDS: BASIC INFO ──
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Black, focusedLabelColor = Color.Black)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // ── CATEGORY DROPDOWN SELECTOR (EXPOSED DROPDOWN BOX) ──
        Text("Select Category", fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(6.dp))
        
        ExposedDropdownMenuBox(
            expanded = isCategoryExpanded,
            onExpandedChange = { isCategoryExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedCategory.ifBlank { "Choose a Category" },
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryExpanded) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Black, focusedLabelColor = Color.Black)
            )
            ExposedDropdownMenu(
                expanded = isCategoryExpanded,
                onDismissRequest = { isCategoryExpanded = false },
                modifier = Modifier.background(Color.White).heightIn(max = 280.dp)
            ) {
                categoryMap.keys.forEach { categoryName ->
                    DropdownMenuItem(
                        text = { Text(categoryName, color = Color.Black) },
                        onClick = {
                            selectedCategory = categoryName
                            selectedSubCategory = ""
                            isCategoryExpanded = false
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text("Other (Custom Category)", color = Color.Black) },
                    onClick = {
                        selectedCategory = "Other"
                        selectedSubCategory = ""
                        isCategoryExpanded = false
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // ── SUB-CATEGORY DROPDOWN SELECTOR (DYNAMIC SUB-LIST) ──
        if (selectedCategory.isNotEmpty() && selectedCategory != "Other") {
            Text("Select Sub-Category", fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(6.dp))
            
            ExposedDropdownMenuBox(
                expanded = isSubCategoryExpanded,
                onExpandedChange = { isSubCategoryExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedSubCategory.ifBlank { "Choose a Sub-Category" },
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isSubCategoryExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Black, focusedLabelColor = Color.Black)
                )
                ExposedDropdownMenu(
                    expanded = isSubCategoryExpanded,
                    onDismissRequest = { isSubCategoryExpanded = false },
                    modifier = Modifier.background(Color.White).heightIn(max = 280.dp)
                ) {
                    val subs = categoryMap[selectedCategory] ?: emptyList()
                    subs.forEach { subName ->
                        DropdownMenuItem(
                            text = { Text(subName, color = Color.Black) },
                            onClick = {
                                selectedSubCategory = subName
                                isSubCategoryExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── DYNAMIC CUSTOM INPUT TEXTBOXES FOR OTHER SELECTION ──
        if (selectedCategory == "Other") {
            OutlinedTextField(
                value = customCategory,
                onValueChange = { customCategory = it },
                label = { Text("Enter Custom Category") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Black, focusedLabelColor = Color.Black)
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = customSubCategory,
                onValueChange = { customSubCategory = it },
                label = { Text("Enter Custom Sub-Category") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Black, focusedLabelColor = Color.Black)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = experience,
            onValueChange = { experience = it },
            label = { Text("Experience (e.g. 3 years)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Black, focusedLabelColor = Color.Black)
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Service Description") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Black, focusedLabelColor = Color.Black)
        )
        Spacer(modifier = Modifier.height(20.dp))

        // ──📍 ACCESS CURRENT PHYSICAL LOCATION BUTTON ──
        Button(
            onClick = {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    fetchProviderLocation(context) { lat: Double, lng: Double -> resolveLocation(lat, lng) }
                } else {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("Get Current Location", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))
        if (city.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("📍 City: $city", color = Color.Black, fontWeight = FontWeight.Bold)
                    Text("📍 Area: $area", color = Color.Black)
                    Text("📍 Address: $fullAddress", color = Color.DarkGray, fontSize = 12.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(30.dp))

        // ── 🚀 FORM SUBMIT & UPLOAD INITIATION BUTTON ──
        Button(
            onClick = {
                val finalCategory = if (selectedCategory == "Other") customCategory else selectedCategory
                val finalSubCategory = if (selectedCategory == "Other") customSubCategory else selectedSubCategory

                if (name.isBlank() || finalCategory.isBlank() || finalSubCategory.isBlank() || experience.isBlank()) {
                    Toast.makeText(context, "Fill all required fields", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (verificationDocUri == null) {
                    Toast.makeText(context, "Upload PDF or Image verification document", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (selectedFileSizeMB > 2.0) {
                    Toast.makeText(context, "Cannot submit: Verification document is larger than 2 MB limit", Toast.LENGTH_LONG).show()
                    return@Button
                }
                if (profilePhotoUri != null && profilePhotoSizeMB > 2.0) {
                    Toast.makeText(context, "Cannot submit: Profile photo is larger than 2 MB limit", Toast.LENGTH_LONG).show()
                    return@Button
                }
                
                isSubmitting = true
                val combinedServiceType = "$finalCategory - $finalSubCategory"
                
                onSubmit(
                    name,
                    combinedServiceType,
                    description,
                    experience,
                    latitude,
                    longitude,
                    city,
                    area,
                    fullAddress,
                    verificationDocUri!!,
                    profilePhotoUri
                )
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Text("Submit Application", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}