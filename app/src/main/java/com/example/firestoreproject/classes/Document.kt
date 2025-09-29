package com.example.firestoreproject.classes

data class Document(
    val title: String,
    val description: String,
    val priority: Int,
    val tags: MutableMap<String, Boolean>
)
{
    constructor():this("","",1, mutableMapOf()) // needed for firestore
}