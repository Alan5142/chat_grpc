package com.chat_grpc.chatapp;


import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.chat.grpc.ChatGrpc;
import com.chat.grpc.ChatServer;
import com.chat.grpc.Uuid;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.checkerframework.checker.nullness.qual.Nullable;

public class CustomAddDialog extends Dialog implements
        android.view.View.OnClickListener {

    private Activity activity;
    private EditText e;
    private Button yes, no;
    private ChatServer.Group group;

    public CustomAddDialog(Activity a, ChatServer.Group group) {
        super(a);
        this.group = group;
        this.activity = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.add_user_to_group);
        e = (EditText) findViewById(R.id.add_to_group_name);
        yes = (Button) findViewById(R.id.add_yes);
        no = (Button) findViewById(R.id.add_no);
        yes.setOnClickListener(this);
        no.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_yes:
                SharedPreferences preferences = activity.getSharedPreferences("USER", 0);
                ChatServer.AddToChatRequest addToChatRequest = ChatServer.AddToChatRequest.newBuilder()
                        .setChatId(group.getId())
                        .setUserToAddEmail(e.getText().toString())
                        .build();
                ChatGrpc.ChatFutureStub chatGrpc = ChatGrpc.newFutureStub(GrpcChannel.getChannel(activity));
                ListenableFuture<com.google.protobuf.Empty> addToChatFuture = chatGrpc.addToChat(addToChatRequest);

                Futures.addCallback(addToChatFuture, new FutureCallback<com.google.protobuf.Empty>() {
                    @Override
                    public void onSuccess(com.google.protobuf.Empty result) {
                        activity.runOnUiThread(() -> Toast.makeText(activity, "Agregado exitosamente", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        activity.runOnUiThread(() -> Toast.makeText(activity, "No se pudo agregar al chat", Toast.LENGTH_SHORT).show());
                    }
                }, GrpcChannel.getExecutor());

                break;
            case R.id.add_no:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }
}