package com.example.a02225076067_ilker_yilmaz

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SecondPageActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second_page)

        //toolbarin actionbar olarak ayarlanmasi
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // RecyclerView'i yapılandır
        setupRecyclerView()

        // Firestore'dan gönderileri çek
        fetchPosts()


    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun fetchPosts() {
        firestore.collection("Posts").get()
            .addOnSuccessListener { result ->
                val posts = result.documents.mapNotNull { doc ->
                    try {
                        Post(
                            imageUrl = doc.getString("imageUrl") ?: "",
                            name = doc.getString("name") ?: "",
                            surname = doc.getString("surname") ?: "",
                            birthplace = doc.getString("birthplace") ?: "",
                            birthDate = doc.getString("birthDate") ?: "",
                            deathDate = doc.getString("deathDate") ?: "",
                            contribution = doc.getString("contribution") ?: "",
                            email = doc.getString("email") ?: "Anonymous" // E-posta alanını ekle
                        )
                    } catch (e: Exception) {
                        null // Hatalı belgeyi atla
                    }
                }
                recyclerView.adapter = PostAdapter(posts)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Gönderiler alınamadı: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        //menu tasarim islemleri
        return true

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_add_post -> {
                // Gönderi ekleme ekranına git
                startActivity(Intent(this, AddPostActivity::class.java))
                true
            }
            R.id.menu_logout -> {
                // Çıkış yap
                auth.signOut()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
