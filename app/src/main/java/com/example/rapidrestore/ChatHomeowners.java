package com.example.rapidrestore;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ChatHomeowners extends AppCompatActivity {

    String providerId;
    FirebaseFirestore db;
    RecyclerView recyclerViewChatUsers;
    ChatUserAdapter adapter;
    List<ChatUser> chatUsersList = new ArrayList<>();;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_homeowners);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        providerId = getIntent().getStringExtra("providerId");

        recyclerViewChatUsers = findViewById(R.id.chatUsersRecycler);
        adapter = new ChatUserAdapter( chatUsersList, ChatHomeowners.this);
        recyclerViewChatUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChatUsers.setAdapter(adapter);

        db.collection("chats")
                .whereEqualTo("providerId", providerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    chatUsersList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String homeownerId = doc.getString("homeownerId");

                        // You may want to fetch the homeowner's name/photo from the 'users' collection
                        db.collection("users")
                                .document(homeownerId)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    String name = userDoc.getString("name");
                                    chatUsersList.add(new ChatUser(homeownerId, name));
                                    adapter.notifyDataSetChanged();
                                });
                    }
                });

    }
}