package com.velaphi.authetication.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.velaphi.authetication.data.model.AuthResult
import com.velaphi.authetication.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun signUp(
        email: String,
        password: String,
        displayName: String
    ): AuthResult<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                // Update display name
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                firebaseUser.updateProfile(profileUpdates).await()
                
                // Create user document in Firestore
                val user = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = displayName,
                    isEmailVerified = firebaseUser.isEmailVerified
                )
                
                firestore.collection("users").document(firebaseUser.uid).set(user).await()
                
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Failed to create user")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Sign up failed")
        }
    }

    override suspend fun signIn(email: String, password: String): AuthResult<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                // Update last login time
                val userRef = firestore.collection("users").document(firebaseUser.uid)
                userRef.update("lastLoginAt", System.currentTimeMillis()).await()
                
                val user = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName ?: "",
                    photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                    isEmailVerified = firebaseUser.isEmailVerified
                )
                
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Sign in failed")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Sign in failed")
        }
    }

    override suspend fun signOut(): AuthResult<Unit> {
        return try {
            auth.signOut()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Sign out failed")
        }
    }

    override suspend fun resetPassword(email: String): AuthResult<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Password reset failed")
        }
    }

    override suspend fun updateProfile(displayName: String): AuthResult<User> {
        return try {
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                firebaseUser.updateProfile(profileUpdates).await()
                
                // Update Firestore
                val userRef = firestore.collection("users").document(firebaseUser.uid)
                userRef.update("displayName", displayName).await()
                
                val user = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = displayName,
                    photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                    isEmailVerified = firebaseUser.isEmailVerified
                )
                
                AuthResult.Success(user)
            } else {
                AuthResult.Error("No user logged in")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Profile update failed")
        }
    }

    override suspend fun deleteAccount(): AuthResult<Unit> {
        return try {
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                // Delete user document from Firestore
                firestore.collection("users").document(firebaseUser.uid).delete().await()
                
                // Delete Firebase Auth user
                firebaseUser.delete().await()
                
                AuthResult.Success(Unit)
            } else {
                AuthResult.Error("No user logged in")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Account deletion failed")
        }
    }

    override fun getCurrentUser(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                val user = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName ?: "",
                    photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                    isEmailVerified = firebaseUser.isEmailVerified
                )
                trySend(user)
            } else {
                trySend(null)
            }
        }
        
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override fun isUserLoggedIn(): Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser != null)
        }
        
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }
}
