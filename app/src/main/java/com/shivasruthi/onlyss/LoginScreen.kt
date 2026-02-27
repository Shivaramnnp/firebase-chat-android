/*
 * Copyright (c) 2025 Shiva Sruthi Technologies
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Shiva Sruthi Technologies ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Shiva Sruthi Technologies.
 *
 * @author Shiva Sruthi Development Team
 * @version 1.0.0
 * @since 2025
 */

package com.shivasruthi.onlyss

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shivasruthi.onlyss.ui.theme.OnlySSTheme

/**
 * Professional Login Screen Component
 *
 * A modern, elegant login interface with love-themed design elements.
 * Features secure authentication with email and password validation,
 * loading states, and comprehensive error handling.
 *
 * @param onLoginSuccess Callback function triggered on successful login attempt
 * @param isLoading Boolean flag indicating authentication in progress
 * @param loginErrorMessage Optional error message to display authentication failures
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (email: String, password: String) -> Unit,
    isLoading: Boolean,
    loginErrorMessage: String?
) {
    // State management for user input
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val performLoginAttempt = {
        onLoginSuccess(email, password)
    }

    // Professional color scheme with love theme
    val primaryPink = Color(0xFFE91E63)
    val secondaryPink = Color(0xFFF8BBD9)
    val accentRose = Color(0xFFFF69B4)
    val deepRose = Color(0xFFC2185B)
    val lightBackground = Color(0xFFFFF0F5)
    val cardBackground = Color(0xFFFFFAFD)

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            lightBackground,
            Color(0xFFFCE4EC),
            secondaryPink.copy(alpha = 0.3f)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Brand Header Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = cardBackground
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "S",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = primaryPink,
                                fontSize = 28.sp
                            )
                        )
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Brand Icon",
                            tint = accentRose,
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .size(32.dp)
                        )
                        Text(
                            text = "S",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = primaryPink,
                                fontSize = 28.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "OnlySS - Where Love Begins",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = deepRose,
                            fontWeight = FontWeight.Medium
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Authentication Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = cardBackground
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome Back",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = deepRose
                        ),
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Email Input Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it.trim() },
                        label = { Text("Email", color = deepRose) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Email,
                                contentDescription = "Email Icon",
                                tint = primaryPink
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        isError = loginErrorMessage != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryPink,
                            unfocusedBorderColor = deepRose.copy(alpha = 0.6f),
                            focusedLabelColor = primaryPink,
                            cursorColor = primaryPink
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Password Input Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password", color = deepRose) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Lock,
                                contentDescription = "Password Icon",
                                tint = primaryPink
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = loginErrorMessage != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryPink,
                            unfocusedBorderColor = deepRose.copy(alpha = 0.6f),
                            focusedLabelColor = primaryPink,
                            cursorColor = primaryPink
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Authentication Button or Loading Indicator
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = primaryPink,
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 4.dp
                        )
                    } else {
                        Button(
                            onClick = performLoginAttempt,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryPink,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 6.dp,
                                pressedElevation = 12.dp
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "Login with Love",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = "Love",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Error Message Display
                    loginErrorMessage?.let {
                        Spacer(modifier = Modifier.height(20.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFEBEE)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = it,
                                color = Color(0xFFD32F2F),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Professional Footer
            Text(
                text = "Made with love — Shiva & Sruthi \n © 2025 Shiva Sruthi Technologies All rights reserved.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = deepRose.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

// Preview Composables for Development
@Preview(showBackground = true, name = "Default Login State")
@Composable
fun LoginScreenPreview() {
    OnlySSTheme {
        LoginScreen(onLoginSuccess = { _, _ -> }, isLoading = false, loginErrorMessage = null)
    }
}

@Preview(showBackground = true, name = "Loading State")
@Composable
fun LoginScreenLoadingPreview() {
    OnlySSTheme {
        LoginScreen(onLoginSuccess = { _, _ -> }, isLoading = true, loginErrorMessage = null)
    }
}

@Preview(showBackground = true, name = "Error State")
@Composable
fun LoginScreenErrorPreview() {
    OnlySSTheme {
        LoginScreen(
            onLoginSuccess = { _, _ -> },
            isLoading = false,
            loginErrorMessage = "Authentication failed. Please verify your credentials and try again."
        )
    }
}