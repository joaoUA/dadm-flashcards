package com.example.mainactivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class AddNewCardActivity : AppCompatActivity() {

    private lateinit var btnConfirmAddNewCard: Button
    private lateinit var btnCancelAddNewCard: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_card)

        btnConfirmAddNewCard = findViewById(R.id.btn_AddNewCardConfirm)
        btnCancelAddNewCard = findViewById(R.id.btn_AddNewCardCancel)

    }
}