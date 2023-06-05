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
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InspectCardActivity : AppCompatActivity() {

    private var changedImage: Boolean = false
    private lateinit var card: Card

    private lateinit var btnCancel: ImageButton
    private lateinit var btnConfirm: ImageButton

    private lateinit var btnAddGalleryImage: ImageButton
    private lateinit var btnAddCameraImage: ImageButton
    private lateinit var btnRemoveImage: ImageButton

    private lateinit var cardFrontText: EditText
    private lateinit var cardBackText: EditText
    private lateinit var cardImage: ImageView
    private lateinit var inspectCardLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inspect_card)

        btnAddGalleryImage = findViewById(R.id.btn_inspectCardAddImage)
        //btnAddCameraImage = findViewById(R.id.btn_inspectCardCamera)
        btnRemoveImage = findViewById(R.id.btn_inspectCardRemoveImage)
        btnCancel = findViewById(R.id.btn_InspectCardCancel)
        btnConfirm = findViewById(R.id.btn_InspectConfirm)

        cardFrontText = findViewById(R.id.et_inspectCardFrontText)
        cardBackText = findViewById(R.id.et_inspectCardBackText)
        cardImage = findViewById(R.id.iv_inspectCardImage)
        inspectCardLayout = findViewById(R.id.ll_inspectCard)

        inspectCardLayout.visibility = View.GONE

        val cardId = intent.getIntExtra("CARD_ID", -1)
        val deckName = intent.getStringExtra("DECK_NAME")
        if (cardId < 0 || deckName == null || deckName.isEmpty()) {
            finish()
        }

        //OBTER DADOS DA CARTAS E TOGGLE VISIBILIDADE DO UI
        CoroutineScope(Dispatchers.IO).launch {
            try {
                card = DatabaseManager.getCard(cardId, deckName!!)
                withContext(Dispatchers.Main) {
                    cardFrontText.setText(card.frente)
                    cardBackText.setText(card.verso)
                    if (card.imagemLink.isNotEmpty()) {
                        Glide.with(this@InspectCardActivity)
                            .load(card.imagemLink)
                            .into(cardImage)
                    }
                    inspectCardLayout.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                Log.w("DATABASE", "Erro a ler dados da carta: $e")
                finish()
            }
        }

        val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if(uri != null) {
                cardImage.setImageURI(uri)
                changedImage = true
            }
        }
        btnAddGalleryImage.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        btnRemoveImage.setOnClickListener {
            cardImage.setImageDrawable(null)
            changedImage = true
        }

        /*btnAddCameraImage.setOnClickListener {
        }*/

        btnCancel.setOnClickListener {
            finish()
        }

        btnConfirm.setOnClickListener {
            val inputFrontText = cardFrontText.text.toString().trim()
            val inputBackText = cardBackText.text.toString().trim()

            card.frente = inputFrontText
            card.verso = inputBackText

            if (card.frente.isEmpty() || card.verso.isEmpty()) {
                Toast.makeText(this, getString(R.string.AvisoCamposVazios), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    if (changedImage) {
                        if (card.imagemRef.isNotEmpty()) {
                            Log.d("DATABASE", "Remover imagem antiga")
                            DatabaseManager.deleteImageFromStorage(card.imagemRef)
                        }
                        if (isImageViewNotEmpty()) {
                            val imageUri = getImageFromImageView() ?: throw Exception("Erro a processar imagem")
                            val result: Pair<String, String> = DatabaseManager.uploadImageToStorage(imageUri)
                            card.imagemRef = result.first
                            card.imagemLink = result.second
                        } else {
                            card.imagemRef = ""
                            card.imagemLink = ""
                        }
                    }
                    DatabaseManager.updateCard(card, deckName!!)
                    finish()
                } catch (e: Exception) {
                    Log.w("DATABASE", "Erro ao atualizar carta: $e")
                } catch (e: StorageException) {
                    Log.w("DATABASE", "Erro ao eliminar imagem anterior do Firebase Storage: $e")
                }
            }
        }
    }

    private fun isImageViewNotEmpty() = cardImage.drawable != null

    private fun getImageFromImageView(): Uri? {
        val imageBitmap: Bitmap = (cardImage.drawable as? BitmapDrawable)?.bitmap ?: return null

        val context = this.applicationContext
        val contentResolver: ContentResolver = context.contentResolver
        val imageName = "Image_${System.currentTimeMillis()}.jpeg"
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, imageName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        val collectionUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val imageUri = contentResolver.insert(collectionUri, values) ?: return null

        val outputStream = contentResolver.openOutputStream(imageUri)
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        outputStream?.close()
        return imageUri
    }

    //DEPRECATED: CASO CONSIGA COLOCAR A IMAGEM A FUNCIONAR
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