package com.emmanuelobasi.maps

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.emmanuelobasi.maps.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.lang.Math.atan2
import java.lang.Math.cos
import java.lang.Math.sin
import java.lang.Math.sqrt


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, AdapterView.OnItemSelectedListener,
    GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerDragListener {
    private val latLngList = mutableListOf<LatLng>()
    var courses = arrayOf(
        "All", "County", "Barony", "Castle", "Seigniory"
    )
    private var index: Int = -1
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    val hashMap: HashMap<Int, String> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val spino = findViewById<Spinner>(R.id.coursesspinner)
        spino.onItemSelectedListener = this

        val ad: ArrayAdapter<*> = ArrayAdapter<Any?>(
            this, android.R.layout.simple_spinner_item, courses
        )

        spino.setSelection(0)

        ad.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )
        spino.adapter = ad;

    }

    private fun showAlertDialogue(markerTitle: String?) {
        val dialog = AlertDialog.Builder(this@MapsActivity)
        dialog.setTitle("$markerTitle")
        dialog.setPositiveButton("Get More Details") { _, _ ->

            startActivity(
                Intent(this@MapsActivity, DetailsActivity::class.java).putExtra(
                    "data", index
                )
            )
        }
        dialog.setNegativeButton("Cancel") { _, _ ->
            Toast.makeText(applicationContext, "Cancel Pressed", Toast.LENGTH_SHORT).show()
        }
        dialog.show()
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        displayCoordinatesFromJson()
        mMap.setOnMapLongClickListener(this)

        markerClickHandle(mMap)
    }

    private fun markerClickHandle(mMap: GoogleMap) {

        mMap.setOnMarkerClickListener { marker ->

            if (marker.isDraggable) {
                // Handle marker drag events here
                // You can differentiate between markers using their IDs or other properties
                // For example, if (clickedMarker.id == someId) { ... }
            }
            // Handle marker click event
            // You can access the clicked marker using the 'marker' parameter
            val markerTitle = marker.title

            for ((key, value) in hashMap) {
                if (value == markerTitle) {
                    index = key
                }
            }

            if (!markerTitle.equals("Long Press Marker")) showAlertDialogue(markerTitle)

            true
        }

    }

    private fun displayCoordinatesFromJson() {
        val inputStream: InputStream = resources.openRawResource(R.raw.places)
        val json = inputStream.bufferedReader().use { it.readText() }

        val jsonArray = JSONArray(json)

        for (i in 0 until jsonArray.length()) {
            val jsonObject: JSONObject = jsonArray.getJSONObject(i)
            val latitude = jsonObject.getDouble("latitude")
            val longitude = jsonObject.getDouble("longitude")
            val place_id = jsonObject.getInt("place_type_id")
            val gaelic = jsonObject.getString("gaelic_name")
            val name = jsonObject.getString("name")

            if (place_id in listOf(2, 6, 12, 13)) {
                val coordinate = LatLng(latitude, longitude)

                latLngList.add(coordinate)
                val markerOptions =
                    MarkerOptions().position(coordinate).title(name).icon(getMarkerIcon(place_id))

                hashMap[i] = name
                mMap.addMarker(markerOptions)
            }
        }

        Log.d(TAG, "displayCoordinatesFromJson: $hashMap")
        // Move the camera to the first coordinate
        val firstCoordinate: JSONObject = jsonArray.getJSONObject(0)
        val firstLatitude = firstCoordinate.getDouble("latitude")
        val firstLongitude = firstCoordinate.getDouble("longitude")
        val firstCoordinateLatLng = LatLng(firstLatitude, firstLongitude)

        val ireland = LatLng(53.301733, -8.067331)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ireland, 7f))
    }

    private fun getMarkerIcon(place_id: Int): BitmapDescriptor {
        return when (place_id) {
            2 -> BitmapDescriptorFactory.fromResource(R.drawable.black)
            6 -> BitmapDescriptorFactory.fromResource(R.drawable.blue_marker)
            12 -> BitmapDescriptorFactory.fromResource(R.drawable.pink)
            13 -> BitmapDescriptorFactory.fromResource(R.drawable.yello)
            else -> BitmapDescriptorFactory.defaultMarker()
        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        filterMarkers(p2)
    }

    private fun filterMarkers(p2: Int) {

        val inputStream: InputStream = resources.openRawResource(R.raw.places)
        val json = inputStream.bufferedReader().use { it.readText() }

        val jsonArray = JSONArray(json)

        val placeIdsToFilter = getPlaceIdsToFilter(p2)
        mMap.clear()
        for (i in 0 until jsonArray.length()) {
            val jsonObject: JSONObject = jsonArray.getJSONObject(i)
            val latitude = jsonObject.getDouble("latitude")
            val longitude = jsonObject.getDouble("longitude")
            val place_id = jsonObject.getInt("place_type_id")
            val gaelic = jsonObject.getString("gaelic_name")
            val name = jsonObject.getString("name")

            if (place_id in placeIdsToFilter) {
                val coordinate = LatLng(latitude, longitude)
                val markerOptions =
                    MarkerOptions().position(coordinate).title(name).icon(getMarkerIcon(place_id))

                hashMap[i] = name
                mMap.addMarker(markerOptions)
            }
        }

        Log.d(TAG, "displayCoordinatesFromJson: $hashMap")

        // Move the camera to the first coordinate
        val firstCoordinate: JSONObject = jsonArray.getJSONObject(0)
        val firstLatitude = firstCoordinate.getDouble("latitude")
        val firstLongitude = firstCoordinate.getDouble("longitude")
        val firstCoordinateLatLng = LatLng(firstLatitude, firstLongitude)

        val ireland = LatLng(53.301733, -8.067331)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ireland, 7f))

    }

    private fun getPlaceIdsToFilter(p2: Int): List<Int> {

        return when (courses[p2]) {
            "County" -> listOf(2)
            "Seigniory" -> listOf(13)
            "Barony" -> listOf(6)
            "Castle" -> listOf(12)
            "All" -> listOf(2, 6, 12, 13)
            else -> emptyList()
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }

    @SuppressLint("SetTextI18n")
    override fun onMapLongClick(longClickPos: LatLng) {
        val markerOptions = MarkerOptions().position(longClickPos).title("Long Press Marker")
        val marker: Marker? = mMap.addMarker(markerOptions)


        val radius = 10000 // radius in meters
        val circleOptions = CircleOptions().center(marker!!.position).radius(radius.toDouble())
            .strokeColor(Color.BLUE)
            .fillColor(Color.argb(70, 0, 0, 255)) // Adjust the fill color and opacity as desired

        mMap.addCircle(circleOptions)
        marker.isDraggable = true

        val nearest: LatLng? = findNearestLatLng(longClickPos, latLngList)
        drawPolyLine(nearest, longClickPos)

        binding.txtKM.text = "${Math.round(getDistanceBetweenLatLng(nearest!!, longClickPos))} KM"
        showPlacesInRadius(nearest, longClickPos)
    }

    private fun getDistanceBetweenLatLng(point1: LatLng, point2: LatLng): Double {
        val lat1 = point1.latitude
        val lon1 = point1.longitude
        val lat2 = point2.latitude
        val lon2 = point2.longitude

        val earthRadius = 6371 // Radius of the earth in kilometers

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a =
            sin(dLat / 2) * sin(dLat / 2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(
                dLon / 2
            ) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        val distance = earthRadius * c // Distance in kilometers

        return distance
    }

    private fun showPlacesInRadius(p0: LatLng, longClickPos: LatLng) {


        for (j in latLngList) {
            val distance = getDistanceBetweenLatLng(longClickPos, j)
            if (distance <= 10) {
                //get title from coordinates

                val inputStream: InputStream = resources.openRawResource(R.raw.places)
                val json = inputStream.bufferedReader().use { it.readText() }

                val jsonArray = JSONArray(json)

                for (i in 0 until jsonArray.length()) {
                    val jsonObject: JSONObject = jsonArray.getJSONObject(i)
                    val latitude = jsonObject.getDouble("latitude")
                    val longitude = jsonObject.getDouble("longitude")
                    val place_id = jsonObject.getInt("place_type_id")
                    val gaelic = jsonObject.getString("gaelic_name")
                    val name = jsonObject.getString("name")


                    val coordinate = LatLng(latitude, longitude)
                    if (coordinate == p0) {

                        val items = mutableListOf<String>()

                        items.add(name)
                        // Create an ArrayAdapter to populate the ListView
                        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)

                        // Set the adapter for the ListView
                        binding.listView.adapter = adapter

                        Log.d(TAG, "showPlacesInRadius: ${items.size}")
                    }


                }




            }
        }

    }

    private fun drawPolyLine(NearestLatLng: LatLng?, currentMarker: LatLng) {
        val list = mutableListOf<LatLng>()
        list.add(NearestLatLng!!)
        list.add(currentMarker)
        val polyOptions = PolylineOptions()
        polyOptions.color(Color.BLUE)
        polyOptions.width(6f)
        polyOptions.addAll(list)
        mMap.addPolyline(polyOptions)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(NearestLatLng, 9f))
    }


    private fun findNearestLatLng(targetLatLng: LatLng, latLngList: List<LatLng>): LatLng? {
        var nearestLatLng: LatLng? = null
        var shortestDistance = Double.MAX_VALUE

        for (latLng in latLngList) {
            val distance = calculateDistance(targetLatLng, latLng)
            if (distance < shortestDistance) {
                shortestDistance = distance
                nearestLatLng = latLng
            }
        }

        return nearestLatLng
    }

    private fun calculateDistance(latLng1: LatLng, latLng2: LatLng): Double {
        val earthRadius = 6371 // in kilometers

        val latDiff = Math.toRadians(latLng2.latitude - latLng1.latitude)
        val lngDiff = Math.toRadians(latLng2.longitude - latLng1.longitude)

        val a =
            sin(latDiff / 2) * kotlin.math.sin(latDiff / 2) + cos(Math.toRadians(latLng1.latitude)) * cos(
                Math.toRadians(latLng2.latitude)
            ) * sin(lngDiff / 2) * kotlin.math.sin(lngDiff / 2)

        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))

        return earthRadius * c
    }


    override fun onMarkerDrag(p0: Marker) {
        updateInfoOnDrag(p0)
    }

    override fun onMarkerDragEnd(p0: Marker) {

    }

    @SuppressLint("SetTextI18n")
    private fun updateInfoOnDrag(marker: Marker) {
        val radius = 10000 // radius in meters
        val circleOptions = CircleOptions().center(marker.position).radius(radius.toDouble())
            .strokeColor(Color.BLUE)
            .fillColor(Color.argb(70, 0, 0, 255)) // Adjust the fill color and opacity as desired

        mMap.addCircle(circleOptions)
        marker.isDraggable = true

        ///////////


        val nearest: LatLng? = findNearestLatLng(marker.position, latLngList)
        drawPolyLine(nearest, marker.position)
        binding.txtKM.text =
            "${Math.round(getDistanceBetweenLatLng(marker.position!!, nearest!!))} KM"

//        showPlacesInRadius(marker.position, longClickPos)

    }


    override fun onMarkerDragStart(p0: Marker) {
        TODO("Not yet implemented")
    }
}