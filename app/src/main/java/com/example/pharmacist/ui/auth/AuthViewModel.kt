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
//import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import com.example.pharmacist.domain.model.UserData
import kotlinx.serialization.json.JsonPrimitive
import com.example.pharmacist.data.dto.UserProfileDto
import io.github.jan.supabase.postgrest.postgrest
//import kotlinx.datetime.Clock
//import kotlinx.datetime.TimeZone
//import kotlinx.datetime.toLocalDateTime

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

    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val session = client.auth.currentSessionOrNull()
                if (session != null) {
                    val userId = session.user?.id.toString()
                    
                    // Try to load from user_profiles table first
                    val profile = try {
                        client.postgrest["user_profiles"]
                            .select { 
                                filter { eq("id", userId) }
                            }
                            .decodeSingle<UserProfileDto>()
                    } catch (e: Exception) {
                        // If profile doesn't exist in table, create it from auth metadata
                        val user = session.user
                        val metadata = user?.userMetadata
                        val name = metadata?.get("full_name")?.toString() ?: ""
                        
                        val newProfile = UserProfileDto(
                            id = userId,
                            email = user?.email ?: "",
                            fullName = name
                        )
                        
                        // Insert the profile into the database
                        client.postgrest["user_profiles"]
                            .insert(newProfile)
                            .decodeSingle<UserProfileDto>()
                    }

                    _userData.value = UserData(
                        email = profile.email,
                        name = profile.fullName
                    )
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to load user profile", e)
                _error.value = e.message ?: "Failed to load user profile"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(name: String, email: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // 1. Update auth metadata
                client.auth.modifyUser {
                    data = buildJsonObject {
                        put("full_name", JsonPrimitive(name))
                    }
                }

                // 2. Get current user ID
                val userId = client.auth.currentUserOrNull()?.id ?: throw Exception("User not authenticated")

                // 3. Update or insert into user_profiles table
                val userProfile = UserProfileDto(
                    id = userId,
                    email = email,
                    fullName = name
                )

                client.postgrest["user_profiles"]
                    .upsert(userProfile) {
                        select()
                    }

                // 4. Update local state
                _userData.value = UserData(email = email, name = name)
                _authState.value = AuthState.ProfileUpdated
                
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Profile update failed", e)
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
    data object Initial : AuthState()
    data object Authenticated : AuthState()
    data object Unauthenticated : AuthState()
    data object NavigateToDrugList : AuthState()
    data object SignUpSuccess : AuthState()
    data object ProfileUpdated : AuthState()
    data class Error(val message: String) : AuthState()
} 