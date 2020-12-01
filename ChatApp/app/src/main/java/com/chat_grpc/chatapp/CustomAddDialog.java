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
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * Dialog para añadir un nuevo usuario a un grupo.
 */
public class CustomAddDialog extends Dialog implements
        android.view.View.OnClickListener {

    /**
     * Activity a la que va a pertenecer el dialog.
     */
    private Activity activity;
    /**
     * Cuadro de texto donde se pone el e-mail del usuario a agregar.
     */
    private EditText e;
    /**
     * Botón de aceptar.
     */
    private Button yes;
    /**
     * Botón de cancelar.
     */
    private Button no;
    /**
     * Cliente gRPC autogenerado.
     */
    private ChatServer.Group group;

    /**
     * Consturctor que recibe la activity y el grupo al que
     * pertenece.
     * @param a Activity a la que pertenece.
     * @param group Grupo al que pertenece el chat.
     */
    public CustomAddDialog(Activity a, ChatServer.Group group) {
        super(a);
        this.group = group;
        this.activity = a;
    }

    /**
     * Función que se realiza al crearse el Dialog, coloca click
     * listeners en los botones.
     * @param savedInstanceState Información de creación de el Dialog.
     */
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

    /**
     * Función que se llama en una acción de click,
     * si la opción es "Aceptar" entonces intenta añadir un usuario
     * al grupo, si no, solo se cierra.
     * @param v View a la que pertenece el dialog.
     */
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