package com.example.mainactivity

import android.content.ContentResolver
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class InspectCardActivity : AppCompatActivity() {

    private lateinit var card: Card
    private lateinit var db: FirebaseFirestore

    private lateinit var cancelBtn: Button
    private lateinit var confirmBtn: Button

    private lateinit var addImageBtn: ImageButton
    private lateinit var addCameraBtn: ImageButton
    private lateinit var removeImageBtn: ImageButton

    private lateinit var cardFrontText: EditText
    private lateinit var cardBackText: EditText
    private lateinit var cardImage: ImageView
    private lateinit var inspectCardLayout: LinearLayout

    private val REQUEST_IMAGE_CAPTURE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inspect_card)

        addImageBtn = findViewById(R.id.btn_inspectCardAddImage)
        addCameraBtn = findViewById(R.id.btn_inspectCardCamera)
        removeImageBtn = findViewById(R.id.btn_inspectCardRemoveImage)
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

        db = DatabaseManager.getDatabase()
        try {
            db.collection("cartas")
                .document(cardID!!)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if(!documentSnapshot.exists()) {
                        throw Exception("Document does not exist: $documentSnapshot")
                    }
                    val data = documentSnapshot.data ?: throw Exception("Couldn't load data from document")
                    val cardDataId = documentSnapshot.id
                    val cardDataFront = data["frente"] as String
                    val cardDataBack = data["verso"] as String
                    val cardDataStudied = data["estudada"] as Boolean
                    val cardDataImage = data["imagem"] as String
                    card = Card(
                        id=cardDataId,
                        frente=cardDataFront,
                        verso=cardDataBack,
                        estudada=cardDataStudied,
                        imagem=cardDataImage
                    )
                    runOnUiThread {
                        if (cardDataImage.isNotEmpty()) {
                            Glide.with(this)
                                .load(cardDataImage)
                                .into(cardImage)
                           }
                        cardFrontText.setText(card.frente)
                        cardBackText.setText(card.verso)
                        inspectCardLayout.visibility = View.VISIBLE
                    }
                }
                .addOnFailureListener {
                    throw it
                }
        } catch (e: Exception) {
            Log.d("CARD", "$e")
            finish()
        }

        val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if(uri != null) {
                cardImage.setImageURI(uri)
            }
        }
        addImageBtn.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        removeImageBtn.setOnClickListener {
            cardImage.setImageResource(R.drawable.ic_launcher_background)
        }

        addCameraBtn.setOnClickListener {

        }

        cancelBtn.setOnClickListener {
            finish()
        }

        //todo: confirmar com imagem vazia
        confirmBtn.setOnClickListener {
            if(card.frente.isEmpty() || card.verso.isEmpty()) {
                Toast.makeText(this, "Can't have empty fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var imageIdentifier = ""
            //GET IMAGE FROM IMAGEVIEW
            val imageBitmap: Bitmap? = (cardImage.drawable as? BitmapDrawable)?.bitmap
            val imageUri: Uri = imageBitmap?.let { bitmap ->
                val context = this.applicationContext
                val contentResolver: ContentResolver = context.contentResolver
                val imageName = "Image_${System.currentTimeMillis()}.jpeg"
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, imageName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                }
                val collectionUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                val imageUri = contentResolver.insert(collectionUri, values)
                imageUri?.let { uri ->
                    val outputStream = contentResolver.openOutputStream(uri)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                    outputStream?.close()
                }
                imageUri
            } ?: return@setOnClickListener

            //UPLOAD IMAGE TO FIREBASE
            val imageName = DatabaseManager.generateUniqueName()
            val imageRef = FirebaseStorage.getInstance().getReference("imagens/$imageName")
            imageRef.putFile(imageUri)
                .addOnSuccessListener {
                    Log.d("IMAGEM", "REF: $imageName")
                    val uriTask = it.storage.downloadUrl
                    while( !uriTask.isSuccessful);
                    imageIdentifier = uriTask.result.toString()
                    Toast.makeText(this, "Image uploaded with success", Toast.LENGTH_SHORT).show()

                    //UPDATE CARD ON FIREBASE
                    card.frente = cardFrontText.text.toString().trim()
                    card.verso = cardBackText.text.toString().trim()
                    card.imagem = imageIdentifier

                    val updatedCard = hashMapOf(
                        "frente" to card.frente,
                        "verso" to card.verso,
                        "imagem" to card.imagem,
                        "estudada" to card.estudada
                    )
                    try {
                        db.collection("cartas")
                            .document(cardID!!)
                            .set(updatedCard)
                            .addOnSuccessListener {
                                Log.d("CARD", "Documento no Firebase atualizado!")
                                finish()
                            }
                            .addOnFailureListener {
                                Log.d("CARD", "Documento no Firebase NÃO atualizado com sucesso")
                            }
                    } catch (e: Exception) {
                        Log.d("Card", "$e")
                    }

                }
                .addOnFailureListener {
                    Log.w("STORAGE", "Failed Upload: $it")
                    Toast.makeText(this, "Image couldn't be uploaded", Toast.LENGTH_SHORT).show()
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
    private fun getImageUri(bitmap: Bitmap): Uri {
        val imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val imageDetails = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Title")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }

        val contentResolver = applicationContext.contentResolver
        val uri = contentResolver.insert(imageCollection, imageDetails)

        uri?.let { imageUri ->
            contentResolver.openOutputStream(imageUri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
        }
        return uri ?: Uri.EMPTY
    }
}