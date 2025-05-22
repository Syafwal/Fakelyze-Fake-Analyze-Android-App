package com.wall.fakelyze.ui.screens.learn

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LearnViewModel : ViewModel() {
    // UI state
    private val _uiState = MutableStateFlow(LearnUiState())
    val uiState: StateFlow<LearnUiState> = _uiState.asStateFlow()

    // Initialize learning content
    init {
        _uiState.value = LearnUiState(
            learningSections = getLearningSections()
        )
    }

    private fun getLearningSections(): List<LearningSection> {
        return listOf(
            LearningSection(
                title = "Cara Kerja Deteksi Gambar AI",
                description = "Deteksi gambar yang dihasilkan oleh AI menggunakan model pembelajaran mendalam untuk mengidentifikasi pola dan artefak yang menjadi karakteristik gambar yang dibuat oleh sistem AI. Model ini menganalisis berbagai fitur visual yang mungkin terlewatkan oleh mata manusia, namun umum ditemukan dalam konten yang dihasilkan oleh AI."
            ),
            LearningSection(
                title = "MobileNetV2 Architecture",
                description = "MobileNetV2 adalah arsitektur Convolutional Neural Network (CNN) ringan yang dirancang untuk perangkat seluler. Arsitektur ini menggunakan konvolusi yang dapat dipisahkan berdasarkan kedalaman untuk mengurangi komputasi dan ukuran model dengan tetap mempertahankan akurasi yang tinggi. Dalam aplikasi ini, Saya telah menyempurnakan MobileNetV2 menggunakan pembelajaran Transfer Learning untuk secara khusus mendeteksi gambar yang dihasilkan oleh AI."
            ),
            LearningSection(
                title = "Transfer Learning",
                description = " Transfer learning adalah teknik pembelajaran mesin di mana model yang telah dilatih sebelumnya yang dikembangkan untuk satu tugas digunakan kembali sebagai titik awal untuk model pada tugas kedua. Untuk deteksi gambar AI, saya mulai dengan MobileNetV2 yang telah dilatih sebelumnya pada ImageNet dan kemudian menyempurnakannya pada kumpulan data gambar nyata dan gambar yang dihasilkan AI. Pendekatan ini memungkinkan saya untuk memanfaatkan kemampuan ekstraksi fitur yang kuat dari model asli sambil mengkhususkannya untuk tugas deteksi spesifik."
            ),
            LearningSection(
                title = "TensorFlow Lite",
                description = "TensorFlow Lite adalah solusi ringan untuk perangkat seluler khususny Android dan perangkat yang disematkan. Solusi ini memungkinkan inferensi pembelajaran mesin pada perangkat dengan latensi rendah. Dalam aplikasi ini, saya menggunakan TensorFlow Lite untuk menjalankan model MobileNetV2 secara efisien di perangkat Android Anda, memungkinkan deteksi gambar AI secara real-time tanpa perlu mengirim foto Anda ke server."
            ),
            LearningSection(
                title = "Common AI Image Artifacts",
                description = "Gambar yang dihasilkan AI sering kali mengandung tanda yang mencurigakan:\n\n" +
                        "• Tekstur dan pola yang tidak alami\n" +
                        "• Pencahayaan dan bayangan yang tidak konsisten\n" +
                        "• Fitur wajah atau proporsi tubuh yang terdistorsi\n" +
                        "• Detail yang aneh dan tidak realistis\n" +
                        "• Area buram dan terlalu halus\n\n" +
                        " Model ini telah dilatih untuk mengenali pola-pola ini dan pola-pola halus lainnya yang mengindikasikan pembangkitan AI."
            ),
            LearningSection(
                title = "Training Dataset",
                description = " Model ini dilatih pada set data yang beragam yang berisi ribuan foto asli dan gambar yang dihasilkan AI dari berbagai sumber termasuk DALL-E, Midjourney, Stable Diffusion, dan generator gambar AI populer lainnya. Keragaman ini memungkinkan model untuk mendeteksi gambar dari sistem AI yang berbeda dengan akurasi yang tinggi."
            ),
            LearningSection(
                title = "Model Performance",
                description = "Detektor berbasis MobileNetV2 mencapai akurasi sekitar 93% pada data validasi. Meskipun ini cukup bagus, namun penting untuk dicatat bahwa teknologi pembuatan gambar AI terus berkembang, dan model yang lebih baru mungkin menghasilkan gambar yang lebih sulit dideteksi."
            )
        )
    }
}

data class LearnUiState(
    val learningSections: List<LearningSection> = emptyList()
)

data class LearningSection(
    val title: String,
    val description: String
)
