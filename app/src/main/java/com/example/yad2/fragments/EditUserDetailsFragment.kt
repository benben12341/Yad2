package com.example.yad2.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.yad2.R
import com.example.yad2.models.Model
import com.example.yad2.models.User
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso
import lombok.SneakyThrows
import java.io.IOException
import java.util.UUID

class EditUserDetailsFragment : Fragment() {
    private var imageBitmap: Bitmap? = null
    var imageUrl: String? = null
    var address: String? = null
    var phoneNumber: String? = null
    var firstName: String? = null
    var lastName: String? = null
    private var camBtn: FloatingActionButton? = null
    private var galleryBtn: FloatingActionButton? = null
    var userImage: ImageView? = null
    var editAddress: TextInputEditText? = null
    var editPhone: TextInputEditText? = null
    var editFirstName: TextInputEditText? = null
    var editLastName: TextInputEditText? = null
    var saveUpdates: Button? = null
    var editAddressLayout: TextInputLayout? = null
    private var progressBar: ProgressBar? = null
    var currentUser: User? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_edit_user_details, container, false)
        camBtn = view.findViewById<FloatingActionButton>(R.id.cameraBtn)
        camBtn?.setOnClickListener(View.OnClickListener { v: View? -> openCam() })
        galleryBtn = view.findViewById<FloatingActionButton>(R.id.galleryBtn)
        galleryBtn?.setOnClickListener(View.OnClickListener { v: View? -> openGallery() })
        progressBar = view.findViewById<ProgressBar>(R.id.edit_user_progress_bar)
        progressBar?.setVisibility(View.GONE)
        userImage = view.findViewById<ImageView>(R.id.user_edit_image)
        editAddress = view.findViewById<TextInputEditText>(R.id.user_edit_address)
        editPhone = view.findViewById<TextInputEditText>(R.id.user_edit_phone)
        editFirstName = view.findViewById<TextInputEditText>(R.id.user_edit_fname)
        editLastName = view.findViewById<TextInputEditText>(R.id.user_edit_lname)
        saveUpdates = view.findViewById<Button>(R.id.save_updates)
        val bundle = this.arguments
        if (bundle != null) {
            currentUser = bundle.getSerializable("user") as User?
        }
        editAddress?.setText(currentUser!!.address)
        editPhone?.setText(currentUser!!.phoneNumber)
        editFirstName?.setText(currentUser!!.firstName)
        editLastName?.setText(currentUser!!.lastName)
        if (currentUser!!.userImageUrl != null) {
            Picasso.get()
                .load(currentUser!!.userImageUrl)
                .into(userImage)
        }
        saveUpdates?.setOnClickListener(View.OnClickListener { view ->
            val address: String = editAddress?.getText().toString()
            val firstName: String = editFirstName?.getText().toString()
            val lastName: String = editLastName?.getText().toString()
            val phoneNumber: String = editPhone?.getText().toString()
            val activity: Activity? = activity
            if (TextUtils.isEmpty(address) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(
                    firstName
                ) || TextUtils.isEmpty(phoneNumber)
            ) {
                Toast.makeText(
                    activity,
                    "Please make sure all fields are filled",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                progressBar?.setVisibility(View.VISIBLE)
                currentUser!!.address = address
                currentUser!!.firstName = firstName
                currentUser!!.lastName = lastName
                currentUser!!.phoneNumber = phoneNumber
                val bundle = Bundle()
                if (imageBitmap == null) {
                    Model.instance.updateUser(currentUser!!) {
                        bundle.putSerializable("user", currentUser)
                        view.clearFocus()
                        progressBar?.setVisibility(View.GONE)
                        Toast.makeText(
                            activity, "user updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        Navigation.findNavController(view)
                            .navigate(R.id.to_nav_user_profile, bundle)
                    }
                } else {
                    Model.instance.saveProductImage(
                        imageBitmap!!,
                        UUID.randomUUID().toString() + ".jpg",
                        Model.SaveImageListener { url: String? ->
                            currentUser!!.userImageUrl = url
                            bundle.putSerializable("user", currentUser)
                            view.clearFocus()
                            progressBar?.setVisibility(View.GONE)
                            Toast.makeText(
                                activity, "user updated successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            Model.instance.updateUser(
                                currentUser!!
                            ) {
                                Navigation.findNavController(view).popBackStack()
                            }
                        })
                }
            }
        })
        return view
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
                userImage!!.setImageBitmap(imageBitmap)
            }
            if (requestCode == REQUEST_GALLERY) {
                val selectedImageUri = data!!.data
                if (selectedImageUri != null) {
                    userImage!!.setImageURI(selectedImageUri)
                    imageBitmap = try {
                        MediaStore.Images.Media.getBitmap(
                            this.requireContext().contentResolver,
                            selectedImageUri
                        )
                    } catch (e: IOException) {
                        throw RuntimeException(e)
                    }
                }
            }
        }
    }

    companion object {
        private const val REQUEST_CAMERA = 1
        private const val REQUEST_GALLERY = 2
    }
}