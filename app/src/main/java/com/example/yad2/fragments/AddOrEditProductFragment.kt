package com.example.yad2.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import com.example.yad2.R
import com.example.yad2.enums.Gender
import com.example.yad2.enums.ProductCategory
import com.example.yad2.enums.ProductCondition
import com.example.yad2.models.Model
import com.example.yad2.models.Product
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import lombok.SneakyThrows
import java.util.Arrays
import java.util.Objects
import java.util.UUID
import java.util.stream.Collectors

class AddOrEditProductFragment : Fragment() {
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private var isEditMode = false
    private var currLocation: LatLng? = null
    private var progressBar: ProgressBar? = null
    private var title: TextInputEditText? = null
    private var gender: MaterialAutoCompleteTextView? = null
    private var category: MaterialAutoCompleteTextView? = null
    private var condition: MaterialAutoCompleteTextView? = null
    private var description: TextInputEditText? = null
    private var price: TextInputLayout? = null
    private var saveBtn: Button? = null
    private var productImage: ImageView? = null
    private var camBtn: FloatingActionButton? = null
    private var galleryBtn: FloatingActionButton? = null
    private var imageBitmap: Bitmap? = null
    private lateinit var categories: Array<String>
    private lateinit var genders: Array<String>
    private lateinit var states: Array<String>
    private var productId: String? = null
    private var isSold = false
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_add_or_edit_product, container, false)
        productId = AddOrEditProductFragmentArgs.fromBundle(requireArguments()).productId
        isEditMode = productId != null
        findAllElements(view)
        if (isEditMode) {
            (activity as AppCompatActivity?)?.supportActionBar?.setTitle("Edit Product")
            saveBtn!!.text = "update"
            setProductDetails()
        } else {
            (activity as AppCompatActivity?)?.supportActionBar?.setTitle("Add Product")
        }
        saveBtn!!.setOnClickListener {
            if (TextUtils.isEmpty(title?.text)) {
                title?.error = "required field"
            }
            if (TextUtils.isEmpty(title?.text)) {
                price?.error = "required field"
            } else {
                save()
            }
        }
        camBtn?.setOnClickListener { openCam() }
        galleryBtn?.setOnClickListener { openGallery() }
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ), 1337
        )
        getStateDropdown(view)
        getGenderDropdown(view)
        getProductCategory(view)
        currentLocation
        return view
    }

    private fun setProductDetails() {
       productId?.let {
            Model.instance.getProductById(productId!!, object: Model.GetProductByIdListener {
              override fun onComplete(product: Product) {
                    title?.setText(product.title)
                    val productPrice: String? = product.price
                    price?.editText?.setText(if (productPrice != null) product.price else "")
                    gender?.setText(
                        gender?.adapter?.getItem(
                            Arrays.stream(genders).collect(Collectors.toList())
                                .indexOf(product.gender)
                        ).toString(), false
                    )
                    category?.setText(
                        category!!.adapter.getItem(
                            Arrays.stream(categories).collect(
                                Collectors.toList()
                            ).indexOf(product.productCategory)
                        ).toString(), false
                    )
                    condition?.setText(
                        condition!!.adapter.getItem(
                            Arrays.stream(states).collect(
                                Collectors.toList()
                            ).indexOf(product.productCondition)
                        ).toString(), false
                    )
                    description?.setText(product.description)
                    if (product.imageUrl != null) {
                        Picasso.get().load(product.imageUrl).into(productImage)
                    }
                    isSold = product.isSold
                }
            })
        }
    }

        private fun findAllElements(view: View) {
            title = view.findViewById(R.id.productTitle)
            gender = view.findViewById(R.id.gender)
            category = view.findViewById(R.id.category)
            condition = view.findViewById(R.id.condition)
            description = view.findViewById(R.id.description)
            price = view.findViewById(R.id.productPrice)
            saveBtn = view.findViewById(R.id.addProductBtn)
            productImage = view.findViewById(R.id.productImage)
            camBtn = view.findViewById(R.id.cameraBtn)
            galleryBtn = view.findViewById(R.id.galleryBtn)
            progressBar = view.findViewById(R.id.add_product_progressbar)
            progressBar!!.visibility = View.GONE
        }

        private fun getStateDropdown(view: View) {
            val dropdown: AutoCompleteTextView =
                view.findViewById(R.id.condition)
            states = arrayOf(
                ProductCondition.GOOD_AS_NEW.toString(),
                ProductCondition.GOOD.toString(),
                ProductCondition.OK.toString()
            )
            setDropdownAdapter(dropdown, states)
        }

        private fun getGenderDropdown(view: View) {
            val dropdown: AutoCompleteTextView =
                view.findViewById(R.id.gender)
            genders = arrayOf(
                Gender.FEMALE.toString(),
                Gender.MALE.toString(),
                Gender.OTHER.toString()
            )
            setDropdownAdapter(dropdown, genders)
        }

        private fun getProductCategory(view: View) {
            val dropdown: AutoCompleteTextView =
                view.findViewById(R.id.category)
            categories = arrayOf(
                ProductCategory.PANTS.toString(),
                ProductCategory.SHIRTS.toString(),
                ProductCategory.SKIRTS.toString(),
                ProductCategory.DRESSES.toString(),
                ProductCategory.JUMPERS.toString(),
                ProductCategory.ACCESSORIES.toString(),
                ProductCategory.OTHER.toString()
            )
            setDropdownAdapter(dropdown, categories)
        }

        private fun setDropdownAdapter(dropdown: AutoCompleteTextView, items: Array<String>) {
            dropdown.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    R.layout.dropdown_item,
                    items
                )
            )
        }

    private fun save() {
        saveBtn!!.isEnabled = false
        camBtn!!.isEnabled = false
        galleryBtn!!.isEnabled = false
        val product = getProduct(productId)
        saveProduct(product)
    }

        private fun saveProduct(product: Product) {
            progressBar!!.visibility = View.VISIBLE
            if (imageBitmap == null) {
                Model.instance.saveProduct(product) {
                    progressBar!!.visibility = View.GONE
                    Toast.makeText(context, "saved product successfully!", Toast.LENGTH_LONG).show()
                    findNavController(title!!).navigateUp()
                }
            } else {
                Model.instance.saveProductImage(
                    imageBitmap!!,
                    UUID.randomUUID().toString() + ".jpg"
                ) { url ->
                    product.imageUrl = url
                    Model.instance.saveProduct(product) {
                        progressBar!!.visibility = View.GONE
                        Toast.makeText(
                            context,
                            "saved product successfully!",
                            Toast.LENGTH_LONG
                        )
                            .show()
                        findNavController(title!!).navigateUp()
                    }
                }
            }
        }

    private fun getProduct(id: String?): Product {
        val key = id ?: FirebaseDatabase.getInstance().reference.push().key!!
        return Product(
            key,
            Objects.requireNonNull(title!!.text).toString(),
            if (description!!.text != null) description!!.text.toString() else "",
            if (isInArray(
                    genders,
                    gender!!.text.toString()
                )
            ) gender!!.text.toString() else Gender.OTHER.toString(),
            if (isInArray(
                    states,
                    condition!!.text.toString()
                )
            ) condition!!.text.toString() else ProductCondition.OK.toString(),
            if (isInArray(
                    categories,
                    this.category!!.text.toString()
                )
            ) this.category!!.text.toString() else ProductCategory.OTHER.toString(),
            Objects.requireNonNull(price!!.editText)?.text.toString(),
            Model.instance.mAuth.uid,
            if (currLocation != null) currLocation!!.latitude else null,
            if (currLocation != null) currLocation!!.longitude else null,
            false,
            if (isEditMode) isSold else false
        )
    }

        private fun isInArray(array: Array<String>, string: String): Boolean {
            return listOf(*array).contains(string)
        }

    private fun openCam() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_CAMERA)
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
                            this.requireContext().contentResolver,
                            selectedImageUri
                        )
                    }
                }
            }
        }

        private fun openGallery() {
            val intent = Intent()
            intent.setType("image/*")
            intent.setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_GALLERY)
        }

        @get:SuppressLint("MissingPermission")
        private val currentLocation: Unit
        get () {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                == PackageManager.PERMISSION_GRANTED &&
                context?.let {
                    ContextCompat.checkSelfPermission(
                        it,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                }
                == PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    mFusedLocationProviderClient = LocationServices
                        .getFusedLocationProviderClient(requireActivity())
                    mFusedLocationProviderClient!!.getCurrentLocation(
                        PRIORITY_HIGH_ACCURACY,
                        object : CancellationToken() {
                            override fun onCanceledRequested(p0: OnTokenCanceledListener): CancellationToken {
                                TODO("Not yet implemented")
                            }

                            override fun isCancellationRequested(): Boolean {
                                return false
                            }
                        }).addOnSuccessListener { location: Location ->
                        currLocation = LatLng(
                            location.latitude,
                            location.longitude
                        )
                    }
                } catch (error: Error) {
                    Log.e("LocationError", error.message!!)
                }
            }
        }

        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
        ) {
            if (requestCode == 1337) {
                if (grantResults.isNotEmpty()) {
                    currentLocation
                }
            }
        }

        companion object {
            private const val REQUEST_CAMERA = 1
            private const val REQUEST_GALLERY = 2
        }
    }