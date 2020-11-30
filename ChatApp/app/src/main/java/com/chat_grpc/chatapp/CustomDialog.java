package com.chat_grpc.chatapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.chat.grpc.ChatGrpc;
import com.chat.grpc.ChatServer;
import com.chat.grpc.Uuid;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.checkerframework.checker.nullness.qual.Nullable;

public class CustomDialog extends Dialog implements
        android.view.View.OnClickListener {

    public Activity c;
    public EditText e;
    public Button yes, no;
    private CheckBox isGroup;

    public CustomDialog(Activity a) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.add_dialog);
        e = (EditText) findViewById(R.id.add_name);
        yes = (Button) findViewById(R.id.btn_yes);
        no = (Button) findViewById(R.id.btn_no);
        isGroup = (CheckBox) findViewById(R.id.check_group);
        yes.setOnClickListener(this);
        no.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_yes:
                ChatServer.CreateChatRequest createChatRequest;
                SharedPreferences preferences = c.getSharedPreferences("USER", 0);
                if (isGroup.isChecked()) {
                    createChatRequest = ChatServer.CreateChatRequest.newBuilder()
                            .setChatType(ChatServer.ChatType.GroupChat)
                            .setName(e.getText().toString())
                            .setUserId(Uuid.UUID.newBuilder().setUuid(preferences.getString("id", "")).build())
                            .build();
                } else {
                    createChatRequest = ChatServer.CreateChatRequest.newBuilder()
                            .setChatType(ChatServer.ChatType.PairChat)
                            .setName(e.getText().toString())
                            .setPartnerEmail(e.getText().toString())
                            .setUserId(Uuid.UUID.newBuilder().setUuid(preferences.getString("id", "")).build())
                            .build();
                }
                ChatGrpc.ChatFutureStub chatGrpc = ChatGrpc.newFutureStub(GrpcChannel.getChannel(c));
                ListenableFuture<ChatServer.Group> createChatFuture = chatGrpc.createChat(createChatRequest);

                Futures.addCallback(createChatFuture, new FutureCallback<ChatServer.Group>() {
                    @Override
                    public void onSuccess(ChatServer.@Nullable Group result) {
                        c.runOnUiThread(() -> Toast.makeText(c, "Agregado exitosamente", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        c.runOnUiThread(() -> Toast.makeText(c, "No se pudo crear, verifica los datos", Toast.LENGTH_SHORT).show());
                    }
                }, GrpcChannel.getExecutor());

                break;
            case R.id.btn_no:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }
}