package com.example.doodl.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.doodl.data.repository.AuthRepository
import kotlinx.coroutines.launch


class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> get() = _authState

    fun register(email: String, password: String) {
        viewModelScope.launch {
            val registerResult = repository.register(email, password)
            when (registerResult) {
                is RegistrationState.Success -> {
                    _authState.value = AuthState.Success
                }
                is RegistrationState.Error -> {
                    _authState.value = AuthState.Error(registerResult.message)
                }
                is RegistrationState.Loading -> {
                    // Handle the loading state if necessary.
                    // You might want to show a progress bar in the UI or similar.
                }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val loginResult = repository.login(email, password)
            when (loginResult) {
                is LoginState.Success -> {
                    _authState.value = AuthState.Success
                }
                is LoginState.Error -> {
                    _authState.value = AuthState.Error(loginResult.message)
                }
                is LoginState.Loading -> {
                    // Handle the loading state if necessary
                    // For example, you might want to show a progress bar in the UI
                }
            }
        }
    }


    fun resetAuthState() {
        _authState.value = null // Or some neutral state if you wish
    }

}

// Sealed class to represent different UI states
sealed class AuthState {
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
sealed class RegistrationState {
    object Success : RegistrationState()
    object Loading : RegistrationState()
    data class Error(val message: String) : RegistrationState()
}

sealed class LoginState {
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}

class AuthViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


