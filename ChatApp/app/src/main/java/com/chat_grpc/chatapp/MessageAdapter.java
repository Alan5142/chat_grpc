package com.chat_grpc.chatapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chat.grpc.ChatServer;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Adapter que se encarga de mostrar los mensajes de un chat.
 */
public class MessageAdapter extends ArrayAdapter<ChatServer.ChatMessage> {
    /**
     * Context de la activity actual.
     */
    private Context context;


    /**
     * Constructor del adapter.
     * @param context Contexto de la activity a la que se aplicara.
     * @param resource Recursos de la activity.
     * @param items Lista de mensajes del chat.
     */
    public MessageAdapter(@NonNull Context context, int resource, List<ChatServer.ChatMessage> items) {
        super(context, resource, items);
        this.context = context;
    }

    /**
     * Construye la vista de un elemento de la lista.
     * @param position Posición del elemento.
     * @param convertView Vista a la que pertenece.
     * @param parent Grupo al que pertenece la vista.
     * @return La view ya construida.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        ChatServer.ChatMessage message = getItem(position);

        if (message != null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(context);
            if (message.hasImageMessage()) {
                v = vi.inflate(R.layout.image, null);
                TextView sender = (TextView) v.findViewById(R.id.senderImage);
                sender.setText(message.getSender().getName());
                ImageView image = (ImageView) v.findViewById(R.id.imageView);
                Picasso.get().setLoggingEnabled(true);
                Picasso.get().load(message.getImageMessage().getUrl()).into(image);
            }
            else {
                v = vi.inflate(R.layout.text, null);
                TextView sender = (TextView) v.findViewById(R.id.sender);
                TextView text = (TextView) v.findViewById(R.id.messageText);
                text.setText(message.getTextMessage().getContent());
                sender.setText(message.getSender().getName());
            }
        }

        return v;
    }
}
