package com.mohammad.journalapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mohammad.journalapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var auth: FirebaseAuth
    lateinit var user: FirebaseUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        var toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        binding.createAcctBTN.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
        auth = Firebase.auth
        binding.emailSignInButton.setOnClickListener {
            var email = binding.email.text.toString().trim()
            var password: String = binding.password.text.toString().trim()
            signIn(email, password)
        }
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    var user = auth.currentUser
                    var journalUser: JournalUser = JournalUser.instance!!
                    journalUser.userId =auth.currentUser?.uid
                    journalUser.username = auth.currentUser?.displayName
                    Log.i("TAGY", "authentication success" + user?.displayName)
                    goto()
                } else {
                    Toast.makeText(this, "Sign In Not Success", Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onStart() {
        super.onStart()
        var user = auth.currentUser
        if (user != null)
            goto()
    }

    private fun goto() {
        var intent: Intent = Intent(this, JournalListActivity::class.java)
        startActivity(intent)
    }
}