package com.example.firestoreproject

import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var titleText: EditText
    private lateinit var descirptionText: EditText
    private lateinit var textViewData: TextView
    private lateinit var saveButton: Button
    private lateinit var loadButton: Button
    private val dbStore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val KEY_TITLE = "title"
    private val KEY_DESCRIPTION = "description"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        titleText = findViewById(R.id.edit_title)
        descirptionText = findViewById(R.id.edit_description)
        saveButton = findViewById(R.id.button_save)
        textViewData = findViewById(R.id.text_view_data)
        loadButton = findViewById(R.id.button_load)

        saveButton.setOnClickListener {
           saveData()
        }

        loadButton.setOnClickListener {
            loadData()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    fun saveData() {
        val title = titleText.text.toString()
        val description = descirptionText.text.toString()
        val docData = mutableMapOf<String, Any>(
            KEY_TITLE to title,
            KEY_DESCRIPTION to description
        )
        dbStore.collection("Collection").document("First Document").set(docData).addOnSuccessListener {
            Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
        }
    }

    fun loadData(){
        dbStore.collection("Collection").document("First Document").get().addOnSuccessListener {
            if (it != null) {
                val title = it.getString(KEY_TITLE)
                val description = it.getString(KEY_DESCRIPTION)
                textViewData.text = "Title: $title\nDescription: $description"
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
        }
    }
}
