package com.example.firebaseauth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.firebaseauth.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //Obtem a instancia do Firebase
        auth = FirebaseAuth.getInstance()
        //Método para deslogar o usuer automaticamente (metodo comentado para que a sessão fique ativa)
//        auth.signOut()
        setupListeners()
    }

    override fun onStart() {
        super.onStart()
//        checkLoggedInState()
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            registerUser()
        }
        binding.btnLogin.setOnClickListener {
            authentication()
        }
        binding.btnUpdateProfile.setOnClickListener {
            updateProfile()
        }
    }

    private fun updateProfile() {
        //Verifica se o user esta logado
        auth.currentUser?.let { user ->
            val username = binding.edtUserName.text.toString()
            val photoUri = Uri.parse("android.resource://$packageName/${R.drawable.icon}")
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .setPhotoUri(photoUri)
                .build()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    user.updateProfile(profileUpdates).await()
                    withContext(Dispatchers.Main) {
                        checkLoggedInState()
                        Toast.makeText(
                            this@MainActivity,
                            "Successfully update user profile",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun authentication() {
        val email = binding.etLoginEmail.text.toString()
        val password = binding.etLoginPassword.text.toString()
        if (email.isNotEmpty() && password.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    //Chama o método para se autenticar passando (email e password)
                    auth.signInWithEmailAndPassword(email, password).await()
                    withContext(Dispatchers.Main) {
                        //Método para o user se autenticar
                       // checkAuthentication()

                    //    //Método para verificar se o user está logado
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

    private fun checkAuthentication() {
        //Verificar através do objeto currentUser,caso o o objeto currentUser não seja nulo,
        // o usuario está logado.
        if (auth.currentUser != null) {
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
        } else {
            binding.tvLoggedIn.text = getString(R.string.txtNotAuthenticated)
        }
    }

    private fun registerUser() {
        val email = binding.etRegisterEmail.text.toString()
        val password = binding.etRegisterPassword.text.toString()
        if (email.isNotEmpty() && password.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    //Chama o método para se registrar passando (email e password)
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

    //Método para verificar se o user está logado
    private fun checkLoggedInState() {
        val user = auth.currentUser
        if (user == null) {
            binding.tvLoggedIn.text = getString(R.string.txtNotLogged)
        } else {
            binding.tvLoggedIn.text = getString(R.string.txtLogged)
            binding.edtUserName.setText(user.displayName)
            binding.ivProfilePicture.setImageURI(user.photoUrl)
        }
    }
}