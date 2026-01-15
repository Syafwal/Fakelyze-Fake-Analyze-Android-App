package com.wall.fakelyze.ui.screens.premium

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wall.fakelyze.data.model.PremiumPlan
import com.wall.fakelyze.data.model.UserRole
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    viewModel: PremiumViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedPlan by remember { mutableStateOf<PremiumPlan?>(null) }
    var showQuickActionDialog by remember { mutableStateOf(false) }
    val isPremiumUser = uiState.userRole == UserRole.PREMIUM
    val scrollState = rememberScrollState()

    // Dialog untuk quick action
    if (showQuickActionDialog) {
        QuickActionDialog(
            isPremium = isPremiumUser,
            onDismiss = { showQuickActionDialog = false },
            onDowngradeToFree = {
                viewModel.downgradeToFree()
                showQuickActionDialog = false
            },
            onNavigateToPlans = {
                showQuickActionDialog = false
                // Scroll to plans section (approximate position)
                // Dalam implementasi nyata, bisa menggunakan LazyColumn dengan key
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isPremiumUser) "Kelola Paket Premium"
                        else "Upgrade ke Premium"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
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
                .verticalScroll(scrollState)
        ) {
            // Header Section dengan gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isPremiumUser) "Anda Premium User!" else "Unlock Premium Features",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = if (isPremiumUser) {
                            Modifier
                                .clickable { showQuickActionDialog = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        } else {
                            Modifier
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isPremiumUser)
                            "Kelola paket atau ubah ke paket lain • Tap untuk mengubah"
                        else
                            "Dapatkan akses unlimited dan fitur eksklusif",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        modifier = if (isPremiumUser) {
                            Modifier
                                .clickable { showQuickActionDialog = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        } else {
                            Modifier
                        }
                    )
                }
            }

            // Current Plan Section (hanya tampil jika user premium)
            if (isPremiumUser) {
                CurrentPlanSection(
                    currentPlanId = uiState.premiumStatus.planId,
                    availablePlans = uiState.availablePlans,
                    expiryDate = uiState.premiumStatus.expiryDate
                )
            }

            // Plan Selection Section
            Text(
                text = if (isPremiumUser) "Ubah Paket" else "Pilih Paket Yang Sesuai",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            // Free Plan Card (hanya tampil jika user bukan premium atau ingin downgrade)
            if (!isPremiumUser || selectedPlan == null) {
                PlanComparisonCard(
                    title = "Free",
                    price = "Gratis",
                    subtitle = "Untuk penggunaan terbatas",
                    features = listOf(
                        "10 scan per hari",
                        "Format: JPG",
                        "Max ukuran file: 10MB",
                        "Hasil analisis dasar",
                        "Iklan tampil"
                    ),
                    isSelected = selectedPlan == null,
                    isPremium = false,
                    onClick = { selectedPlan = null },
                    isCurrentPlan = isPremiumUser && !uiState.premiumStatus.isPremium
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Premium Plans
            uiState.availablePlans.forEach { plan ->
                PlanComparisonCard(
                    title = plan.name,
                    price = plan.price,
                    subtitle = plan.description,
                    features = plan.features,
                    isSelected = selectedPlan == plan,
                    isPremium = true,
                    isPopular = plan.isPopular,
                    onClick = { selectedPlan = plan },
                    isCurrentPlan = isPremiumUser && uiState.premiumStatus.planId == plan.id
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Features Comparison Table
            FeaturesComparisonSection()

            // Action Buttons
            ActionButtonsSection(
                isPremiumUser = isPremiumUser,
                selectedPlan = selectedPlan,
                currentPlanId = uiState.premiumStatus.planId,
                availablePlans = uiState.availablePlans,
                onPurchase = { viewModel.purchasePlan(it) },
                onChangePlan = { viewModel.changePlan(it) },
                onDowngrade = { viewModel.downgradeToFree() },
                onNavigateBack = { navController.navigateUp() }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PlanComparisonCard(
    title: String,
    price: String,
    subtitle: String,
    features: List<String>,
    isSelected: Boolean,
    isPremium: Boolean,
    isPopular: Boolean = false,
    isCurrentPlan: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() }
            .then(
                if (isSelected) {
                    Modifier.border(
                        2.dp,
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(12.dp)
                    )
                } else if (isCurrentPlan) {
                    Modifier.border(
                        2.dp,
                        MaterialTheme.colorScheme.secondary,
                        RoundedCornerShape(12.dp)
                    )
                } else {
                    Modifier
                }
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected || isCurrentPlan) 8.dp else 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentPlan) {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            } else if (isPremium && isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else if (isPremium) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        shape = RoundedCornerShape(12.dp) // PERBAIKAN: Tambah shape consistency
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth() // PERBAIKAN: Pastikan Column mengisi penuh width
                .padding(20.dp)
        ) {
            // Header dengan badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f) // PERBAIKAN: Tambah weight untuk space distribution
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth() // PERBAIKAN: Row mengisi width
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isPremium) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )

                        if (isPremium) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth() // PERBAIKAN: Text mengisi width
                    )
                }

                // Badges
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(start = 8.dp) // PERBAIKAN: Tambah padding untuk spacing
                ) {
                    if (isCurrentPlan) {
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.secondary,
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "PAKET AKTIF",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        if (isPopular) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    if (isPopular) {
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "POPULER",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp)) // PERBAIKAN: Spacing yang lebih konsisten

            // Price Section
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth() // PERBAIKAN: Row mengisi width
            ) {
                Text(
                    text = if (price == "Gratis") "Rp 0" else price,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isPremium) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                if (price != "Gratis") {
                    Text(
                        text = "/bulan",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp)) // PERBAIKAN: Spacing yang lebih konsisten

            // Features list
            Column(
                modifier = Modifier.fillMaxWidth() // PERBAIKAN: Column features mengisi width
            ) {
                features.forEach { feature ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth() // PERBAIKAN: Setiap row feature mengisi width
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = if (isPremium) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = feature,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f) // PERBAIKAN: Text menggunakan weight untuk mengisi sisa space
                        )
                    }
                }
            }

            // Selection indicator
            if (isSelected) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isPremium) "Paket Dipilih" else "Tetap Gratis",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else if (isCurrentPlan) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Paket Saat Ini",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
