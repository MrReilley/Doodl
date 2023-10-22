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
        repository.register(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error(task.exception?.message ?: "Unknown error")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                repository.login(email, password)
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

// Sealed class to represent different UI states
sealed class AuthState {
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


