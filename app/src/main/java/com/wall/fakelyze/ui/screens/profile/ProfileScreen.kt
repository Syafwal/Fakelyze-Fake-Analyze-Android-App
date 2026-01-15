package com.wall.fakelyze.ui.screens.profile

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.wall.fakelyze.R
import com.wall.fakelyze.ui.component.PremiumBadge
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val uiState by viewModel.combinedUiState.collectAsState() // PERBAIKAN: Gunakan combinedUiState untuk stats otomatis
    var isLoggedOut by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // PERBAIKAN: Trigger refresh stats ketika screen muncul
    LaunchedEffect(Unit) {
        viewModel.refreshHistoryStats()
    }

    // State untuk foto profil
    var profilePhotoUri by remember { mutableStateOf<Uri?>(uiState.profilePhotoUri) }
    LaunchedEffect(uiState.profilePhotoUri) {
        profilePhotoUri = uiState.profilePhotoUri
    }

    // State untuk bottom sheet
    var showBottomSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState()

    // State untuk permission
    var showPermissionDialog by remember { mutableStateOf(false) }
    var permissionToRequest by remember { mutableStateOf("") }

    // Dialog state untuk editing profile
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showDowngradeDialog by remember { mutableStateOf(false) }
    var editUsername by remember { mutableStateOf("") }
    var editDisplayName by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }

    // Uri untuk foto dari kamera
    var tempCameraUri: Uri? by remember { mutableStateOf(null) }

    // Buat file untuk foto kamera
    fun createImageFile(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"

        // Membuat direktori ProfilePhotos jika belum ada
        val storageDir = File(context.getExternalFilesDir(null), "ProfilePhotos")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        val imageFile = File.createTempFile(imageFileName, ".jpg, .png, .jpeg", storageDir)

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    }

    // DEKLARASI LAUNCHER DULU SEBELUM DIGUNAKAN
    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val currentUri = tempCameraUri
        if (success && currentUri != null) {
            profilePhotoUri = currentUri
            viewModel.updateProfilePhoto(currentUri)
            showBottomSheet = false
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            profilePhotoUri = it
            viewModel.updateProfilePhoto(it)
            showBottomSheet = false
        }
    }

    // Permission launcher - PINDAHKAN KE BAWAH SETELAH FUNGSI DIDEKLARASI
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            when (permissionToRequest) {
                Manifest.permission.CAMERA -> {
                    try {
                        tempCameraUri = createImageFile()
                        val currentUri = tempCameraUri
                        currentUri?.let { uri ->
                            cameraLauncher.launch(uri)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_MEDIA_IMAGES -> {
                    galleryLauncher.launch("image/*")
                }
            }
        }
    }

    // SETELAH LAUNCHER DIDEKLARASI, BARU FUNGSI YANG MENGGUNAKANNYA
    // Function to open camera
    fun openCamera() {
        try {
            tempCameraUri = createImageFile()
            val currentUri = tempCameraUri
            currentUri?.let { uri ->
                cameraLauncher.launch(uri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Function to open gallery
    fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    // Function to check and request camera permission
    fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                permissionToRequest = Manifest.permission.CAMERA
                showPermissionDialog = true
            }
        }
    }

    // Function to check and request gallery permission
    fun checkGalleryPermissionAndOpen() {
        val readImagePermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(
                context,
                readImagePermission
            ) == PackageManager.PERMISSION_GRANTED -> {
                openGallery()
            }
            else -> {
                permissionToRequest = readImagePermission
                showPermissionDialog = true
            }
        }
    }

    // Function to open edit dialog with current values
    fun openEditDialog() {
        editUsername = uiState.username
        editDisplayName = uiState.displayName
        editEmail = uiState.email
        editPhone = uiState.phone
        showEditProfileDialog = true
    }

    // Observe logout state and navigate when true
    LaunchedEffect(isLoggedOut) {
        if (isLoggedOut) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profil") },
            text = {
                Column(modifier = Modifier.padding(8.dp)) {
                    // Foto Profil Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Profile Image Preview
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                .clickable { showBottomSheet = true },
                            contentAlignment = Alignment.Center
                        ) {
                            if (profilePhotoUri != null) {
                                AsyncImage(
                                    model = profilePhotoUri,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.profile_placeholder),
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Foto Profil",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tap untuk mengubah",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        TextButton(
                            onClick = { showBottomSheet = true }
                        ) {
                            Text("Ubah")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = editDisplayName,
                        onValueChange = { editDisplayName = it },
                        label = { Text("Nama") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )

                    OutlinedTextField(
                        value = editUsername,
                        onValueChange = { editUsername = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )

                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )

                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Nomor Telepon") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateUserProfile(
                            editUsername,
                            editDisplayName,
                            editEmail,
                            editPhone
                        )
                        showEditProfileDialog = false
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Permission dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Izin Diperlukan") },
            text = {
                Text("Untuk dapat mengubah foto profil, aplikasi memerlukan izin akses " +
                    if (permissionToRequest == Manifest.permission.CAMERA) "kamera." else "media/penyimpanan.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        permissionLauncher.launch(permissionToRequest)
                        showPermissionDialog = false
                    }
                ) {
                    Text("Berikan Izin")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Downgrade Confirmation Dialog
    if (showDowngradeDialog) {
        AlertDialog(
            onDismissRequest = { showDowngradeDialog = false },
            title = {
                Text(
                    text = "Konfirmasi Downgrade",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Apakah Anda yakin ingin downgrade ke paket gratis?",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Anda akan kehilangan:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            val lostFeatures = listOf(
                                "• Scan unlimited (kembali ke 10 scan/hari)",
                                "• Format file PNG, JPEG, WEBP dan BMP",
                                "• Max ukuran file 50MB (kembali ke 10MB)",
                                "• Analisis mendalam",
                                "• Akses tanpa iklan"
                            )

                            lostFeatures.forEach { feature ->
                                Text(
                                    text = feature,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Panggil fungsi downgrade di ViewModel
                        viewModel.downgradeToPremium()
                        showDowngradeDialog = false
                    },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        text = "Ya, Downgrade",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDowngradeDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Bottom sheet for choosing image source
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = bottomSheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Ubah Foto Profil",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                ListItem(
                    headlineContent = { Text("Ambil Foto") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Camera,
                            contentDescription = "Camera"
                        )
                    },
                    modifier = Modifier.clickable { checkCameraPermissionAndOpen() }
                )

                ListItem(
                    headlineContent = { Text("Pilih dari Galeri") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Gallery"
                        )
                    },
                    modifier = Modifier.clickable { checkGalleryPermissionAndOpen() }
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                actions = {
                    // Add edit button in the top bar
                    IconButton(onClick = { openEditDialog() }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profil"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Header with Avatar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Image
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .clickable { openEditDialog() }, // Langsung buka edit dialog saat foto diklik
                        contentAlignment = Alignment.Center
                    ) {
                        if (profilePhotoUri != null) {
                            AsyncImage(
                                model = profilePhotoUri,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.profile_placeholder),
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // User Name
                    Text(
                        text = uiState.displayName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // User Status or Role
                    Text(
                        text = uiState.role,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            // Profile Information Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Personal Information",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email
                    ProfileInfoItem(
                        icon = Icons.Default.Email,
                        label = "Email",
                        value = uiState.email
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Phone
                    ProfileInfoItem(
                        icon = Icons.Default.Phone,
                        label = "Phone",
                        value = uiState.phone
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Username
                    ProfileInfoItem(
                        icon = Icons.Default.Person,
                        label = "Username",
                        value = uiState.username
                    )
                }
            }

            // Activity Stats Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Activity Stats",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(label = "Scans", value = uiState.scanCount.toString())
                        StatItem(label = "AI Detected", value = uiState.aiDetectedCount.toString())
                        StatItem(label = "Real Images", value = uiState.realImageCount.toString())
                    }
                }
            }

            // Account & System Info Card (Gabungan)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Account & System Info",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Account Status Section - dengan logic premium
                    SettingItem(
                        icon = Icons.Default.Star,
                        title = if (uiState.isPremium) "Premium Account" else "Free Account",
                        subtitle = getPremiumSubtitle(uiState),
                        action = {
                            if (!uiState.isPremium) {
                                Row {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                MaterialTheme.colorScheme.surfaceVariant,
                                                CircleShape
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "FREE",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    TextButton(
                                        onClick = { navController.navigate("premium") }
                                    ) {
                                        Text("Upgrade")
                                    }
                                }
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.End
                                ) {
                                    PremiumBadge()
                                    uiState.premiumExpiryDate?.let { expiryDate ->
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = getPremiumTimeRemaining(expiryDate),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    // Tambah tombol untuk kelola paket premium
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        TextButton(
                                            onClick = { navController.navigate("premium") }
                                        ) {
                                            Text(
                                                text = "Kelola",
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        }
                                        TextButton(
                                            onClick = {
                                                // Trigger dialog konfirmasi downgrade
                                                showDowngradeDialog = true
                                            },
                                            colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                                                contentColor = MaterialTheme.colorScheme.error
                                            )
                                        ) {
                                            Text(
                                                text = "Downgrade",
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // File Format Support Section - dengan logic premium
                    SettingItem(
                        icon = Icons.Default.FileOpen,
                        title = "Format File Didukung",
                        subtitle = getFileFormatSubtitle(uiState.isPremium),
                        action = {
                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // User free hanya bisa upload JPG
                                    FormatTag("JPG")

                                    // User premium bisa upload format tambahan
                                    if (uiState.isPremium) {
                                        FormatTag("WEBP")
                                        FormatTag("PNG")
                                        FormatTag("JPEG")
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (uiState.isPremium) "Max 50MB" else "Max 10MB",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Storage Usage Section - dengan logic premium
                    SettingItem(
                        icon = Icons.Default.Storage,
                        title = "Penggunaan Storage",
                        subtitle = getStorageSubtitle(uiState),
                        action = {
                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "${uiState.scanCount} file",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "~${uiState.scanCount * 2}MB",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (uiState.isPremium) {
                                    Text(
                                        text = "Cloud Backup: ON",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    )

                    // Scan Limit Section (khusus untuk free user)
                    if (!uiState.isPremium) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        SettingItem(
                            icon = Icons.Default.Star,
                            title = "Scan Limit Harian",
                            subtitle = getScanLimitSubtitle(uiState.remainingScans),
                            action = {
                                Column(
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = if (uiState.remainingScans == -1) "∞" else "${uiState.remainingScans}/10",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (uiState.remainingScans <= 3 && uiState.remainingScans != -1)
                                            MaterialTheme.colorScheme.error
                                        else
                                            MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (uiState.remainingScans == -1) "unlimited" else "tersisa",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        )
                    } else {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        // PERBAIKAN: Tampilkan status premium unlimited scans
                        SettingItem(
                            icon = Icons.Default.Star,
                            title = "Premium Unlimited Scans",
                            subtitle = "Scan tanpa batas setiap hari",
                            action = {
                                Column(
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "∞",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "unlimited",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        )
                    }
                }
            }

            // App Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Info sections from InfoViewModel
                    val infoSections = getInfoSections()
                    infoSections.forEach { section ->
                        ModelInfoCard(
                            title = section.title,
                            description = section.description
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Logout Button
            Button(
                onClick = { viewModel.logout { isLoggedOut = true } },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = "Logout")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProfileInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    action: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        action()
    }
}

data class InfoSection(
    val title: String,
    val description: String
)

@Composable
fun ModelInfoCard(
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun getInfoSections(): List<InfoSection> {
    return listOf(
        InfoSection(
            title = "Arsitektur Model",
            description = "Menggunakan MobileNetV2 dengan pembelajaran transfer untuk deteksi gambar AI yang efisien pada perangkat mobile."
        ),
        InfoSection(
            title = "Framework",
            description = "TensorFlow Lite untuk optimalisasi performa dan ukuran model pada perangkat Android."
        ),
        InfoSection(
            title = "Akurasi Model",
            description = "Model telah dilatih dengan dataset yang beragam untuk mencapai tingkat akurasi yang tinggi dalam mendeteksi gambar AI."
        ),
        InfoSection(
            title = "Penggunaan",
            description = "Aplikasi dapat menganalisis gambar dari galeri atau kamera langsung untuk mendeteksi apakah gambar dibuat oleh AI."
        )
    )
}





@Composable
fun AccountStatusCard(
    role: String,
    onUpgradeClick: () -> Unit
) {
    val isPremium = role == "Premium"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPremium)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (isPremium)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = if (isPremium) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isPremium) "Premium Account" else "Free Account",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isPremium)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isPremium)
                        "Nikmati semua fitur premium tanpa batas"
                    else
                        "10 scan per hari • Upgrade untuk unlimited",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isPremium) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Scan Unlimited",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Action button/badge
            if (isPremium) {
                PremiumBadge()
            } else {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                CircleShape
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "FREE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = onUpgradeClick,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = "Upgrade",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FileFormatCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FileOpen,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Format File Didukung",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Jenis gambar yang dapat dianalisis aplikasi",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Format tags
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FormatTag("JPG")
                    FormatTag("PNG")
                    FormatTag("JPEG")
                }
            }

            // File size limit info
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Max Size",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "10MB",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun FormatTag(format: String) {
    Box(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                CircleShape
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = format,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// Fungsi untuk mendapatkan subtitle premium secara dinamis
fun getPremiumSubtitle(uiState: com.wall.fakelyze.ui.screens.profile.ProfileUiState): String {
    return if (uiState.isPremium) {
        "Nikmati semua fitur premium tanpa batas"
    } else {
        "10 scan per hari • Upgrade untuk unlimited"
    }
}

// Fungsi untuk mendapatkan subtitle format file secara dinamis
fun getFileFormatSubtitle(isPremium: Boolean): String {
    return if (isPremium) {
        "JPG, WEBP, PNG, JPEG • Max 50MB"
    } else {
        "Hanya JPG • Max 10MB"
    }
}

// Fungsi untuk mendapatkan subtitle penggunaan storage secara dinamis
fun getStorageSubtitle(uiState: com.wall.fakelyze.ui.screens.profile.ProfileUiState): String {
    return if (uiState.isPremium) {
        "Data lokal aplikasi dan cadangan cloud"
    } else {
        "Data lokal aplikasi"
    }
}

// Fungsi untuk mendapatkan subtitle batasan scan secara dinamis
fun getScanLimitSubtitle(remainingScans: Int): String {
    return "$remainingScans/10"
}

// Fungsi untuk menghitung sisa waktu premium
fun getPremiumTimeRemaining(expiryDate: Long): String {
    val currentTime = System.currentTimeMillis()
    val diff = expiryDate - currentTime

    return when {
        diff <= 0 -> "Expired"
        diff < 24 * 60 * 60 * 1000 -> "< 1 hari"
        else -> {
            val daysRemaining = diff / (24 * 60 * 60 * 1000)
            "$daysRemaining hari lagi"
        }
    }
}
