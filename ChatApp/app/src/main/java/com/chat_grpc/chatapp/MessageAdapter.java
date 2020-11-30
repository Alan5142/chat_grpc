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

public class MessageAdapter extends ArrayAdapter<ChatServer.ChatMessage> {
    private int resourceLayout;
    private Context context;


    public MessageAdapter(@NonNull Context context, int resource, List<ChatServer.ChatMessage> items) {
        super(context, resource, items);
        this.resourceLayout = resource;
        this.context = context;
    }

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
