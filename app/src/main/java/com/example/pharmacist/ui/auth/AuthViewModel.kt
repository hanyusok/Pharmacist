package com.example.pharmacist.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val client: SupabaseClient
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            client.auth.sessionStatus.collect {
                when(it) {
                    is SessionStatus.Authenticated -> {
                        _isLoading.value = false
                        // Always set to Authenticated first
                        _authState.value = AuthState.Authenticated
                        // Then navigate if it's a new authentication
                        if (it.oldStatus !is SessionStatus.Authenticated) {
                            _authState.value = AuthState.NavigateToDrugList
                        }
                    }
                    is SessionStatus.NotAuthenticated -> {
                        _isLoading.value = false
                        _authState.value = AuthState.Unauthenticated
                    }
                    is SessionStatus.NetworkError -> {
                        _isLoading.value = false
                        _authState.value = AuthState.Error("Network error")
                    }
                    is SessionStatus.LoadingFromStorage -> {
                        // Handle the loading state, typically showing a loading indicator
                        _isLoading.value = true
                    }
                }
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                // Set navigation state after successful sign in
                _authState.value = AuthState.NavigateToDrugList
            } catch (e: Exception) {
                _isLoading.value = false
                _authState.value = AuthState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                client.auth.signOut()
                _authState.value = AuthState.Unauthenticated
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Sign out failed")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signUp(email: String, password: String, name: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                    // Add user metadata
                    data = buildJsonObject {
                        put("full_name", name)
                    }
                }
                // After successful signup, automatically sign in
                client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                _authState.value = AuthState.NavigateToDrugList
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Sign up failed")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(name: String, email: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // Update user profile in your backend
                _authState.value = AuthState.ProfileUpdated
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Profile update failed")
            } finally {
                _isLoading.value = false
            }
        }
    }
}

sealed class AuthState {
    object Initial : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object NavigateToDrugList : AuthState()
    object SignUpSuccess : AuthState()
    object ProfileUpdated : AuthState()
    data class Error(val message: String) : AuthState()
} 