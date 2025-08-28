package com.velaphi.authetication.data.repository

import com.velaphi.authetication.data.model.AuthResult
import com.velaphi.authetication.data.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signUp(email: String, password: String, displayName: String): AuthResult<User>
    suspend fun signIn(email: String, password: String): AuthResult<User>
    suspend fun signOut(): AuthResult<Unit>
    suspend fun resetPassword(email: String): AuthResult<Unit>
    suspend fun updateProfile(displayName: String): AuthResult<User>
    suspend fun deleteAccount(): AuthResult<Unit>
    fun getCurrentUser(): Flow<User?>
    fun isUserLoggedIn(): Flow<Boolean>
}
