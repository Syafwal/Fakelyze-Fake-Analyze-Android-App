package com.wall.fakelyze.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    // Login UI State
    private val _loginUiState = MutableStateFlow(LoginUiState())
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    // Register UI State
    private val _registerUiState = MutableStateFlow(RegisterUiState())
    val registerUiState: StateFlow<RegisterUiState> = _registerUiState.asStateFlow()

    // Dummy user database for demonstration
    private val userDatabase = mutableMapOf(
        "admin@example.com" to UserData("Admin User", "admin@example.com", "admin123")
    )

    // Login Form handling
    fun updateEmail(email: String) {
        _loginUiState.update { currentState ->
            currentState.copy(
                email = email,
                emailError = validateEmail(email)
            )
        }
        validateLoginForm()
    }

    fun updatePassword(password: String) {
        _loginUiState.update { currentState ->
            currentState.copy(
                password = password,
                passwordError = validatePassword(password)
            )
        }
        validateLoginForm()
    }

    fun login(onSuccess: () -> Unit) {
        _loginUiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                // Simulate network delay
                delay(800)

                // Check if user exists in our database and password matches
                val email = _loginUiState.value.email
                val password = _loginUiState.value.password

                if (userDatabase.containsKey(email) && userDatabase[email]?.password == password) {
                    // Login successful
                    onSuccess()
                } else {
                    // Login failed
                    _loginUiState.update { it.copy(errorMessage = "Email atau kata sandi tidak valid") }
                }
            } catch (e: Exception) {
                _loginUiState.update { it.copy(errorMessage = e.message ?: "Terjadi kesalahan yang tidak diketahui") }
            } finally {
                _loginUiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // Register Form handling
    fun updateName(name: String) {
        _registerUiState.update { currentState ->
            currentState.copy(
                name = name,
                nameError = validateName(name)
            )
        }
        validateRegisterForm()
    }

    fun updateRegisterEmail(email: String) {
        _registerUiState.update { currentState ->
            currentState.copy(
                email = email,
                emailError = validateEmail(email)
            )
        }
        validateRegisterForm()
    }

    fun updateRegisterPassword(password: String) {
        _registerUiState.update { currentState ->
            currentState.copy(
                password = password,
                passwordError = validatePassword(password),
                // Also update confirm password error if it's not empty
                confirmPasswordError = if (currentState.confirmPassword.isNotEmpty()) {
                    validateConfirmPassword(password, currentState.confirmPassword)
                } else null
            )
        }
        validateRegisterForm()
    }

    fun updateConfirmPassword(confirmPassword: String) {
        _registerUiState.update { currentState ->
            currentState.copy(
                confirmPassword = confirmPassword,
                confirmPasswordError = validateConfirmPassword(currentState.password, confirmPassword)
            )
        }
        validateRegisterForm()
    }

    fun register(onSuccess: () -> Unit) {
        _registerUiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                // Simulate network delay
                delay(800)

                val email = _registerUiState.value.email
                val name = _registerUiState.value.name
                val password = _registerUiState.value.password

                // Check if user already exists
                if (userDatabase.containsKey(email)) {
                    _registerUiState.update { it.copy(errorMessage = "Email sudah terdaftar") }
                    return@launch
                }

                // Register the new user
                userDatabase[email] = UserData(name, email, password)

                // Registration successful
                onSuccess()
            } catch (e: Exception) {
                _registerUiState.update { it.copy(errorMessage = e.message ?: "Terjadi kesalahan yang tidak diketahui") }
            } finally {
                _registerUiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // Validation methods
    private fun validateEmail(email: String): String? {
        return if (email.isBlank()) {
            "Email wajib diisi"
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            "Masukkan email yang valid"
        } else {
            null
        }
    }

    private fun validatePassword(password: String): String? {
        return if (password.isBlank()) {
            "Kata sandi diperlukan"
        } else if (password.length < 6) {
            "Kata sandi minimal harus terdiri dari 6 karakter"
        } else {
            null
        }
    }

    private fun validateName(name: String): String? {
        return if (name.isBlank()) {
            "Nama wajib diisi"
        } else {
            null
        }
    }

    private fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return if (confirmPassword.isBlank()) {
            "Harap konfirmasi kata sandi Anda"
        } else if (password != confirmPassword) {
            "Kata sandi tidak cocok"
        } else {
            null
        }
    }

    private fun validateLoginForm() {
        val state = _loginUiState.value
        _loginUiState.update {
            it.copy(isFormValid = state.emailError == null && state.email.isNotBlank() &&
                   state.passwordError == null && state.password.isNotBlank())
        }
    }

    private fun validateRegisterForm() {
        val state = _registerUiState.value
        _registerUiState.update {
            it.copy(isFormValid = state.nameError == null && state.name.isNotBlank() &&
                   state.emailError == null && state.email.isNotBlank() &&
                   state.passwordError == null && state.password.isNotBlank() &&
                   state.confirmPasswordError == null && state.confirmPassword.isNotBlank())
        }
    }

    // Data class to represent a user in our local database
    data class UserData(
        val name: String,
        val email: String,
        val password: String
    )
}

// Login UI State
data class LoginUiState(
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val isFormValid: Boolean = false,
    val errorMessage: String? = null
)

// Register UI State
data class RegisterUiState(
    val name: String = "",
    val nameError: String? = null,
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val confirmPassword: String = "",
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val isFormValid: Boolean = false,
    val errorMessage: String? = null
)

