package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserEntity
import com.example.data.local.UserRole
import com.example.ui.components.ProcurementStatusBadge
import com.example.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginRegisterScreen(
    users: List<UserEntity>,
    currentUser: UserEntity?,
    currentRole: UserRole,
    onLoginUser: (UserEntity) -> Unit,
    onLogoutUser: () -> Unit,
    onRegisterUser: (name: String, email: String, role: UserRole, department: String) -> Unit,
    onSwitchPersonaRole: (UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedAuthTab by remember { mutableIntStateOf(0) } // 0 = Login, 1 = Register

    // Email pattern regex
    val emailRegex = remember { Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$") }

    // Login Form State
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var loginEmailTouched by remember { mutableStateOf(false) }
    var loginPasswordTouched by remember { mutableStateOf(false) }
    var loginErrorMessage by remember { mutableStateOf<String?>(null) }

    // Register Form State
    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }
    var isRegPasswordVisible by remember { mutableStateOf(false) }
    var regNameTouched by remember { mutableStateOf(false) }
    var regEmailTouched by remember { mutableStateOf(false) }
    var regPasswordTouched by remember { mutableStateOf(false) }
    var regConfirmPasswordTouched by remember { mutableStateOf(false) }
    var selectedDept by remember { mutableStateOf("Software Engineering") }
    var selectedRole by remember { mutableStateOf(UserRole.EMPLOYEE) }
    var regErrorMessage by remember { mutableStateOf<String?>(null) }
    var regSuccessMessage by remember { mutableStateOf<String?>(null) }

    // Real-Time Login Form Validation Rules
    val loginEmailError: String? = when {
        !loginEmailTouched -> null
        loginEmail.isBlank() -> "Email address is required."
        !emailRegex.matches(loginEmail.trim()) -> "Please enter a valid email address (e.g. name@company.com)."
        else -> null
    }

    val loginPasswordError: String? = when {
        !loginPasswordTouched -> null
        loginPassword.isBlank() -> "Password is required."
        loginPassword.length < 6 -> "Password must be at least 6 characters."
        else -> null
    }

    val isLoginFormValid = loginEmail.isNotBlank() &&
            emailRegex.matches(loginEmail.trim()) &&
            loginPassword.length >= 6

    // Real-Time Register Form Validation Rules
    val regNameError: String? = when {
        !regNameTouched -> null
        regName.trim().isEmpty() -> "Full Name is required."
        regName.trim().length < 2 -> "Full Name must be at least 2 characters."
        else -> null
    }

    val isEmailDuplicate = remember(regEmail, users) {
        users.any { it.email.equals(regEmail.trim(), ignoreCase = true) }
    }

    val regEmailError: String? = when {
        !regEmailTouched -> null
        regEmail.trim().isEmpty() -> "Work Email Address is required."
        !emailRegex.matches(regEmail.trim()) -> "Please enter a valid work email (e.g. name@company.com)."
        isEmailDuplicate -> "An account with this email already exists in MongoDB."
        else -> null
    }

    val regPasswordError: String? = when {
        !regPasswordTouched -> null
        regPassword.isEmpty() -> "Password is required."
        regPassword.length < 6 -> "Password must be at least 6 characters."
        else -> null
    }

    val regConfirmPasswordError: String? = when {
        !regConfirmPasswordTouched -> null
        regConfirmPassword.isEmpty() -> "Please confirm your password."
        regConfirmPassword != regPassword -> "Passwords do not match."
        else -> null
    }

    val isRegFormValid = regName.trim().length >= 2 &&
            regEmail.isNotBlank() &&
            emailRegex.matches(regEmail.trim()) &&
            !isEmailDuplicate &&
            regPassword.length >= 6 &&
            regConfirmPassword.isNotEmpty() &&
            regConfirmPassword == regPassword

    val departments = listOf(
        "Software Engineering",
        "Facilities & Operations",
        "Corporate Finance",
        "Procurement Admin",
        "Central Warehouse",
        "External Vendor"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // MongoDB Cluster Banner
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF023430)),
                modifier = Modifier.fillMaxWidth().testTag("mongodb_cluster_card")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF13AA52)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = "MongoDB Database",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "MongoDB Document Store",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF13AA52))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Cluster0 Connected",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                        Text(
                            text = "Collection: smartflow.users (${users.size} Document Records)",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFA7F3D0)),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Active Auth Status Header Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth().testTag("auth_active_status_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "User Avatar",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = currentUser?.name ?: "Guest Session (${currentRole.displayName})",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = currentUser?.email ?: "Role Active: ${currentRole.displayName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        if (currentUser != null) {
                            OutlinedButton(
                                onClick = onLogoutUser,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("logout_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Logout,
                                    contentDescription = "Sign Out",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sign Out", fontSize = 12.sp)
                            }
                        }
                    }

                    if (currentUser != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Department: ${currentUser.department}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                ProcurementStatusBadge(status = "Authenticated OK")
                            }
                            Text(
                                text = "MongoDB Doc ID: ${currentUser.mongoId}",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Global Registration Success Banner if present
        if (regSuccessMessage != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("reg_success_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = Color(0xFF15803D),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = regSuccessMessage!!,
                            color = Color(0xFF15803D),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // Auth Navigation Tabs (Sign In / Register)
        item {
            TabRow(
                selectedTabIndex = selectedAuthTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("auth_tab_row")
            ) {
                Tab(
                    selected = selectedAuthTab == 0,
                    onClick = { selectedAuthTab = 0 },
                    text = { Text("Sign In", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Login, contentDescription = "Sign In") },
                    modifier = Modifier.testTag("sign_in_tab")
                )
                Tab(
                    selected = selectedAuthTab == 1,
                    onClick = { selectedAuthTab = 1 },
                    text = { Text("Create Account", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.PersonAdd, contentDescription = "Register") },
                    modifier = Modifier.testTag("register_tab")
                )
            }
        }

        if (selectedAuthTab == 0) {
            // SIGN IN TAB
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        SectionHeader(
                            title = "User Sign In",
                            subtitle = "Enter credentials or select a registered account below"
                        )

                        if (loginErrorMessage != null) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().testTag("login_error_banner")
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.ErrorOutline, contentDescription = "Error", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                    Text(
                                        text = loginErrorMessage!!,
                                        color = Color(0xFFDC2626),
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = loginEmail,
                            onValueChange = {
                                loginEmail = it
                                loginEmailTouched = true
                                loginErrorMessage = null
                            },
                            label = { Text("Work Email Address *") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                            trailingIcon = {
                                if (loginEmailTouched && loginEmailError == null && loginEmail.isNotBlank()) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Valid Email", tint = Color(0xFF13AA52))
                                } else if (loginEmailError != null) {
                                    Icon(Icons.Default.Error, contentDescription = "Invalid Email", tint = MaterialTheme.colorScheme.error)
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            isError = loginEmailError != null,
                            supportingText = {
                                if (loginEmailError != null) {
                                    Text(text = loginEmailError, color = MaterialTheme.colorScheme.error, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                } else {
                                    Text(text = "Enter your company email (e.g. sarah.j@company.com)", fontSize = 11.sp)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("login_email_input")
                        )

                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = {
                                loginPassword = it
                                loginPasswordTouched = true
                                loginErrorMessage = null
                            },
                            label = { Text("Password *") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (loginPasswordTouched && loginPasswordError == null && loginPassword.isNotEmpty()) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Valid Password", tint = Color(0xFF13AA52), modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                        Icon(
                                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle password"
                                        )
                                    }
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            isError = loginPasswordError != null,
                            supportingText = {
                                if (loginPasswordError != null) {
                                    Text(text = loginPasswordError, color = MaterialTheme.colorScheme.error, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                } else {
                                    Text(text = "Minimum 6 characters", fontSize = 11.sp)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("login_password_input")
                        )

                        Button(
                            onClick = {
                                loginEmailTouched = true
                                loginPasswordTouched = true
                                if (!isLoginFormValid) {
                                    loginErrorMessage = "Please enter a valid work email and password (minimum 6 characters)."
                                    return@Button
                                }
                                val found = users.firstOrNull { it.email.equals(loginEmail.trim(), ignoreCase = true) }
                                if (found != null) {
                                    onLoginUser(found)
                                    loginErrorMessage = null
                                } else {
                                    loginErrorMessage = "No user found matching '$loginEmail'. Try registering an account below."
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            enabled = isLoginFormValid || (!loginEmailTouched && !loginPasswordTouched),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("login_submit_button")
                        ) {
                            Icon(Icons.Default.Login, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sign In to SmartFlow", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Quick Login MongoDB Accounts List
            item {
                SectionHeader(
                    title = "Registered MongoDB User Documents (${users.size})",
                    subtitle = "Select any user to authenticate & load their active persona"
                )
            }

            items(users) { user ->
                val matchedRole = UserRole.entries.firstOrNull { it.displayName.equals(user.role, ignoreCase = true) }
                    ?: UserRole.EMPLOYEE

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLoginUser(user) }
                        .testTag("user_item_${user.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(matchedRole.badgeColorHex).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user.name.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(matchedRole.badgeColorHex)
                                )
                            }

                            Column {
                                Text(
                                    text = user.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = user.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Doc ID: ${user.mongoId}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                    color = Color(0xFF13AA52)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(matchedRole.badgeColorHex))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = user.role,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Text(
                                text = user.department,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            // REGISTER TAB
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        SectionHeader(
                            title = "Register New User Account",
                            subtitle = "Fills all credentials to create a new MongoDB document & log in"
                        )

                        if (regErrorMessage != null) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().testTag("reg_error_banner")
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.ErrorOutline, contentDescription = "Error", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                    Text(
                                        text = regErrorMessage!!,
                                        color = Color(0xFFDC2626),
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }

                        // Real-time live requirement checklist
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().testTag("reg_validation_checklist")
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Real-Time Submission Requirements:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                ValidationCheckItem(label = "Full Name (minimum 2 characters)", isMet = regName.trim().length >= 2)
                                ValidationCheckItem(label = "Valid Work Email address", isMet = regEmail.isNotBlank() && emailRegex.matches(regEmail.trim()))
                                ValidationCheckItem(label = "Email unique in MongoDB cluster", isMet = regEmail.isNotBlank() && !isEmailDuplicate)
                                ValidationCheckItem(label = "Password length >= 6 characters", isMet = regPassword.length >= 6)
                                ValidationCheckItem(label = "Passwords match", isMet = regConfirmPassword.isNotEmpty() && regConfirmPassword == regPassword)
                            }
                        }

                        OutlinedTextField(
                            value = regName,
                            onValueChange = {
                                regName = it
                                regNameTouched = true
                                regErrorMessage = null
                            },
                            label = { Text("Full Name *") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name") },
                            trailingIcon = {
                                if (regNameTouched && regNameError == null && regName.isNotBlank()) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Valid Name", tint = Color(0xFF13AA52))
                                } else if (regNameError != null) {
                                    Icon(Icons.Default.Error, contentDescription = "Invalid Name", tint = MaterialTheme.colorScheme.error)
                                }
                            },
                            singleLine = true,
                            isError = regNameError != null,
                            supportingText = {
                                if (regNameError != null) {
                                    Text(text = regNameError, color = MaterialTheme.colorScheme.error, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                } else {
                                    Text(text = "Enter your full official name", fontSize = 11.sp)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("register_name_input")
                        )

                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = {
                                regEmail = it
                                regEmailTouched = true
                                regErrorMessage = null
                            },
                            label = { Text("Work Email Address *") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                            trailingIcon = {
                                if (regEmailTouched && regEmailError == null && regEmail.isNotBlank()) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Valid Email", tint = Color(0xFF13AA52))
                                } else if (regEmailError != null) {
                                    Icon(Icons.Default.Error, contentDescription = "Invalid Email", tint = MaterialTheme.colorScheme.error)
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            isError = regEmailError != null,
                            supportingText = {
                                if (regEmailError != null) {
                                    Text(text = regEmailError, color = MaterialTheme.colorScheme.error, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                } else {
                                    Text(text = "Must be unique in MongoDB cluster (e.g. name@company.com)", fontSize = 11.sp)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("register_email_input")
                        )

                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = {
                                regPassword = it
                                regPasswordTouched = true
                                regErrorMessage = null
                            },
                            label = { Text("Password *") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (regPasswordTouched && regPasswordError == null && regPassword.isNotEmpty()) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Valid Password", tint = Color(0xFF13AA52), modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    IconButton(onClick = { isRegPasswordVisible = !isRegPasswordVisible }) {
                                        Icon(
                                            imageVector = if (isRegPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle password"
                                        )
                                    }
                                }
                            },
                            visualTransformation = if (isRegPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            isError = regPasswordError != null,
                            supportingText = {
                                if (regPasswordError != null) {
                                    Text(text = regPasswordError, color = MaterialTheme.colorScheme.error, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                } else {
                                    Text(text = "Minimum 6 characters", fontSize = 11.sp)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("register_password_input")
                        )

                        OutlinedTextField(
                            value = regConfirmPassword,
                            onValueChange = {
                                regConfirmPassword = it
                                regConfirmPasswordTouched = true
                                regErrorMessage = null
                            },
                            label = { Text("Confirm Password *") },
                            leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = "Confirm Password") },
                            trailingIcon = {
                                if (regConfirmPasswordTouched && regConfirmPasswordError == null && regConfirmPassword.isNotEmpty()) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Passwords Match", tint = Color(0xFF13AA52))
                                } else if (regConfirmPasswordError != null) {
                                    Icon(Icons.Default.Error, contentDescription = "Passwords Do Not Match", tint = MaterialTheme.colorScheme.error)
                                }
                            },
                            visualTransformation = if (isRegPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            isError = regConfirmPasswordError != null,
                            supportingText = {
                                if (regConfirmPasswordError != null) {
                                    Text(text = regConfirmPasswordError, color = MaterialTheme.colorScheme.error, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                } else {
                                    Text(text = "Must match password field exactly", fontSize = 11.sp)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("register_confirm_password_input")
                        )

                        Text(
                            text = "Department",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            departments.chunked(2).forEach { rowDepts ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowDepts.forEach { dept ->
                                        val isSel = dept == selectedDept
                                        FilterChip(
                                            selected = isSel,
                                            onClick = { selectedDept = dept },
                                            label = { Text(dept, fontSize = 11.sp) },
                                            modifier = Modifier.weight(1f).testTag("dept_chip_${dept.lowercase().replace(" ", "_")}")
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = "Enterprise Procurement Role",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            UserRole.entries.chunked(2).forEach { rowRoles ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowRoles.forEach { role ->
                                        val isSel = role == selectedRole
                                        FilterChip(
                                            selected = isSel,
                                            onClick = {
                                                selectedRole = role
                                                onSwitchPersonaRole(role)
                                            },
                                            label = { Text(role.displayName, fontSize = 11.sp) },
                                            leadingIcon = {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(role.badgeColorHex))
                                                )
                                            },
                                            modifier = Modifier.weight(1f).testTag("role_chip_${role.name.lowercase()}")
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = {
                                regNameTouched = true
                                regEmailTouched = true
                                regPasswordTouched = true
                                regConfirmPasswordTouched = true

                                val trimmedName = regName.trim()
                                val trimmedEmail = regEmail.trim()

                                if (!isRegFormValid) {
                                    regErrorMessage = when {
                                        trimmedName.length < 2 -> "Please enter a valid full name (minimum 2 characters)."
                                        trimmedEmail.isBlank() || !emailRegex.matches(trimmedEmail) -> "Please enter a valid work email address."
                                        isEmailDuplicate -> "An account with email '$trimmedEmail' is already registered in MongoDB."
                                        regPassword.length < 6 -> "Password must be at least 6 characters long."
                                        regConfirmPassword != regPassword -> "Passwords do not match. Please ensure both fields match."
                                        else -> "Please complete all registration fields correctly."
                                    }
                                    regSuccessMessage = null
                                    return@Button
                                }

                                // Execute Registration into MongoDB Document Store
                                onRegisterUser(trimmedName, trimmedEmail, selectedRole, selectedDept)

                                regSuccessMessage = "🎉 Account successfully registered and stored in MongoDB Database! Signed in as $trimmedName (${selectedRole.displayName})."
                                regErrorMessage = null

                                // Reset form state
                                regName = ""
                                regEmail = ""
                                regPassword = ""
                                regConfirmPassword = ""
                                regNameTouched = false
                                regEmailTouched = false
                                regPasswordTouched = false
                                regConfirmPasswordTouched = false

                                // Switch to Sign In tab view so user sees their new active session & document ID
                                selectedAuthTab = 0
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF13AA52),
                                disabledContainerColor = Color(0xFF13AA52).copy(alpha = 0.4f)
                            ),
                            enabled = isRegFormValid || (!regNameTouched && !regEmailTouched && !regPasswordTouched && !regConfirmPasswordTouched),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("register_submit_button")
                        ) {
                            Icon(Icons.Default.HowToReg, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Complete Registration (Save to MongoDB)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ValidationCheckItem(label: String, isMet: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = if (isMet) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = if (isMet) "Met" else "Not Met",
            tint = if (isMet) Color(0xFF13AA52) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                color = if (isMet) Color(0xFF13AA52) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}
