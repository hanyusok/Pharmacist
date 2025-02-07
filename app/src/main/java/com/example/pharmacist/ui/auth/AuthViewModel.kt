package com.example.pharmacist.ui.auth

import android.util.Log
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

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData.asStateFlow()

    init {
        checkAuthState()
        loadUserProfile()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            client.auth.sessionStatus.collect {
                when(it) {
                    is SessionStatus.Authenticated -> {
                        _isLoading.value = false
                        loadUserProfile()
                        _authState.value = AuthState.Authenticated
                        if (it.oldStatus !is SessionStatus.Authenticated) {
                            _authState.value = AuthState.NavigateToDrugList
                        }
                    }
                    is SessionStatus.NotAuthenticated -> {
                        _isLoading.value = false
                        _authState.value = AuthState.Unauthenticated
                        _userData.value = null
                    }
                    is SessionStatus.NetworkError -> {
                        _isLoading.value = false
                        _authState.value = AuthState.Error("Network error")
                    }
                    is SessionStatus.LoadingFromStorage -> {
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
                    data = buildJsonObject {
                        put("full_name", name)
                    }
                }
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

    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val user = client.auth.currentUserOrNull()
                if (user != null) {
                    Log.d("AuthViewModel", "Current user: ${user.email}, metadata: ${user.userMetadata}")
                    
                    _userData.value = UserData(
                        email = user.email ?: "",
                        name = user.userMetadata?.get("full_name")?.toString() ?: "",
                        id = user.id
                    )
                } else {
                    Log.d("AuthViewModel", "No current user found")
                    _userData.value = null
                    _authState.value = AuthState.Unauthenticated
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error loading user profile", e)
                _authState.value = AuthState.Error(e.message ?: "Failed to load profile")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(name: String, email: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val currentUser = client.auth.currentUserOrNull()
                if (currentUser == null) {
                    _authState.value = AuthState.Error("No authenticated user found")
                    return@launch
                }

                client.auth.modifyUser {
                    this.email = email
                    data = buildJsonObject {
                        put("full_name", name)
                    }
                }

                loadUserProfile()
                
                _authState.value = AuthState.ProfileUpdated
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error updating profile", e)
                _authState.value = AuthState.Error(e.message ?: "Profile update failed")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setError(message: String?) {
        _error.value = message
    }

    fun clearError() {
        _error.value = null
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

data class UserData(
    val email: String,
    val name: String,
    val id: String
) 