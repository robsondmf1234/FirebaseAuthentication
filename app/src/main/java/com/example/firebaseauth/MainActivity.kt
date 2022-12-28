package com.example.firebaseauth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.firebaseauth.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        auth.signOut()
        setupListeners()
    }

    override fun onStart() {
        super.onStart()
        checkLoggedInState()
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            registerUser()
        }
        binding.btnLogin.setOnClickListener {
            authentication()
        }
    }

    private fun authentication() {
        val email = binding.etLoginEmail.text.toString()
        val password = binding.etLoginPassword.text.toString()
        if (email.isNotEmpty() && password.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    auth.signInWithEmailAndPassword(email, password).await()
                    withContext(Dispatchers.Main) {
//                        checkLoggedInState()
                        checkAuthentication()
                    }
                } catch (exception: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, exception.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    private fun checkAuthentication() {
        if (auth.currentUser != null) {
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
        }else{
            binding.txStatusLog.text = getString(R.string.txtNotAuthenticated)
        }
    }

    private fun registerUser() {
        val email = binding.etRegisterEmail.text.toString()
        val password = binding.etRegisterPassword.text.toString()
        if (email.isNotEmpty() && password.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    auth.createUserWithEmailAndPassword(email, password).await()
                    withContext(Dispatchers.Main) {
                        checkLoggedInState()
                    }
                } catch (exception: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, exception.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    private fun checkLoggedInState() {
        if (auth.currentUser == null) {
            binding.txStatusLog.text = getString(R.string.txtNotLogged)
        } else {
            binding.txStatusLog.text = getString(R.string.txtLogged)
        }
    }
}