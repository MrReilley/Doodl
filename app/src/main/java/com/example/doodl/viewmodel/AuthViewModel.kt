package com.example.doodl.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.doodl.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
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
                    // We can show a progress bar in the UI.
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
                    // We can show a progress bar in the UI
                }
            }
        }
    }

    fun sendPasswordResetEmail(email: String, context: Context) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Password reset email sent", Toast.LENGTH_SHORT).show()
                } else {
                    val errorMessage = task.exception?.message ?: "Error sending reset email"
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
    }



    fun resetAuthState() {
        _authState.value = null
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


