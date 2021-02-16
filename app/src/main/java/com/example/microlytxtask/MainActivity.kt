package com.example.microlytxtask

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.microlytxtask.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.jaredrummler.android.device.DeviceName


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    lateinit var viewModel: InfoViewModel

    private lateinit var lm: LocationManager

    private lateinit var fusedLocationClient: FusedLocationProviderClient


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel = ViewModelFactory(application).create(InfoViewModel::class.java)


        binding.viewmodel = viewModel
        binding.lifecycleOwner = this


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        lm = application.getSystemService(LOCATION_SERVICE) as LocationManager

        DeviceName.init(this)

        viewModel._info.observe(this, Observer {
            binding.ispTv.text = "Isp: ${it.isp}"
            binding.ipAdTv.text = "IP/Query Address: ${it.query}"
            binding.cityTv.text = "City: ${it.city}"
            binding.countryCodeTv.text = "CountryCode: ${it.countryCode}"
        })

        viewModel._error.observe(this, Observer {
            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
        })


        var lon = ""
        var lat = ""

        DeviceName.with(this).request { info, _ ->
            val manufacturer = info.manufacturer
            val model = info.model

            binding.modelTv.text = " Phone Model: $model"
            binding.handsetMakeTv.text = " Phone Make: $manufacturer"
        }

        if (ActivityCompat.checkSelfPermission(
                application,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                application,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ), 101
                )
            }

        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations this can be null.
                when {
                    location != null -> {
                        lon = location.longitude.toString()
                        lat = location.latitude.toString()
                        binding.latlonTv.text = "Latitude and Longitude: $lat $lon"

                    }
                }
            }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            101 -> {
                // If request is cancelled, the result arrays are empty.
                if (!(grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    Toast.makeText(this, "We need your location to provide info", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }


}