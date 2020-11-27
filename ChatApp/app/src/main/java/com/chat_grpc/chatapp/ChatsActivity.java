package com.chat_grpc.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.chat.grpc.ChatGrpc;
import com.chat.grpc.ChatServer;
import com.chat.grpc.Uuid;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class ChatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

        SharedPreferences sharedPreferences = getSharedPreferences("USER", 0);


        ChatGrpc.ChatFutureStub chatGrpc = ChatGrpc.newFutureStub(GrpcChannel.getChannel(this));

        ListenableFuture<ChatServer.GetUserChatsResponse> request = chatGrpc.getUserChats(
                ChatServer.GetUserChatsRequest.newBuilder()
                        .setUserId(Uuid.UUID.newBuilder().setUuid(sharedPreferences.getString("id", "")).build())
                        .build()
        );

        ListView chats = findViewById(R.id.chats);

        Futures.addCallback(request, new FutureCallback<ChatServer.GetUserChatsResponse>() {
            @Override
            public void onSuccess(@NullableDecl ChatServer.GetUserChatsResponse result) {
                for (ChatServer.Group group : result.getChatsList()) {
                    Log.i("Group", group.getName());
                }
                runOnUiThread(() -> {
                    chats.setAdapter(new ChatAdapter(ChatsActivity.this, R.layout.chat, result.getChatsList()));
                });
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("error", t.getLocalizedMessage());

            }
        }, GrpcChannel.getExecutor());
    }
}
