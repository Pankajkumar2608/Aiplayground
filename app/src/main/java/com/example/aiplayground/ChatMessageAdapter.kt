package com.example.aiplayground

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatMessageAdapter(private val messageList: List<ChatMessage>) :
    RecyclerView.Adapter<ChatMessageAdapter.ChatMessageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatMessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return ChatMessageViewHolder(view)
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: ChatMessageViewHolder, position: Int) {
        val message = messageList[position]
        if (message.image != null) {
            holder.selectedImageView.setImageBitmap(message.image)
            holder.selectedImageView.visibility = View.VISIBLE

        } else {
            holder.selectedImageView.visibility = View.GONE
        }

        if (message.ai) {
            holder.aiTextView.text = message.user// Assuming 'user' holds the message content
            holder.aiTextView.visibility = View.VISIBLE
            holder.userTextView.visibility = View.GONE
        } else {
            holder.userTextView.text = message.user // Assuming 'user' holds the message content
            holder.userTextView.visibility = View.VISIBLE
            holder.aiTextView.visibility = View.GONE
        }

    }

    inner class ChatMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val selectedImageView: ImageView = itemView.findViewById(R.id.BSelectImage)
        val userTextView: TextView = itemView.findViewById(R.id.userMessage)
        val aiTextView: TextView = itemView.findViewById(R.id.aiMessage)

    }
}