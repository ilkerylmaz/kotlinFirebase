package com.example.a02225076067_ilker_yilmaz

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation

class PostAdapter(private val posts: List<Post>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewName: TextView = view.findViewById(R.id.tvName)
        val textViewSurame: TextView = view.findViewById(R.id.tvSurname)
        val textViewBirth: TextView = view.findViewById(R.id.tvBirthView)
        val textViewDeath: TextView = view.findViewById(R.id.tvDeathView)
        val tvImageView: ImageView = view.findViewById(R.id.ivPostImage)
        val textViewBirthPlace: TextView = view.findViewById(R.id.tvDogumYeri)
        val textViewContribution: TextView = view.findViewById(R.id.tvContribution)
        val emailTextView: TextView = view.findViewById(R.id.tvEmail)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.textViewName.text = post.name
        holder.textViewSurame.text = post.surname
        holder.textViewBirth.text = post.birthDate
        holder.textViewDeath.text = post.deathDate
        holder.textViewBirthPlace.text = post.birthplace
        holder.textViewContribution.text = post.contribution
        holder.emailTextView.text = post.email
        Picasso.get()
            .load(post.imageUrl) // Firebase'den gelen URL
            .placeholder(R.drawable.loading7528256) // Yüklenme sırasında gösterilecek resim
            .error(R.drawable.errorimagegeneric) // Hata durumunda gösterilecek resim
            .transform(RoundedCornersTransformation(16, 0))
            .into(holder.tvImageView)

    }

    override fun getItemCount() = posts.size

}