package com.hfad.agencyapp.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthViewModel extends AndroidViewModel {

    private static final String TAG = "AuthViewModel";
    private FirebaseAuth firebaseAuth;
    public final MutableLiveData<FirebaseUser> userLive = new MutableLiveData<>();
    public final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    public final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        try {
            firebaseAuth = FirebaseAuth.getInstance();
            if (firebaseAuth != null) {
                userLive.setValue(firebaseAuth.getCurrentUser());
            }
        } catch (Exception e) {
            Log.w(TAG, "Firebase not initialized. Make sure google-services.json is configured.", e);
            errorMessage.setValue("Firebase not configured. Please add google-services.json to app/ directory.");
            firebaseAuth = null;
        }
    }

    public void signIn(String email, String password) {
        if (firebaseAuth == null) {
            errorMessage.postValue("Firebase not initialized. Please add google-services.json to app/ directory.");
            return;
        }
        
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            errorMessage.postValue("Email and password are required");
            return;
        }

        loading.postValue(true);
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    loading.postValue(false);
                    if (task.isSuccessful()) {
                        userLive.postValue(firebaseAuth.getCurrentUser());
                        errorMessage.postValue(null);
                    } else {
                        errorMessage.postValue(task.getException() != null ? task.getException().getMessage() : "Sign in failed");
                        userLive.postValue(null);
                    }
                });
    }

    public void signUp(String email, String password) {
        if (firebaseAuth == null) {
            errorMessage.postValue("Firebase not initialized. Please add google-services.json to app/ directory.");
            return;
        }
        
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            errorMessage.postValue("Email and password are required");
            return;
        }

        loading.postValue(true);
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    loading.postValue(false);
                    if (task.isSuccessful()) {
                        userLive.postValue(firebaseAuth.getCurrentUser());
                        errorMessage.postValue(null);
                    } else {
                        errorMessage.postValue(task.getException() != null ? task.getException().getMessage() : "Sign up failed");
                        userLive.postValue(null);
                    }
                });
    }

    public void signOut() {
        if (firebaseAuth != null) {
            firebaseAuth.signOut();
        }
        userLive.postValue(null);
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth != null ? firebaseAuth.getCurrentUser() : null;
    }
}



