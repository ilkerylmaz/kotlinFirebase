package com.example.a02225076067_ilker_yilmaz

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import java.util.Calendar

class AddPostActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private var selectedImageUri: Uri? = null
    private lateinit var ivSelectedImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        // Firebase bağlantıları
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        // Layout bileşenlerini bağlama
        ivSelectedImage = findViewById(R.id.ivSelectedImage)
        val etName = findViewById<EditText>(R.id.etName)
        val etSurname = findViewById<EditText>(R.id.etSurname)
        val etBirthplace = findViewById<EditText>(R.id.etBirthplace)
        val etBirthDate = findViewById<EditText>(R.id.etBirthDate)
        val etDeathDate = findViewById<EditText>(R.id.etDeathDate)
        val etContribution = findViewById<EditText>(R.id.etContribution)
        val btnSubmit = findViewById<TextView>(R.id.btnSubmit)

        // Tarih girişlerini kontrol eden TextWatcher ekleme
        etBirthDate.addTextChangedListener(DateInputWatcher(etBirthDate))
        etDeathDate.addTextChangedListener(DateInputWatcher(etDeathDate))

        // ImageView'e tıklayınca galeri açılır
        ivSelectedImage.setOnClickListener { openGalleryWithPermission(it) }

        // Gönderi butonuna tıklama
        btnSubmit.setOnClickListener {
            val name = etName.text.toString().trim()
            val surname = etSurname.text.toString().trim()
            val birthplace = etBirthplace.text.toString().trim()
            val birthDate = etBirthDate.text.toString().trim()
            val deathDate = etDeathDate.text.toString().trim()
            val contribution = etContribution.text.toString().trim()

            if (name.isEmpty() || surname.isEmpty() || birthplace.isEmpty() ||
                birthDate.isEmpty() || deathDate.isEmpty() || contribution.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Resim seçilmişse yükleme ve kaydetme
            if (selectedImageUri != null) {
                uploadImageToFirebase(name, surname, birthplace, birthDate, deathDate, contribution)
            } else {
                Toast.makeText(this, "Lütfen bir resim seçin", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openGalleryWithPermission(view: android.view.View) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openGallery()
        } else {
            if (shouldShowRequestPermissionRationale(permission)) {
                Snackbar.make(view, "Galeriye erişim izni gerekiyor", Snackbar.LENGTH_INDEFINITE)
                    .setAction("İzin Ver") {
                        requestPermissionLauncher.launch(permission)
                    }.show()
            } else {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun uploadImageToFirebase(
        name: String,
        surname: String,
        birthplace: String,
        birthDate: String,
        deathDate: String,
        contribution: String
    ) {
        val filename = UUID.randomUUID().toString()
        val storageRef = storage.reference.child("images/$filename")

        storageRef.putFile(selectedImageUri!!).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                savePostToFirestore(name, surname, birthplace, birthDate, deathDate, contribution, uri.toString())
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Resim yüklenemedi: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun savePostToFirestore(
        name: String,
        surname: String,
        birthplace: String,
        birthDate: String,
        deathDate: String,
        contribution: String,
        imageUrl: String
    ) {
        val currentUser = auth.currentUser
        val email = currentUser?.email ?: "Anonymous"

        val post = hashMapOf(
            "name" to name,
            "surname" to surname,
            "birthplace" to birthplace,
            "birthDate" to birthDate,
            "deathDate" to deathDate,
            "contribution" to contribution,
            "imageUrl" to imageUrl,
            "email" to email, // Göndericinin e-posta adresi
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("Posts").add(post).addOnSuccessListener {
            Toast.makeText(this, "Gönderi başarıyla kaydedildi", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, SecondPageActivity::class.java))
        }.addOnFailureListener {
            Toast.makeText(this, "Gönderi kaydedilirken hata oluştu: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let {
                    selectedImageUri = it
                    ivSelectedImage.setImageURI(it)
                }
            }
        }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openGallery()
            } else {
                Toast.makeText(this, "Galeriye erişim izni reddedildi", Toast.LENGTH_SHORT).show()
            }
        }

    // Tarih girişlerini kontrol eden TextWatcher
    // Tarih girişlerini kontrol eden TextWatcher


    class DateInputWatcher(private val editText: EditText) : TextWatcher {
        private var isUpdating = false
        private var previousText = "" // Önceki geçerli metni saklar
        private val currentYear = Calendar.getInstance().get(Calendar.YEAR) // Günümüz yılı

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            if (isUpdating) return

            val text = s.toString().replace("/", "") // Eski '/' karakterlerini kaldır
            if (text.length > 8) {
                // Geçersiz uzunlukta giriş
                showToast("Tarih en fazla 8 karakter olmalı (gg/aa/yyyy)")
                revertText(s)
                return
            }

            var day = 0
            var month = 0
            var year = 0

            if (text.length >= 2) {
                day = text.substring(0, 2).toIntOrNull() ?: 0
                if (day !in 1..31) {
                    // Geçersiz gün
                    showToast("Geçersiz gün (1-31 arasında olmalı)")
                    revertText(s)
                    return
                }
            }

            if (text.length >= 4) {
                month = text.substring(2, 4).toIntOrNull() ?: 0
                if (month !in 1..12) {
                    // Geçersiz ay
                    showToast("Geçersiz ay (1-12 arasında olmalı)")
                    revertText(s)
                    return
                }
            }

            if (text.length == 8) {
                year = text.substring(4, 8).toIntOrNull() ?: 0
                if (year > currentYear) {
                    // Geçersiz yıl
                    showToast("Geçersiz yıl (günümüz yılından büyük olamaz)")
                    revertText(s)
                    return
                }
            }

            // Tarih formatını güncelle (gg/aa/yyyy)
            val formatted = StringBuilder()
            for (i in text.indices) {
                formatted.append(text[i])
                if ((i == 1 || i == 3) && i != text.length - 1) {
                    formatted.append("/") // Doğru yere '/' ekle
                }
            }

            isUpdating = true
            previousText = formatted.toString() // Geçerli metni sakla
            s?.replace(0, s.length, formatted.toString()) // Girişi formatla
            isUpdating = false
        }

        private fun revertText(s: Editable?) {
            isUpdating = true
            s?.replace(0, s.length, previousText) // Metni eski haline getir
            isUpdating = false
        }

        private fun showToast(message: String) {
            Toast.makeText(editText.context, message, Toast.LENGTH_SHORT).show()
        }
    }



}
