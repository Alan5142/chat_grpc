package com.chat_grpc.chatapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.chat.grpc.ChatGrpc;
import com.chat.grpc.ChatServer;
import com.chat.grpc.Uuid;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class ChatsActivity extends AppCompatActivity {

    private ChatGrpc.ChatFutureStub chatGrpcClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add);

        CustomDialog cdd = new CustomDialog(this);

        fab.setOnClickListener(v -> {
            cdd.show();
        });
        chatGrpcClient = ChatGrpc.newFutureStub(GrpcChannel.getChannel(this));
        getChats();
    }

    public void getChats() {
        SharedPreferences sharedPreferences = getSharedPreferences("USER", 0);
        ListenableFuture<ChatServer.GetUserChatsResponse> request = chatGrpcClient.getUserChats(
                ChatServer.GetUserChatsRequest.newBuilder()
                        .setUserId(Uuid.UUID.newBuilder().setUuid(sharedPreferences.getString("id", "")).build())
                        .build()
        );
        ListView chats = findViewById(R.id.chats);

        Futures.addCallback(request, new FutureCallback<ChatServer.GetUserChatsResponse>() {
            @Override
            public void onSuccess(@Nullable ChatServer.GetUserChatsResponse result) {
                runOnUiThread(() -> chats.setAdapter(new ChatAdapter(ChatsActivity.this, R.layout.chat, result.getChatsList())));
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(ChatsActivity.this, "No se pudieron cargar los chats, checa tu conexión a internet", Toast.LENGTH_SHORT).show();
                Log.e("error", t.getLocalizedMessage());

            }
        }, GrpcChannel.getExecutor());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chats_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.refresh) {
            getChats();
            return true;
        }
        return true;
    }
}
