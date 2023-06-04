package com.example.mainactivity

data class Card(
    var id: Int,
    var frente: String,
    var verso: String,
    var estudada: Boolean,
    var imagemLink: String,
    var imagemRef: String
)