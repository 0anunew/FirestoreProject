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
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var titleText: EditText
    private lateinit var descriptionText: EditText
    private lateinit var textViewData: TextView
    private lateinit var addButton: AppCompatButton
    private lateinit var updateButton: AppCompatButton
    private lateinit var loadButton: AppCompatButton
    private lateinit var deleteButton: AppCompatButton
    private lateinit var deleteAllButton: AppCompatButton
    private val dbStoreRef: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val docCollectionRef: CollectionReference = dbStoreRef.collection("Collection")

    private var documentCounter = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        titleText = findViewById(R.id.edit_title)
        descriptionText = findViewById(R.id.edit_description)
        addButton = findViewById(R.id.button_add)
        updateButton = findViewById(R.id.button_update)
        textViewData = findViewById(R.id.text_view_data)
        loadButton = findViewById(R.id.button_load)
        deleteButton = findViewById(R.id.button_delete)
        deleteAllButton = findViewById(R.id.button_delete_all)

        titleText.addTextChangedListener(textWatcher)
        descriptionText.addTextChangedListener(textWatcher)

        addButton.isEnabled = false
        loadButton.isEnabled = false

        addButton.setOnClickListener {
            addNewDocumentData()
        }
        updateButton.setOnClickListener {
            updateDocumentData()
        }

        loadButton.setOnClickListener {
            getAllDocumentsAndShow()
        }
        deleteButton.setOnClickListener {
            deleteData()
        }
        deleteAllButton.setOnClickListener {
            deleteAllDocuments()
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
            addButton.isEnabled = inputtedUsername.isNotEmpty() && inputtedPassword.isNotEmpty()
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
            addButton.isEnabled = titleText.text.isNotEmpty() && descriptionText.text.isNotEmpty()
        }
    }

    override fun onStart() {
        super.onStart()
        disableButtonsAndRefresh()
        //this will remove the listener automatically when the activity is destroyed in onStop
        docCollectionRef.addSnapshotListener(this) { snapshot, dbError ->
            dbError?.let {
                return@addSnapshotListener
            }
            snapshot?.let {
                getAllDocumentsAndShow()
            }
        }
    }

    private fun enableButtons() {
        loadButton.isEnabled = true
        deleteButton.isEnabled = true
        updateButton.isEnabled = true
        deleteAllButton.isEnabled = true
    }

    private fun disableButtonsAndRefresh() {
        loadButton.isEnabled = false
        deleteButton.isEnabled = false
        updateButton.isEnabled = false
        textViewData.text = ""
        titleText.text.clear()
        descriptionText.text.clear()
        deleteAllButton.isEnabled = false
        documentCounter = 1
    }

    private fun addNewDocumentData() {
        val title = titleText.text.toString()
        val description = descriptionText.text.toString()
        val tagMap = mutableMapOf("tag1" to true, "tag2" to false, "tag3" to true)

        val docData = Document(title, description, documentCounter,tagMap)

        val pathString = "Document $documentCounter"
        val document = docCollectionRef.document(pathString)
        document.set(docData).addOnSuccessListener {
            documentCounter++
            Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Error in save", Toast.LENGTH_SHORT).show()
        }

//        docCollectionRef.add(docData).addOnSuccessListener {
//            Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show()
//        }.addOnSuccessListener {
//            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
//        }
        titleText.text.clear()
        descriptionText.text.clear()
        enableButtons()
        getAllDocumentsAndShow()
    }

    private fun updateDocumentData() {
        val title = titleText.text.toString()
        val description = descriptionText.text.toString()

        docCollectionRef.get().addOnSuccessListener { result ->
            val documents = result.documents
            if (documents.isNotEmpty()) {
                val lastDocument: DocumentSnapshot = documents[documents.size - 1]
                val tagsLastDocument: MutableMap<String, Boolean> = lastDocument.get("tags") as MutableMap<String, Boolean>
                val docData = Document(title, description, documentCounter,tagsLastDocument)
                lastDocument.reference.set(docData).addOnSuccessListener {
                    Toast.makeText(this, "Data updated", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getAllDocumentsAndShow() {
        docCollectionRef.orderBy("priority").get().addOnSuccessListener { resultQuerySnapshot ->

            val stringBuilder = StringBuilder()
            val documents = resultQuerySnapshot.documents
            for (document in documents) {
                val docData = document.toObject(Document::class.java)
                val title = docData?.title
                val description = docData?.description
                val priorityDoc = docData?.priority
                val tags = docData?.tags
                val loadText =
                    "Title: $title\nDescription: $description \nPriority: $priorityDoc\nTags: $tags\n\n"
                stringBuilder.append(loadText)
            }
            textViewData.text = stringBuilder.toString()
        }
    }

    private fun deleteAllDocuments() {
        docCollectionRef.get().addOnSuccessListener { resultQuerySnapshot ->
            val documents = resultQuerySnapshot.documents
            for (document in documents) {
                document.reference.delete()
            }
            Toast.makeText(this, "All data deleted", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
        }
        disableButtonsAndRefresh()
    }

    private fun deleteData() {
        docCollectionRef.get().addOnSuccessListener { resultQuerySnapshot ->
            val documents = resultQuerySnapshot.documents
            if (documents.isNotEmpty()) {
                val lastDocument = documents[documents.size - 1]
                lastDocument.reference.delete().addOnSuccessListener {
                    Toast.makeText(this, "Data deleted", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
        }
    }
}
