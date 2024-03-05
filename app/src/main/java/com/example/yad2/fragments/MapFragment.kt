package com.example.yad2.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.yad2.viewModels.ProductListRvViewModel
import com.example.yad2.R
import com.example.yad2.models.Product
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.example.yad2.fragments.MapFragmentDirections

class MapFragment : Fragment() {
    var viewModel: ProductListRvViewModel? = null
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private var gmGoogleMap: GoogleMap? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel =
            ViewModelProvider(this).get<ProductListRvViewModel>(ProductListRvViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Initialize view
        val view: View = inflater.inflate(R.layout.fragment_map, container, false)

        // Initialize map fragment
        val supportMapFragment: SupportMapFragment? =
            childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment?

        // Async map
        supportMapFragment?.getMapAsync(object : OnMapReadyCallback {
            @SuppressLint("MissingPermission")
            override fun onMapReady(googleMap: GoogleMap) {
                gmGoogleMap = googleMap
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ), 1337
                )
                relocateMap(googleMap)
                googleMap.setOnInfoWindowClickListener(object :
                    GoogleMap.OnInfoWindowClickListener {
                    override fun onInfoWindowClick(marker: Marker) {
                        val stId: String = (marker.getTag() as Product).id
                        findNavController().navigate(MapFragmentDirections.navMapViewToNavProductDetails(stId))

//                        findNavController(view).navigate(
//                            MapFragmentDirections.navMapViewToNavProductDetails(
//                                stId
//                            )
//                        )
                    }
                })
                for (product in viewModel?.data?.getValue()!!) {
                    if (product.latitude != null) {
                        val currCoords = LatLng(product.latitude!!, product.longitude!!)
                        // Initialize marker options
                        val markerOptions = MarkerOptions()
                        // Set position of marker
                        markerOptions.position(currCoords)
                        // Set title of marker
                        markerOptions.title(product.title)
                        markerOptions.snippet(if (product.price != null) "price: " + product.price else "price unknown")

                        // Add marker on map
                        val currentMarker: Marker? = googleMap.addMarker(markerOptions)
                        if (currentMarker != null) {
                            currentMarker.setTag(product)
                        }
                    }
                }
            }
        })
        // Return view
        return view
    }

    @SuppressLint("MissingPermission")
    private fun relocateMap(googleMap: GoogleMap?) {
        if (context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
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
                mFusedLocationProviderClient = activity?.let {
                    LocationServices
                        .getFusedLocationProviderClient(it)
                }
                mFusedLocationProviderClient?.getCurrentLocation(
                    PRIORITY_HIGH_ACCURACY,
                    object : CancellationToken() {
                        override fun onCanceledRequested(p0: OnTokenCanceledListener): CancellationToken {
                            TODO("Not yet implemented")
                        }

                        override fun isCancellationRequested(): Boolean {
                            return false
                        }
                    })?.addOnSuccessListener { location ->
                    if (googleMap != null) {
                        googleMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    location.getLatitude(),
                                    location.getLongitude()
                                ),
                                11F
                            )
                        )
                    }
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
            if (grantResults.size > 0) {
                relocateMap(gmGoogleMap)
            }
        }
    }
}
