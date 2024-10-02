package com.example.fotografpaylasma.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.fotografpaylasma.R
import com.example.fotografpaylasma.databinding.RecyclerRowBinding
import com.example.fotografpaylasma.model.Post
import com.squareup.picasso.Picasso

class FeedRecyclerAdapter(private val postList : ArrayList<Post>) : RecyclerView.Adapter<FeedRecyclerAdapter.PostHolder>() {

    class PostHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostHolder(binding)
    }

    override fun getItemCount(): Int {
        return postList.size

    }

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        val imageUrl = postList[position].downloadUrl


        holder.binding.recyclerEmailText.text = postList.get(position).email
        holder.binding.recyclerCommentText.text = postList.get(position).comment
        Picasso.get().load(postList[position].downloadUrl).into(holder.binding.recyclerImageView)
        if (!imageUrl.isNullOrEmpty()) {
            // Eğer URL boş değilse Picasso ile yükleme yap
            Picasso.get().load(imageUrl).into(holder.binding.recyclerImageView)
        } else {
            // URL boşsa, bir placeholder resim yükle
            holder.binding.recyclerImageView.setImageResource(R.drawable.placeholder_image)
        }
    }




    }


