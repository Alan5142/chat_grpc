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

/**
 * Actividad que muestra todos los chats de un usuario.
 */
public class ChatsActivity extends AppCompatActivity {

    /**
     * Cliente gRPC autogenerado.
     */
    private ChatGrpc.ChatFutureStub chatGrpcClient;

    /**
     * Función que se realiza al crearse la Activity, guarda los chats del usuario
     * para mostrarse y crea el botón para añadir usuarios/grupos.
     *
     * @param savedInstanceState Información de creación de la Activity.
     */
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

    /**
     * Se conecta al servidor para obtener los chats del usuario y colocarlos
     * haciendo uso del adapter correspondiente.
     */
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
                runOnUiThread(() -> {
                    Toast.makeText(ChatsActivity.this, "No se pudieron cargar los chats, checa tu conexión a internet", Toast.LENGTH_SHORT).show();
                    Log.e("error", t.getLocalizedMessage());
                });

            }
        }, GrpcChannel.getExecutor());
    }

    /**
     * Pone una toolbar en la actividad para la opción de
     * recargar la Activity.
     *
     * @param menu Toolbar de la activity.
     * @return Verdadero si fue creado exitosamente, False de lo contrario.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chats_menu, menu);
        return true;
    }

    /**
     * Si se selecciona la opción de "Recargar" refresca los chats
     * para que se vean los nuevos.
     *
     * @param item Elemento de las opciones de la toolbar.
     * @return Verdadero si la acción se realizo exitosamente, False de lo contrario.
     */
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
