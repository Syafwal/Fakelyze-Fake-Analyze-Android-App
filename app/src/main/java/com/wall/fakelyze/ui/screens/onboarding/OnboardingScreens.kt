package com.wall.fakelyze.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wall.fakelyze.R.drawable.onboarding_1
import com.wall.fakelyze.R.drawable.onboarding_2
import com.wall.fakelyze.R.drawable.onboarding_3
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class OnboardingPage(
    val imageRes: Int,
    val title: String,
    val description: String
)

@Composable
fun OnboardingScreens(
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pages = listOf(
        OnboardingPage(
            imageRes = onboarding_1,
            title = "Selamat Datang di Fakelyze",
            description = "Aplikasi cerdas untuk mendeteksi gambar palsu yang dibuat oleh AI dengan teknologi deteksi canggih."
        ),
        OnboardingPage(
            imageRes = onboarding_2,
            title = "Deteksi Gambar AI",
            description = "Cukup pilih atau ambil foto, dan Fakelyze akan segera menganalisis apakah gambar tersebut dibuat oleh AI atau asli."
        ),
        OnboardingPage(
            imageRes = onboarding_3,
            title = "Lindungi dari Hoax Visual",
            description = "Gunakan Fakelyze untuk memverifikasi keaslian gambar dan membedakan konten asli dari yang dibuat oleh AI."
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            TopSection(
                onSkipClick = onFinishOnboarding,
                isLastPage = pagerState.currentPage == pages.size - 1,
                modifier = Modifier.weight(1f)
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(10f)
                    .fillMaxWidth(),
            ) { position ->
                PagerScreen(onboardingPage = pages[position], position = position)
            }

            BottomSection(
                size = pages.size,
                index = pagerState.currentPage,
                pagerState = pagerState,
                onFinishClick = onFinishOnboarding
            )
        }
    }
}

@Composable
fun TopSection(
    onSkipClick: () -> Unit,
    isLastPage: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        if (!isLastPage) {
            // Use TextButton instead of Button for a flat clickable text
            androidx.compose.material3.TextButton(
                onClick = onSkipClick,
                modifier = Modifier,
            ) {
                Text(
                    text = "Lewati",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun BottomSection(
    size: Int,
    index: Int,
    pagerState: PagerState,
    onFinishClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        // Indicators
        MinimalDotIndicator(
            pageCount = size,
            pagerState = pagerState,
            modifier = Modifier.align(Alignment.Center),
        )

        // Only show the Finish button on the last page
        val isLastPage = index == size - 1
        if (isLastPage) {
            AnimatedVisibility(
                visible = true,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Button(
                    onClick = onFinishClick,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Mulai",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun PagerScreen(onboardingPage: OnboardingPage, position: Int) {
    // Animation states
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = true) {
        delay(300) // Delay untuk animasi bertahap
        visible = true
    }

    // Animation untuk gambar
    val imageScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.6f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "imageScale"
    )

    // Animation untuk text
    val textOffset by animateDpAsState(
        targetValue = if (visible) 0.dp else 100.dp,
        animationSpec = tween(durationMillis = 500),
        label = "textOffset"
    )

    // Animation untuk opacity
    val contentAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 700),
        label = "contentAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Image with animation
            Image(
                painter = painterResource(id = onboardingPage.imageRes),
                contentDescription = "Onboarding Image",
                modifier = Modifier
                    .size(300.dp)
                    .scale(imageScale)
                    .alpha(contentAlpha)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Title with animation
            Text(
                text = onboardingPage.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = textOffset)
                    .alpha(contentAlpha),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description with animation
            Text(
                text = onboardingPage.description,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = textOffset)
                    .alpha(contentAlpha),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MinimalDotIndicator(
    pageCount: Int,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    unselectedColor: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { iteration ->
            val isSelected = pagerState.currentPage == iteration

            val width by animateDpAsState(
                targetValue = if (isSelected) 24.dp else 8.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "dotWidth"
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .height(6.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) selectedColor else unselectedColor
                    )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    OnboardingScreens(onFinishOnboarding = {})
}
