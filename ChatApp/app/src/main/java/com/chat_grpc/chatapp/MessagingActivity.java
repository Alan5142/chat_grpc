package com.chat_grpc.chatapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.chat.grpc.ChatGrpc;
import com.chat.grpc.ChatServer;
import com.chat.grpc.Uuid;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.grpc.stub.StreamObserver;

public class MessagingActivity extends AppCompatActivity {

    private List<ChatServer.ChatMessage> messageList;
    private MessageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);
        ListView messages = (ListView) findViewById(R.id.messageList);

        SharedPreferences sharedPreferences = getSharedPreferences("USER", 0);
        Uuid.UUID chatId = Uuid.UUID.newBuilder().setUuid(getIntent().getStringExtra("ChatID")).build();

        ChatGrpc.ChatFutureStub chatGrpc = ChatGrpc.newFutureStub(GrpcChannel.getChannel(this));
        ChatGrpc.ChatStub chat = ChatGrpc.newStub(GrpcChannel.getChannel(this));
        chat.joinChat(ChatServer.JoinChatRequest.newBuilder().setId(chatId).build(), new StreamObserver<ChatServer.ChatMessage>() {
            @Override
            public void onNext(ChatServer.ChatMessage value) {
                runOnUiThread(() -> {
                    try {
                        if (messageList != null) {
                            messageList.add(value);
                            adapter.notifyDataSetChanged();

                            final Handler handler = new Handler();
                            handler.postDelayed(() -> messages.smoothScrollToPosition(messageList.size() - 1), 10);
                        }
                    } catch (Exception e) {
                        Log.e("aaaaaaaaaaaaaa", e.getLocalizedMessage());
                    }
                });
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted() {
            }
        });

        ListenableFuture<ChatServer.Group> request = chatGrpc.getChat(
                ChatServer.GetChatRequest.newBuilder()
                        .setId(chatId)
                        .build()
        );


        Futures.addCallback(request, new FutureCallback<ChatServer.Group>() {
            @Override
            public void onSuccess(@Nullable ChatServer.Group result) {
                runOnUiThread(() -> {
                    messageList = new ArrayList<>();
                    messageList.addAll(result.getMessagesList());
                    adapter = new MessageAdapter(MessagingActivity.this, R.layout.chat, messageList);
                    messages.setAdapter(adapter);
                    final Handler handler = new Handler();
                    handler.postDelayed(() -> {
                        messages.setSelection(messages.getCount() - 1);
                    }, 100);

                });
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("error", t.getLocalizedMessage());

            }
        }, GrpcChannel.getExecutor());
    }


}