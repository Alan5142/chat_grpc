package com.chat_grpc.chatapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chat.grpc.ChatServer;

import java.util.List;
import java.util.stream.Collectors;

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
            v.setOnClickListener(view -> {
                Intent intent = new Intent(context, MessagingActivity.class);
                context.startActivity(intent.putExtra("ChatID", group.getId().getUuid()));
            });
            TextView name = (TextView) v.findViewById(R.id.chat_name);
            if (group.getGroupType() == ChatServer.GroupType.GROUP) {
                name.setText(group.getName());
            } else {
                SharedPreferences preferences = context.getSharedPreferences("USER", 0);
                String userId = preferences.getString("id", "");
                List<String> email = group.getMembersList()
                        .stream()
                        .filter(u -> !u.getId().getUuid().equals(userId))
                        .map(ChatServer.User::getEmail).collect(Collectors.toList());
                name.setText(email.get(0));
            }
        }

        return v;
    }


}
