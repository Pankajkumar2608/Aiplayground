package com.example.aiplayground

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.text.Spanned
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import io.noties.markwon.Markwon


class ChatWithAi : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var chatMessageAdapter: ChatMessageAdapter
    private lateinit var messageList: MutableList<ChatMessage>
    private lateinit var chat: Chat
    private lateinit var image: ImageView
    private lateinit var imageButton: ImageButton
    private var selectedImageBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_with_ai)
        image = findViewById(R.id.IVPreviewImage)
        imageButton = findViewById(R.id.BSelectImage)
        val sendMessage = findViewById<EditText>(R.id.enterMessage)
        val send = findViewById<ImageButton>(R.id.send)
        val aiModels = findViewById<Spinner>(R.id.aiModel)
        recyclerView = findViewById(R.id.recyclerview)

        // Load your API key securely (e.g., from environment variables)
        val apiKey = "api_key"
        ArrayAdapter.createFromResource(
            this, R.array.ai_model,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
            aiModels.adapter = adapter
        }
        class SpinnerActivity : Activity(), AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                // An item is selected. You can retrieve the selected item using
                parent.getItemAtPosition(pos)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                Toast.makeText(this, "Select a ai model to intract", Toast.LENGTH_SHORT).show()
            }
        }


        val model = GenerativeModel(
            aiModels.selectedItem.toString(),
            apiKey,
            generationConfig = generationConfig {

                temperature = 0.70f
                topK = 32
                topP = 0.40f
                maxOutputTokens = 1000
                responseMimeType = "text/plain"
            }
        )


        messageList = mutableListOf()
        chatMessageAdapter = ChatMessageAdapter(messageList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = chatMessageAdapter

        chat = model.startChat()
        val pic = imageButton.setOnClickListener {
            val img = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            changeImage.launch(img)
        }

        send.setOnClickListener {
            val userMessage = sendMessage.text.toString()

            sendMessage.setText("") // Clear the input field

            // Add user message to the list and update UI
            messageList.add(ChatMessage(userMessage, true, selectedImageBitmap))
            chatMessageAdapter.notifyItemInserted(messageList.size - 1)
            recyclerView.smoothScrollToPosition(messageList.size - 1)

            image.visibility = View.GONE

            CoroutineScope(Dispatchers.IO).launch {
                if (selectedImageBitmap != null) {
                    sendMessageToAI(userMessage, selectedImageBitmap!!)
                } else {
                    sendMessageToAI(userMessage)
                }
            }

            // Reset selectedImageBitmap after sending the message
            // Clear the selected image
        }
        selectedImageBitmap = null

    }

    private val changeImage =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                val data = it.data
                val imgUri = data?.data
                image.setImageURI(imgUri)

                // Get the Bitmap from the URI
                selectedImageBitmap = imgUri?.let {
                    contentResolver.openInputStream(it)?.use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)
                    }
                }
            }
        }


    private suspend fun sendMessageToAI(message: String, image: Bitmap? = null) {
        val response = if (image != null) {
            val content = prepareContent(message, image)
            chat.sendMessage(content)

        } else {
            chat.sendMessage(message)
        }


        val markdownText = response.text ?: "Error: Could not format response" // Get the AI response

        val markwon = Markwon.create(this)
         // Create a Markwon instance
        val formattedText: Spanned = markwon.toMarkdown(markdownText) // Get the formatted Spanned text



        val formattedResponse = formattedText.toString()



        val builder = StringBuilder()
        val chunkSize = 7050 // adjust this value as needed

        for (i in 0 until formattedResponse.length step chunkSize) {
            val chunk = formattedResponse.substring(i, minOf(i + chunkSize, formattedResponse.length))
            builder.append(chunk)
            builder.append("\n")

            runOnUiThread {


                // Add AI response to the list and update UI
                    messageList.add(ChatMessage(formattedResponse, false))
                    chatMessageAdapter.notifyItemInserted(messageList.size - 1)
                    recyclerView.smoothScrollToPosition(messageList.size - 1)
            }
        }
    }

    private fun prepareContent(message: String, image: Bitmap): Content {
        return content {
            text(message)
            this.image(image)
        }
    }
}