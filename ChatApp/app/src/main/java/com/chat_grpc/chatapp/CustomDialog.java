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

/**
 * Dialog para añadir un nuevo usuario a tu chat o crear un grupo.
 */
public class CustomDialog extends Dialog implements
        android.view.View.OnClickListener {

    /**
     * Activity a la que va a pertenecer el dialog.
     */
    private Activity activity;
    /**
     * Cuadro de texto donde se pone el e-mail del usuario a agregar o el nombre del grupo.
     */
    private EditText editText;
    /**
     * Boton para aceptar la acción.
     */
    private Button yes;
    /**
     * Botón para cancelar la acción.
     */
    private Button no;
    /**
     * Check Box para saber si se quiere crear un grupo o agregar un usuario.
     */
    private CheckBox isGroup;

    /**
     * Consturctor que recibe la activity a la que pertenece.
     * @param a Activity a la que pertenece.
     */
    public CustomDialog(Activity a) {
        super(a);
        // TODO Auto-generated constructor stub
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
        setContentView(R.layout.add_dialog);
        editText = (EditText) findViewById(R.id.add_name);
        yes = (Button) findViewById(R.id.btn_yes);
        no = (Button) findViewById(R.id.btn_no);
        isGroup = (CheckBox) findViewById(R.id.check_group);
        yes.setOnClickListener(this);
        no.setOnClickListener(this);

    }

    /**
     * Función que se llama en una acción de click.
     * Si la opción es "Aceptar" y la Check Box está marcada entonces crea
     * un nuevo grupo con el nombre indicado. Si no está marcada intenta añadir
     * un usuario con el e-mail indicado.
     * Si la opción es cancelar cierra el dialog.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_yes:
                ChatServer.CreateChatRequest createChatRequest;
                SharedPreferences preferences = activity.getSharedPreferences("USER", 0);
                if (isGroup.isChecked()) {
                    createChatRequest = ChatServer.CreateChatRequest.newBuilder()
                            .setChatType(ChatServer.ChatType.GroupChat)
                            .setName(editText.getText().toString())
                            .setUserId(Uuid.UUID.newBuilder().setUuid(preferences.getString("id", "")).build())
                            .build();
                } else {
                    createChatRequest = ChatServer.CreateChatRequest.newBuilder()
                            .setChatType(ChatServer.ChatType.PairChat)
                            .setName(editText.getText().toString())
                            .setPartnerEmail(editText.getText().toString())
                            .setUserId(Uuid.UUID.newBuilder().setUuid(preferences.getString("id", "")).build())
                            .build();
                }
                ChatGrpc.ChatFutureStub chatGrpc = ChatGrpc.newFutureStub(GrpcChannel.getChannel(activity));
                ListenableFuture<ChatServer.Group> createChatFuture = chatGrpc.createChat(createChatRequest);

                Futures.addCallback(createChatFuture, new FutureCallback<ChatServer.Group>() {
                    @Override
                    public void onSuccess(ChatServer.@Nullable Group result) {
                        activity.runOnUiThread(() -> Toast.makeText(activity, "Agregado exitosamente", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        activity.runOnUiThread(() -> Toast.makeText(activity, "No se pudo crear, verifica los datos", Toast.LENGTH_SHORT).show());
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