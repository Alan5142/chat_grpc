package com.chat_grpc.chatapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chat.grpc.ChatServer;

import java.util.List;

public class ChatAdapter extends ArrayAdapter<ChatServer.Group> {
    private int resourceLayout;
    private Context context;


    public ChatAdapter(@NonNull Context context, int resource, List<ChatServer.Group> items) {
        super(context, resource, items);
        this.resourceLayout = resource;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(context);
            v = vi.inflate(resourceLayout, null);
        }

        ChatServer.Group group = getItem(position);

        if (group != null) {
            TextView name = (TextView) v.findViewById(R.id.chat_name);
            if (name != null) {
                name.setText(group.getName());
            }
        }

        return v;
    }
}
