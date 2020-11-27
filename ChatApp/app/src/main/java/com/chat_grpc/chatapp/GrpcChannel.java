package com.chat_grpc.chatapp;

import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GrpcChannel {
    private static ManagedChannel channel;

    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    public static ExecutorService getExecutor() {
        return executorService;
    }

    public static ManagedChannel getChannel(Context context) {
        if (channel == null) {
            channel = ManagedChannelBuilder.forTarget(context.getResources().getString(R.string.grpc_host)).usePlaintext().build();
        }
        return channel;
    }
}
