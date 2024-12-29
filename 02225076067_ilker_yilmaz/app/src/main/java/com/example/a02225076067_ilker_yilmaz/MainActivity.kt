package com.example.a02225076067_ilker_yilmaz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Firebase Authentication tanımlaması
        auth = FirebaseAuth.getInstance()

        // Firestore tanımlaması
        val db = Firebase.firestore

        // Layout bileşenlerini bağla
        val email = findViewById<EditText>(R.id.kullaniciEmail)
        val password = findViewById<EditText>(R.id.kullaniciSifresi)
        val kayitButon = findViewById<TextView>(R.id.butonKayit)
        val girisButon = findViewById<Button>(R.id.butonGiris)

        // Oturum kontrolü: Kullanıcı oturum açmışsa, SecondPageActivity'ye yönlendir
        if (auth.currentUser != null) {
            startActivity(Intent(this, SecondPageActivity::class.java))
            finish()
        }

        // Kayıt butonuna tıklanma işlemi
        kayitButon.setOnClickListener {
            val emailText = email.text.toString().trim()
            val passwordText = password.text.toString().trim()

            if (emailText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (passwordText.length < 6) {
                Toast.makeText(this, "Şifre en az 6 karakter olmalıdır", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(emailText, passwordText)
                .addOnSuccessListener {
                    Toast.makeText(this, "Kayıt başarılı", Toast.LENGTH_LONG).show()
                    // Kullanıcı bilgilerini Firestore'a kaydet
                    val user = hashMapOf(
                        "email" to emailText,
                        "userId" to auth.currentUser?.uid
                    )
                    db.collection("Users").document(auth.currentUser!!.uid).set(user)
                        .addOnSuccessListener {
                            // SecondPageActivity'ye yönlendir
                            startActivity(Intent(this, SecondPageActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Kayıt sırasında hata: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Kayıt başarısız: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                }
        }

        // Giriş butonuna tıklanma işlemi
        girisButon.setOnClickListener {
            val emailText = email.text.toString().trim()
            val passwordText = password.text.toString().trim()

            if (emailText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(emailText, passwordText)
                .addOnSuccessListener {
                    Toast.makeText(this, "Giriş başarılı", Toast.LENGTH_LONG).show()
                    // SecondPageActivity'ye yönlendir
                    startActivity(Intent(this, SecondPageActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Giriş başarısız: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                }
        }
    }
}
