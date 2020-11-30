package com.chat_grpc.chatapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.chat.grpc.ChatGrpc;
import com.chat.grpc.ChatServer;
import com.chat.grpc.Uuid;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.ByteString;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.grpc.stub.StreamObserver;

public class MessagingActivity extends AppCompatActivity {

    private List<ChatServer.ChatMessage> messageList;
    private MessageAdapter adapter;
    private EditText textMessage;
    private static final int PICK_IMAGE = 1;
    ChatGrpc.ChatFutureStub chatFutureStub;
    Uuid.UUID chatId;
    Uuid.UUID userId;
    ChatServer.Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);
        textMessage = findViewById(R.id.text_message);

        ListView messages = (ListView) findViewById(R.id.messageList);

        SharedPreferences sharedPreferences = getSharedPreferences("USER", 0);
        userId = Uuid.UUID.newBuilder().setUuid(sharedPreferences.getString("id", "")).build();
        chatId = Uuid.UUID.newBuilder().setUuid(getIntent().getStringExtra("ChatID")).build();

        chatFutureStub = ChatGrpc.newFutureStub(GrpcChannel.getChannel(this));
        ChatGrpc.ChatStub chatStub = ChatGrpc.newStub(GrpcChannel.getChannel(this));
        chatStub.joinChat(ChatServer.JoinChatRequest.newBuilder().setId(chatId).build(), new StreamObserver<ChatServer.ChatMessage>() {
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

        ListenableFuture<ChatServer.Group> request = chatFutureStub.getChat(
                ChatServer.GetChatRequest.newBuilder()
                        .setId(chatId)
                        .build()
        );


        Futures.addCallback(request, new FutureCallback<ChatServer.Group>() {
            @Override
            public void onSuccess(@Nullable ChatServer.Group result) {
                group = result;
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

        // send text
        findViewById(R.id.send_text).setOnClickListener(v -> {
            String message = textMessage.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(this, "No puede estar vacío", Toast.LENGTH_LONG).show();
                return;
            }

            if (message.length() > 2000) {
                Toast.makeText(this, "Mensaje muy largo, debe ser menor a 2000 caracteres", Toast.LENGTH_LONG).show();
                return;
            }

            ListenableFuture<com.chat.grpc.ChatServer.ChatMessage> sendMessageFuture = chatFutureStub.sendMessage(ChatServer.SendMessageRequest.newBuilder()
                    .setGroup(chatId)
                    .setUserId(userId)
                    .setTextMessage(ChatServer.TextMessage.newBuilder()
                            .setContent(message)
                            .build())
                    .build());
            Futures.addCallback(sendMessageFuture, new FutureCallback<ChatServer.ChatMessage>() {
                @Override
                public void onSuccess(ChatServer.@org.checkerframework.checker.nullness.qual.Nullable ChatMessage result) {
                    runOnUiThread(() -> textMessage.setText(""));
                }

                @Override
                public void onFailure(@NotNull Throwable t) {
                    Log.e("Error", t.toString());
                }
            }, GrpcChannel.getExecutor());
        });

        // Send image
        findViewById(R.id.send_image).setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.messages_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_user) {
            new CustomAddDialog(this, group).show();
            return true;
        }
        return true;
    }

    private String getRealPathFromURI(Uri contentURI) {

        String thePath = "no-path-found";
        String[] filePathColumn = {MediaStore.Images.Media.DISPLAY_NAME};
        Cursor cursor = getContentResolver().query(contentURI, filePathColumn, null, null, null);
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            thePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return thePath;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            try {
                Uri filePath = data.getData();
                InputStream inputStream = this.getContentResolver().openInputStream(filePath);
                File f = new File(getRealPathFromURI(filePath));
                ListenableFuture<com.chat.grpc.ChatServer.UploadImageResponse> uploadFuture = chatFutureStub.uploadImage(ChatServer.UploadImageRequest.newBuilder()
                        .setName(f.getName())
                        .setImage(ByteString.readFrom(inputStream))
                        .build());

                Futures.addCallback(uploadFuture, new FutureCallback<ChatServer.UploadImageResponse>() {
                    @Override
                    public void onSuccess(ChatServer.@org.checkerframework.checker.nullness.qual.Nullable UploadImageResponse result) {

                        if (result != null) {
                            ListenableFuture<ChatServer.ChatMessage> sendMessageFuture = chatFutureStub.sendMessage(ChatServer.SendMessageRequest.newBuilder()
                                    .setGroup(chatId)
                                    .setUserId(userId)
                                    .setImageMessage(ChatServer.ImageMessage.newBuilder()
                                            .setUrl(result.getUrl())
                                            .build())
                                    .build());

                            Futures.addCallback(sendMessageFuture, new FutureCallback<ChatServer.ChatMessage>() {
                                @Override
                                public void onSuccess(ChatServer.@org.checkerframework.checker.nullness.qual.Nullable ChatMessage result) {
                                    runOnUiThread(() -> textMessage.setText(""));
                                }

                                @Override
                                public void onFailure(@NotNull Throwable t) {
                                    Log.e("Error", t.toString());
                                }
                            }, GrpcChannel.getExecutor());
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Throwable t) {
                    }
                }, GrpcChannel.getExecutor());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}