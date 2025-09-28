package com.example.firestoreproject.classes

data class Document(val title: String,val description: String){
    constructor():this("","") // needed for firestore
}