package com.example.lbo_marketplace.ui.screens.user

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.lbo_marketplace.auth.ProviderViewModel

@Composable
fun ServicesTab(
    onBookClick: (String) -> Unit,
    viewModel: ProviderViewModel = viewModel()
) {

    val providers =
        viewModel.providers

    var searchQuery by remember {
        mutableStateOf("")
    }

    LaunchedEffect(Unit) {

        viewModel.fetchProviders()
    }

    val filteredProviders =
        providers.filter {
            it.serviceType.contains(searchQuery, ignoreCase = true) ||
                    it.name.contains(searchQuery, ignoreCase = true)
        }

    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)

    ) {

        Text(

            text = "Available Providers",

            style =
                MaterialTheme
                    .typography
                    .titleLarge,

            fontWeight =
                FontWeight.Bold,

            color = Color.Black
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        OutlinedTextField(

            value = searchQuery,

            onValueChange = {
                searchQuery = it
            },

            label = {
                Text("Search service (e.g. Electrician)")
            },

            modifier =
                Modifier.fillMaxWidth(),

            colors =
                OutlinedTextFieldDefaults.colors(

                    focusedBorderColor =
                        Color.Black,

                    unfocusedBorderColor =
                        Color.LightGray,

                    focusedLabelColor =
                        Color.Black
                ),

            shape =
                RoundedCornerShape(12.dp)
        )

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        if (filteredProviders.isEmpty()) {

            Box(

                modifier =
                    Modifier.fillMaxSize(),

                contentAlignment =
                    Alignment.Center

            ) {

                Text(
                    text = "No providers found",
                    color = Color.Gray
                )
            }

        } else {

            LazyColumn(

                modifier =
                    Modifier.fillMaxSize(),

                verticalArrangement =
                    Arrangement.spacedBy(12.dp)

            ) {

                items(filteredProviders) { provider ->

                    Card(

                        modifier =
                            Modifier.fillMaxWidth(),

                        colors =
                            CardDefaults.cardColors(

                                containerColor =
                                    Color(0xFFF9F9F9)
                            ),

                        shape =
                            RoundedCornerShape(16.dp)

                    ) {

                        Column(

                            modifier =
                                Modifier.padding(16.dp)

                        ) {

                            val imageUrl =
                                provider.profileImage ?: provider.profileImageUrl

                            if (
                                !imageUrl.isNullOrBlank()
                            ) {

                                AsyncImage(

                                    model = imageUrl,

                                    contentDescription = null,

                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                            .clip(
                                                RoundedCornerShape(12.dp)
                                            ),

                                    contentScale =
                                        ContentScale.Crop
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(12.dp)
                                )
                            }

                            Text(

                                text = provider.name,

                                style =
                                    MaterialTheme
                                        .typography
                                        .titleMedium,

                                fontWeight =
                                    FontWeight.Bold,

                                color = Color.Black
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(4.dp)
                            )

                            Text(

                                text = provider.serviceType,

                                style =
                                    MaterialTheme
                                        .typography
                                        .bodyMedium,

                                color = Color.DarkGray
                            )

                            if (
                                provider.experience.isNotBlank()
                            ) {

                                Spacer(
                                    modifier =
                                        Modifier.height(4.dp)
                                )

                                Text(

                                    text =
                                        "Experience: ${provider.experience}",

                                    style =
                                        MaterialTheme
                                            .typography
                                            .bodySmall,

                                    color = Color.Gray
                                )
                            }

                            if (
                                provider.description.isNotBlank()
                            ) {

                                Spacer(
                                    modifier =
                                        Modifier.height(8.dp)
                                )

                                Text(

                                    text = provider.description,

                                    style =
                                        MaterialTheme
                                            .typography
                                            .bodySmall,

                                    color = Color.Gray
                                )
                            }

                            Spacer(
                                modifier =
                                    Modifier.height(16.dp)
                            )

                            Button(

                                onClick = {
                                    onBookClick(
                                        provider.id
                                    )
                                },

                                modifier =
                                    Modifier.fillMaxWidth(),

                                colors =
                                    ButtonDefaults.buttonColors(

                                        containerColor =
                                            Color.Black
                                    ),

                                shape =
                                    RoundedCornerShape(12.dp)

                            ) {

                                Text(
                                    "Book Now",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}