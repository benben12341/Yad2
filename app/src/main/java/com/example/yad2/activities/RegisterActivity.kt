package com.example.yad2.activities

import com.example.yad2.models.Model
import com.example.yad2.models.User
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.UUID
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.example.yad2.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import lombok.SneakyThrows

class RegisterActivity : AppCompatActivity() {
    private var firstNameEdt: TextInputEditText? = null
    private var lastNameEdt: TextInputEditText? = null
    private var passwordEdt: TextInputEditText? = null
    private var confirmPwdEdt: TextInputEditText? = null
    private var emailEdt: TextInputEditText? = null
    private var phoneNumberEdt: TextInputEditText? = null
    private var addressEdt: TextInputEditText? = null
    private var productImage: ImageView? = null
    private var camBtn: FloatingActionButton? = null
    private var galleryBtn: FloatingActionButton? = null
    private var loginTV: TextView? = null
    private var registerBtn: Button? = null
    private var mAuth: FirebaseAuth? = null
    private var imageBitmap: Bitmap? = null
    private var progressBar: ProgressBar? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        firstNameEdt = findViewById(R.id.idEdtFirstName)
        lastNameEdt = findViewById(R.id.idEdtLastName)
        passwordEdt = findViewById(R.id.idEdtPassword)
        confirmPwdEdt = findViewById(R.id.idEdtConfirmPassword)
        emailEdt = findViewById(R.id.idEdtEmail)
        phoneNumberEdt = findViewById(R.id.idEdtPhoneNumber)
        addressEdt = findViewById(R.id.idEdtAddress)
        loginTV = findViewById(R.id.idTVLoginUser)
        registerBtn = findViewById(R.id.idBtnRegister)
        mAuth = FirebaseAuth.getInstance()
        productImage = findViewById(R.id.productImage)
        camBtn = findViewById(R.id.cameraBtn)
        camBtn?.setOnClickListener { v -> openCam() }
        galleryBtn = findViewById(R.id.galleryBtn)
        galleryBtn?.setOnClickListener { v -> openGallery() }
        progressBar = findViewById(R.id.register_progressBar)
        progressBar!!.visibility = View.GONE

        // adding on click for login tv.
        loginTV!!.setOnClickListener { // opening a login activity on clicking login text.
            val i: Intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(i)
        }

        // adding click listener for register button.
        registerBtn!!.setOnClickListener {
            progressBar!!.visibility = View.GONE
            val firstName: String = firstNameEdt?.getText().toString()
            val lastName: String = lastNameEdt?.getText().toString()
            val email: String = emailEdt?.getText().toString()
            val phoneNumber: String = phoneNumberEdt?.getText().toString()
            val address: String = addressEdt?.getText().toString()
            val pwd: String = passwordEdt?.getText().toString()
            val cnfPwd: String = confirmPwdEdt?.getText().toString()
            if (pwd != cnfPwd && !TextUtils.isEmpty(pwd)) {
                Toast.makeText(
                    this@RegisterActivity,
                    "Please check both having same password..",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pwd) || TextUtils.isEmpty(
                    cnfPwd
                ) || TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(address)
            ) {
                Toast.makeText(
                    this@RegisterActivity,
                    "Please make sure all fields are filled",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                progressBar!!.visibility = View.VISIBLE

                // on below line we are creating a new user by passing email and password.
                mAuth!!.createUserWithEmailAndPassword(email, pwd)
                    .addOnCompleteListener(object : OnCompleteListener<AuthResult?> {
                        override fun onComplete(task: Task<AuthResult?>) {
                            // on below line we are checking if the task is success or not.
                            if (task.isSuccessful()) {
                                val user = User(
                                    firstName,
                                    lastName,
                                    email,
                                    phoneNumber,
                                    address,
                                    ArrayList()
                                )
                                if (imageBitmap != null) {
                                    Model.instance.saveUserImage(
                                        imageBitmap!!,
                                        UUID.randomUUID().toString() + ".jpg"
                                    ) { url ->
                                        user.userImageUrl = url.toString();
                                        Model.instance.saveUser(
                                            user,
                                            task.getResult()?.getUser()!!.getUid()
                                        )
                                    }
                                } else {
                                    Model.instance.saveUser(
                                        user,
                                        task.getResult()!!.getUser()!!.getUid()
                                    )
                                }
                                progressBar!!.visibility = View.GONE
                                Toast.makeText(
                                    this@RegisterActivity,
                                    "User Registered..",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val i: Intent =
                                    Intent(this@RegisterActivity, LoginActivity::class.java)
                                startActivity(i)
                                finish()
                            } else {

                                // in else condition we are displaying a failure toast message.
                                progressBar!!.visibility = View.GONE
                                Toast.makeText(
                                    this@RegisterActivity,
                                    "Fail to register user..",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    })
            }
        }
    }

    private fun openCam() {
        val intent: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_CAMERA)
    }

    private fun openGallery() {
        val intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_GALLERY)
    }

    @SneakyThrows
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                val extras = data!!.extras
                imageBitmap = extras!!["data"] as Bitmap?
                productImage!!.setImageBitmap(imageBitmap)
            }
            if (requestCode == REQUEST_GALLERY) {
                val selectedImageUri = data!!.data
                if (selectedImageUri != null) {
                    productImage!!.setImageURI(selectedImageUri)
                    imageBitmap = MediaStore.Images.Media.getBitmap(
                        this.getContentResolver(),
                        selectedImageUri
                    )
                }
            }
        }
    }

    companion object {
        private const val REQUEST_CAMERA = 1
        private const val REQUEST_GALLERY = 2
    }
}