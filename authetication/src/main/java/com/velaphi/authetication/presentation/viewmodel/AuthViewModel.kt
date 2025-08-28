package com.velaphi.authetication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.velaphi.authetication.data.model.AuthResult
import com.velaphi.authetication.data.model.User
import com.velaphi.authetication.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthResult<User>?>(null)
    val authState: StateFlow<AuthResult<User>?> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { user ->
                _currentUser.value = user
                _isLoggedIn.value = user != null
            }
        }
    }

    fun signUp(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authState.value = AuthResult.Loading
            
            val result = authRepository.signUp(email, password, displayName)
            _authState.value = result
            _isLoading.value = false
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authState.value = AuthResult.Loading
            
            val result = authRepository.signIn(email, password)
            _authState.value = result
            _isLoading.value = false
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.signOut()
            if (result is AuthResult.Success) {
                _authState.value = null
                _currentUser.value = null
                _isLoggedIn.value = false
            }
            _isLoading.value = false
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            val result = authRepository.resetPassword(email)
            // Handle password reset result separately since it returns Unit, not User
            if (result is AuthResult.Success) {
                // Password reset email sent successfully
                // We don't need to update _authState for this
            } else if (result is AuthResult.Error) {
                // Set error state for display
                _authState.value = AuthResult.Error(result.message)
            }
            _isLoading.value = false
        }
    }

    fun updateProfile(displayName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.updateProfile(displayName)
            if (result is AuthResult.Success) {
                _currentUser.value = result.data
            }
            _isLoading.value = false
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.deleteAccount()
            if (result is AuthResult.Success) {
                _authState.value = null
                _currentUser.value = null
                _isLoggedIn.value = false
            }
            _isLoading.value = false
        }
    }

    fun clearAuthState() {
        _authState.value = null
    }

    fun clearError() {
        val currentState = _authState.value
        if (currentState is AuthResult.Error) {
            _authState.value = null
        }
    }
}
