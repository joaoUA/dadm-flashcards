package com.example.mainactivity

import android.content.ContentResolver
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddNewCardActivity : AppCompatActivity() {

    private lateinit var btnConfirmAddNewCard: ImageButton
    private lateinit var btnCancelAddNewCard: ImageButton

    private lateinit var btnAddGalleryImage: ImageButton
    private lateinit var btnAddCameraImage: ImageButton
    private lateinit var btnRemoveImage: ImageButton

    private lateinit var cardFrontText: EditText
    private lateinit var cardBackText: EditText
    private lateinit var cardImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_card)

        btnAddGalleryImage = findViewById(R.id.btn_inspectCardAddImage)
        //btnAddCameraImage = findViewById(R.id.btn_inspectCardCamera)
        btnRemoveImage = findViewById(R.id.btn_inspectCardRemoveImage)
        btnConfirmAddNewCard = findViewById(R.id.btn_AddNewCardConfirm)
        btnCancelAddNewCard = findViewById(R.id.btn_AddNewCardCancel)

        cardFrontText = findViewById(R.id.et_inspectCardFrontText)
        cardBackText = findViewById(R.id.et_inspectCardBackText)
        cardImage = findViewById(R.id.iv_inspectCardImage)

        val deckName = intent.getStringExtra("DECK_NAME")

        if (deckName == null || deckName.isEmpty())
            finish()

        val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if(uri != null) {
                cardImage.setImageURI(uri)
            }
        }
        btnAddGalleryImage.setOnClickListener {
            val galleryPermission = android.Manifest.permission.READ_EXTERNAL_STORAGE
            val neededPermissions = arrayOf(galleryPermission)
            val hasPermissions = checkPermissions(neededPermissions)
            Log.d("PERMISSIONS", "$hasPermissions")
            if(!hasPermissions) {
                ActivityCompat.requestPermissions(this, neededPermissions,
                    GALLERY_PERMISSION_REQUEST_CODE
                )
            } else {
                galleryLauncher.launch("image/*")
            }
        }
        btnRemoveImage.setOnClickListener {
            cardImage.setImageDrawable(null)
        }

        btnConfirmAddNewCard.setOnClickListener {
            val frontText = cardFrontText.text.toString().trim()
            val backText = cardBackText.text.toString().trim()

            if (frontText.isEmpty() || backText.isEmpty()) {
                Toast.makeText(this, getString(R.string.AvisoCamposVazios), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val card = Card(
                id = -1,
                frente = frontText,
                verso = backText,
                estudada = false,
                imagemLink = "",
                imagemRef = ""
            )

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    if (isImageViewNotEmpty()) {
                        val imageUri = getImageFromImageView() ?: throw Exception("Erro a processar imagem")
                        val result: Pair<String, String> = DatabaseManager.uploadImageToStorage(imageUri)
                        card.imagemRef = result.first
                        card.imagemLink = result.second
                    }
                    DatabaseManager.addNewCard(card, deckName!!)
                    finish()
                } catch (e: Exception) {
                Log.w("DATABASE", "Erro ao atualizar carta: $e")
                } catch (e: StorageException) {
                    Log.w("DATABASE", "Erro ao eliminar imagem anterior do Firebase Storage: $e")
                }
            }
        }

        btnCancelAddNewCard.setOnClickListener {
            finish()
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

    private fun checkPermissions(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    companion object {
        private const val GALLERY_PERMISSION_REQUEST_CODE = 100
    }
}