package com.example.firestoreproject

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.firestoreproject.classes.Document
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var titleText: EditText
    private lateinit var descriptionText: EditText
    private lateinit var textViewData: TextView
    private lateinit var saveButton: AppCompatButton
    private lateinit var loadButton: AppCompatButton
    private lateinit var deleteButton: AppCompatButton
    private val dbStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val docRef: DocumentReference =
        dbStore.collection("Collection").document("First Document")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        titleText = findViewById(R.id.edit_title)
        descriptionText = findViewById(R.id.edit_description)
        saveButton = findViewById(R.id.button_save)
        textViewData = findViewById(R.id.text_view_data)
        loadButton = findViewById(R.id.button_load)
        deleteButton = findViewById(R.id.button_delete)

        titleText.addTextChangedListener(textWatcher)
        descriptionText.addTextChangedListener(textWatcher)

        saveButton.isEnabled = false
        loadButton.isEnabled = false

        saveButton.setOnClickListener {
            saveData()
        }

        loadButton.setOnClickListener {
            loadData()
        }
        deleteButton.setOnClickListener {
            deleteData()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            val inputtedUsername = titleText.text.toString().trim()
            val inputtedPassword = descriptionText.text.toString().trim()
            saveButton.isEnabled = inputtedUsername.isNotEmpty() && inputtedPassword.isNotEmpty()
        }

        override fun beforeTextChanged(
            s: CharSequence?,
            start: Int,
            count: Int,
            after: Int
        ) {
        }

        override fun onTextChanged(
            s: CharSequence?,
            start: Int,
            before: Int,
            count: Int
        ) {
            saveButton.isEnabled = titleText.text.isNotEmpty() && descriptionText.text.isNotEmpty()
        }
    }

    override fun onStart() {
        super.onStart()
        //this will remove the listener automatically when the activity is destroyed in onStop
        docRef.addSnapshotListener(this) { snapshot, dbError ->
            dbError?.let {
                return@addSnapshotListener
            }
            snapshot?.let {
                if (it.exists()) {
                    loadData()
                }
            }
        }
    }

    fun enableButtons() {
        loadButton.isEnabled = true
        deleteButton.isEnabled = true
    }

    fun disableButtons() {
        loadButton.isEnabled = false
        deleteButton.isEnabled = false
    }

    fun saveData() {
        val title = titleText.text.toString()
        val description = descriptionText.text.toString()

        val docData = Document(title,description)

        docRef.set(docData).addOnSuccessListener {
            Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show()
            enableButtons()
        }.addOnFailureListener {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
        }
    }

    fun loadData() {
        docRef.get().addOnSuccessListener {
            if (it != null) {
                val docData = it.toObject(Document::class.java)
                val title = docData?.title
                val description = docData?.description

                val loadText = "Title: $title\nDescription: $description"
                textViewData.text = loadText
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            disableButtons()
        }
    }

    fun deleteData() {
        docRef.get().addOnSuccessListener {
            if (it != null) {
                docRef.delete().addOnSuccessListener {
                    Toast.makeText(this, "Data deleted", Toast.LENGTH_SHORT).show()
                    disableButtons()
                    textViewData.text = ""
                }.addOnFailureListener {
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
