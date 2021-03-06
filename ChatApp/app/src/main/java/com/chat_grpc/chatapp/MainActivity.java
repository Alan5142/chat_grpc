package com.chat_grpc.chatapp;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.auth0.android.Auth0;
import com.auth0.android.authentication.AuthenticationAPIClient;
import com.auth0.android.authentication.AuthenticationException;
import com.auth0.android.authentication.storage.SecureCredentialsManager;
import com.auth0.android.authentication.storage.SharedPreferencesStorage;
import com.auth0.android.callback.BaseCallback;
import com.auth0.android.provider.AuthCallback;
import com.auth0.android.provider.WebAuthProvider;
import com.auth0.android.result.Credentials;
import com.auth0.android.result.UserProfile;
import com.chat.grpc.ChatGrpc;
import com.chat.grpc.ChatServer;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

/**
 * Activity principal, si no hay un usuario logeado se muestra el botón de Log In, y si lo hay
 * pasa directamente a la pestaña de tus chats.
 */
public class MainActivity extends AppCompatActivity {
    /**
     * Cliente 0auth.
     */
    private Auth0 auth0;

    /**
     * Gestor de sesión del usuario.
     */
    private SecureCredentialsManager credentialsManager;

    /**
     * Obtiene las credenciales del usuario y crea la Activity.
     * Si ya se ha iniciado sesión manda directo a los chats.
     * @param savedInstanceState Información de creación de la Activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth0 = new Auth0(this);
        auth0.setOIDCConformant(true);
        credentialsManager = new SecureCredentialsManager(this, new AuthenticationAPIClient(auth0), new SharedPreferencesStorage(this));

        if (credentialsManager.hasValidCredentials()) {
            goToMainScreen();
        }

        findViewById(R.id.login_button).setOnClickListener(v -> login());
    }

    /**
     * Intent para cambiar hacia la Activity que contiene los chats del usuario.
     */
    private void goToMainScreen() {
        Intent intent = new Intent(MainActivity.this, ChatsActivity.class);
        startActivity(intent);
    }

    /**
     * Te permite hacer Log In en tu cuenta a través de Auth0 la cual se encarga
     * de gestionar los usuarios, si es un nuevo usuario se ingresa su cuenta a la BD.
     * Después ingresa a los chats del usuario.
     */
    private void login() {
        WebAuthProvider.login(auth0)
                .withScheme("demo")
                .withAudience(String.format("https://%s/userinfo", getString(R.string.com_auth0_domain)))
                .withScope("update:current_user_identities openid profile email offline_access read:current_user update:current_user_metadata")
                .start(this, new AuthCallback() {
                    @Override
                    public void onFailure(@NonNull Dialog dialog) {
                        // Show error Dialog to user
                        dialog.show();
                    }

                    @Override
                    public void onFailure(AuthenticationException exception) {
                        // Show error to user
                        Toast.makeText(MainActivity.this, R.string.login_error, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onSuccess(@NonNull final Credentials credentials) {
                        AuthenticationAPIClient authenticationAPIClient = new AuthenticationAPIClient(auth0);

                        authenticationAPIClient.userInfo(credentials.getAccessToken())
                                .start(new BaseCallback<UserProfile, AuthenticationException>() {
                                    @Override
                                    public void onSuccess(@Nullable UserProfile userProfile) {
                                        SharedPreferences.Editor editor = getSharedPreferences("USER", 0).edit();

                                        ChatGrpc.ChatFutureStub chatGrpc = ChatGrpc.newFutureStub(GrpcChannel.getChannel(MainActivity.this));
                                        Futures.addCallback(chatGrpc.createUser(ChatServer.RegisterUserRequest.newBuilder()
                                                .setEmail(userProfile.getEmail())
                                                .setName(userProfile.getName())
                                                .setAuth0Id(userProfile.getId())
                                                .build()), new FutureCallback<ChatServer.User>() {
                                            @Override
                                            public void onSuccess(@Nullable ChatServer.User result) {
                                                editor.putString("id", result.getId().getUuid());
                                                editor.commit();
                                                goToMainScreen();
                                            }

                                            @Override
                                            public void onFailure(Throwable t) {

                                            }
                                        }, GrpcChannel.getExecutor());
                                    }

                                    @Override
                                    public void onFailure(@NonNull AuthenticationException error) {
                                        Log.e("Error", error.toString());
                                    }
                                });
                        credentialsManager.saveCredentials(credentials);
                    }
                });
    }
}