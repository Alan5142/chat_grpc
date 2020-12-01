package com.chat_grpc.chatapp;

import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * Utilidades para el cliente gRPC.
 */
public class GrpcChannel {
    /**
     * Canal por el cual se envían los datos de gRPC.
     */
    private static ManagedChannel channel;

    /**
     * Es un thread pool que ejecuta las peticiones gRPC.
     */
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * Devuelve el thread pool.
     * @return
     */
    public static ExecutorService getExecutor() {
        return executorService;
    }

    /**
     * Devuelve el canal por el que se comunica y recibe el contexto para
     * conocer el servidor gRPC.
     * @param context
     * @return
     */
    public static ManagedChannel getChannel(Context context) {
        if (channel == null) {
            channel = ManagedChannelBuilder.forTarget(context.getResources().getString(R.string.grpc_host)).usePlaintext().build();
        }
        return channel;
    }
}
