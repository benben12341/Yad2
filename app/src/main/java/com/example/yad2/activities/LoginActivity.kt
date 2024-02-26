package com.example.yad2.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.example.yad2.R

class LoginActivity : ComponentActivity() {
    private lateinit var emailEdt: TextInputEditText
    private lateinit var passwordEdt: TextInputEditText
    private lateinit var loginBtn: Button
    private lateinit var newUserTV: TextView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mAuth = FirebaseAuth.getInstance()

        if (mAuth.currentUser != null) {
            val i = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(i)
            finish()
        }

        emailEdt = findViewById(R.id.idEdtEmail)
        passwordEdt = findViewById(R.id.idEdtPassword)
        loginBtn = findViewById(R.id.idBtnLogin)
        newUserTV = findViewById(R.id.idTVNewUser)
        progressBar = findViewById(R.id.login_progress_bar)
        progressBar.visibility = View.GONE

        // adding click listener for our new user tv.
        newUserTV.setOnClickListener {
            // on below line opening a login activity.
            val i = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(i)
        }

        // adding on click listener for our login button.
        loginBtn.setOnClickListener {
            progressBar.visibility = View.VISIBLE

            val email = emailEdt.text.toString()
            val password = passwordEdt.text.toString()

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this@LoginActivity, "Please enter your credentials..", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this@LoginActivity, OnCompleteListener<AuthResult> { task ->
                    if (task.isSuccessful) {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@LoginActivity, "Login Successful..", Toast.LENGTH_SHORT).show()

                        val i = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(i)
                        finish()
                    } else {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@LoginActivity, "Please enter valid user credentials..", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}
