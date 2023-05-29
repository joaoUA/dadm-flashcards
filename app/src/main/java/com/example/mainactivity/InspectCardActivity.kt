package com.example.mainactivity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore

class InspectCardActivity : AppCompatActivity() {

    private lateinit var card: Card
    private lateinit var db: FirebaseFirestore

    private lateinit var cancelBtn: Button
    private lateinit var confirmBtn: Button
    private lateinit var addImageBtn: Button
    private lateinit var addCameraBtn: Button
    private lateinit var cardFrontText: EditText
    private lateinit var cardBackText: EditText
    private lateinit var cardImage: ImageView
    private lateinit var inspectCardLayout: LinearLayout

    private val PERMISSION_REQUEST_CAMERA = 1
    private val PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inspect_card)

        addImageBtn = findViewById(R.id.btn_inspectCardAddImage)
        addCameraBtn = findViewById(R.id.btn_inspectCardCamera)
        cancelBtn = findViewById(R.id.btn_InspectCardCancel)
        confirmBtn = findViewById(R.id.btn_InspectConfirm)
        cardFrontText = findViewById(R.id.et_inspectCardFrontText)
        cardBackText = findViewById(R.id.et_inspectCardBackText)
        cardImage = findViewById(R.id.iv_inspectCardImage)
        inspectCardLayout = findViewById(R.id.ll_inspectCard)

        inspectCardLayout.visibility = View.GONE

        val cardID = intent.getStringExtra("CARD_ID")
        if (cardID == null || cardID.isEmpty()) {
            finish()
        }

        val pickImageFromGalleryContract = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                cardImage.setImageURI(uri)
            }
        }
        val takePictureContract =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val imageUri: Uri? = result.data?.data
                    imageUri?.let {
                        cardImage.setImageURI(imageUri)
                    }
                }
            }

        db = DatabaseManager.getDatabase()
        try {
            db.collection("cartas")
                .document(cardID!!)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if(!documentSnapshot.exists())
                        throw Exception("Document does not exist: $documentSnapshot")
                    val data = documentSnapshot.data ?: throw Exception("Couldn't load data from document")
                    val cardDataID = documentSnapshot.id
                    val cardFront = data["frente"] as String
                    val cardBack = data["verso"] as String
                    val cardStudied = data["estudada"] as Boolean
                    val cardImage = data["imagem"] as String
                    card = Card(cardDataID, cardFront, cardBack, cardStudied, cardImage)

                    runOnUiThread {
                        cardFrontText.setText(card.frente)
                        cardBackText.setText(card.verso)
                        inspectCardLayout.visibility = View.VISIBLE
                    }
                }
                .addOnFailureListener {
                    throw Exception("Couldn't get card document")
                }
        } catch (e: Exception) {
            Log.d("Card", "$e")
        }

        addImageBtn.setOnClickListener {
            pickImageFromGalleryContract.launch("image/*")
        }
        addCameraBtn.setOnClickListener {
            val permissions = arrayOf(android.Manifest.permission.CAMERA)
            if (checkPermissions(permissions)) {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                takePictureContract.launch(intent)
            } else {
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CAMERA)
            }
        }
        cancelBtn.setOnClickListener {
            finish()
        }
        confirmBtn.setOnClickListener {
            if(cardID == null)
                return@setOnClickListener

            card.frente = cardFrontText.text.toString().trim()
            card.verso = cardBackText.text.toString().trim()

            if(card.frente.isEmpty() || card.verso.isEmpty()) {
                Toast.makeText(this, "Can't have empty fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val newCardData = hashMapOf(
                "frente" to card.frente,
                "verso" to card.verso,
                "imagem" to card.imagem,
                "estudada" to card.estudada
            )
            try {
                db.collection("cartas").document(cardID)
                    .set(newCardData)
                    .addOnSuccessListener {
                        Log.d("Card", "DocumentSnapshot successfully written!")
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Log.w("Card", "Error writing document", e)
                    }
            } catch (e: Exception) {
                Log.d("Card", "$e")
            }
        }

    }

    private fun checkPermissions(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }
}