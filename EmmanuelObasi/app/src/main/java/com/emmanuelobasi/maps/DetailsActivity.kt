package com.emmanuelobasi.maps

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.emmanuelobasi.maps.databinding.ActivityDetailsBinding
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream

@Suppress("DEPRECATION")
class DetailsActivity : AppCompatActivity() {
    var index: Int = -1
    var type: String? = ""

    private lateinit var binding: ActivityDetailsBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        index = intent.getIntExtra("data", -1)

        val inputStream: InputStream = resources.openRawResource(R.raw.places)
        val json = inputStream.bufferedReader().use { it.readText() }


        val jsonArray = JSONArray(json)

        val a = jsonArray[index]

        Log.d(TAG, "got the data : $a")

        val jsonObject: JSONObject = a as JSONObject
        val latitude = jsonObject.getDouble("latitude")
        val longitude = jsonObject.getDouble("longitude")
        val place_id = jsonObject.getInt("place_type_id")
        val gaelic = jsonObject.getString("gaelic_name")
        val name = jsonObject.getString("name")


        val inputStream2: InputStream = resources.openRawResource(R.raw.place_types)
        val json2 = inputStream2.bufferedReader().use { it.readText() }

        val jsonArray2 = JSONArray(json2)

        for (i in 0 until jsonArray2.length()) {

            val jsonObject2: JSONObject = jsonArray2.getJSONObject(i)
            val p_id = jsonObject2.getInt("id")
            if (p_id == place_id) type = jsonObject2.getString("name")
        }

        if (gaelic.equals(null))
            binding.txtTitle.text = name
        else
            binding.txtTitle.text = "$name ($gaelic)"

        binding.txtGps.text = "Coordinates: $longitude , $latitude"
        binding.txtID.text = "Id# $place_id"
        binding.txtType.text = "Type $type"

        //show in full screen all details of the place
        // (id, name, Gaelic name, type, GPS coordinates)
        //Show the image of the place loaded from the internet 2
        //[Bonus] Show relevant images using any free web service,
        // or search engine
        //Show a back button; when pressed, go back to the map view

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

    }
}