fun FeaturesComparisonSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Perbandingan Fitur Lengkap",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Fitur",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(2f)
                )
                Text(
                    text = "Free",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Premium",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Feature comparison rows
            val comparisons = listOf(
                Triple("Scan per hari", "10", "Unlimited"),
                Triple("Format file", "JPG", "Semua format"),
                Triple("Max ukuran file", "10MB", "50MB"),
                Triple("Analisis mendalam", "❌", "✓"),
                Triple("Bagikan hasil", "❌", "✓"),
                Triple("Backup cloud", "❌", "✓"),
                Triple("Tanpa iklan", "❌", "✓"),
                Triple("Priority support", "❌", "✓")
            )

            comparisons.forEach { (feature, free, premium) ->
                ComparisonRow(
                    feature = feature,
                    freeValue = free,
                    premiumValue = premium
                )
            }
        }
    }
}

@Composable
fun ComparisonRow(
    feature: String,
    freeValue: String,
    premiumValue: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = feature,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(2f)
        )

        Text(
            text = freeValue,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = if (freeValue == "❌") {
                MaterialTheme.colorScheme.outline
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.weight(1f)
        )

        Text(
            text = premiumValue,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = if (premiumValue == "✓") {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.primary
            },
            fontWeight = if (premiumValue == "✓" || premiumValue.contains("Unlimited")) {
                FontWeight.Bold
            } else {
                FontWeight.Normal
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun CurrentPlanSection(
    currentPlanId: String?,
    availablePlans: List<PremiumPlan>,
    expiryDate: Long?
) {
    val currentPlan = availablePlans.find { it.id == currentPlanId }

    // Format tanggal dari Long ke String
    val formattedExpiryDate = expiryDate?.let { timestamp ->
        try {
            val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
            dateFormat.format(Date(timestamp))
        } catch (e: Exception) {
            "Tidak diketahui"
        }
    } ?: "Tidak diketahui"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Paket Saat Ini",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (currentPlan != null) {
                Text(
                    text = currentPlan.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Berlaku hingga: $formattedExpiryDate",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            } else {
                Text(
                    text = "Anda belum memiliki paket premium.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun ActionButtonsSection(
    isPremiumUser: Boolean,
    selectedPlan: PremiumPlan?,
    currentPlanId: String?,
    availablePlans: List<PremiumPlan>,
    onPurchase: (PremiumPlan) -> Unit,
    onChangePlan: (PremiumPlan) -> Unit,
    onDowngrade: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (selectedPlan != null) {
            Button(
                onClick = {
                    if (isPremiumUser) {
                        onChangePlan(selectedPlan)
                    } else {
                        onPurchase(selectedPlan)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (isPremiumUser) "Ubah ke ${selectedPlan.name}" else "Beli ${selectedPlan.name} - ${selectedPlan.price}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Tetap Gunakan Gratis",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "• Bisa batalkan kapan saja\n• Pembayaran aman\n• Support 24/7",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // Downgrade button (hanya tampil untuk user premium)
        if (isPremiumUser && currentPlanId != null) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = {
                    onDowngrade()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = "Downgrade ke Gratis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun QuickActionDialog(
    isPremium: Boolean,
    onDismiss: () -> Unit,
    onDowngradeToFree: () -> Unit,
    onNavigateToPlans: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Kelola Paket Premium",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Apa yang ingin Anda lakukan?",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quick actions as cards
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onNavigateToPlans()
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Ubah Paket Premium",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Pilih paket premium lainnya",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDowngradeToFree()
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Downgrade ke Gratis",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Kembali ke paket gratis",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}
