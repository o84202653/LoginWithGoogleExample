package com.dtse.oliverbotello.loginwithgoogle.huawei;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectAuthCredential;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.auth.GoogleAuthProvider;
import com.huawei.agconnect.auth.SignInResult;
import com.huawei.agconnect.auth.internal.user.AGConnectDefaultUser;
import com.huawei.agconnect.core.service.auth.OnTokenListener;
import com.huawei.agconnect.core.service.auth.TokenSnapshot;
import com.huawei.hmf.tasks.OnCanceledListener;
import com.huawei.hmf.tasks.OnCompleteListener;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;
import net.openid.appauth.TokenResponse;

import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final int RQ_GOOGLE = 101;
    private static final int RQ_GOOGLE_OID = 102;
    private Button btnMethod1;
    private Button btnMethod2;
    private Button btnMethod3;
    private AGConnectAuth agcInstance;
    private Task<SignInResult> singIn;
    private AGConnectUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        agcInstance = AGConnectAuth.getInstance();
        agcInstance.addTokenListener(
                new OnTokenListener() {
                    @Override
                    public void onChanged(TokenSnapshot tokenSnapshot) {
                        showMessage(tokenSnapshot.getState().toString());
                    }
                }
        );
    }

    private void initView() {
        btnMethod1 = (Button) findViewById(R.id.btnLoginGoogle);
        btnMethod2 = (Button) findViewById(R.id.btnLoginGoogle2);
        btnMethod3 = (Button) findViewById(R.id.btnLoginGoogle3);

        btnMethod1.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showMessage("Sing In With Google Method 1");
                        loginWithGoogleMethod1();
                    }
                }
        );
        btnMethod2.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showMessage("Sing In With Google Method 2");
                        loginWithGoogleMethod2();
                    }
                }
        );
        btnMethod3.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showMessage("Sing In With Google Method 3");
                        loginWithGoogleMethod3();
                    }
                }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RQ_GOOGLE) {
            showMessage("onActivityResult Method 2");
            com.google.android.gms.tasks.Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            task.addOnSuccessListener(googleSignInAccount -> {
                String idToken = googleSignInAccount.getIdToken();
                AGConnectAuthCredential credential = GoogleAuthProvider.credentialWithToken(idToken);
                singIn = agcInstance.signIn(credential)
                        .addOnSuccessListener(new OnSuccessListener<SignInResult>() {
                            @Override
                            public void onSuccess(SignInResult signInResult) {
                                user = signInResult.getUser();

                                if (user != null)
                                    onSuccessLogin();
                                else
                                    onFailedLogin();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                onFailedLogin();
                            }
                        }).addOnCanceledListener(
                                new OnCanceledListener() {
                                    @Override
                                    public void onCanceled() {
                                        showMessage("Login Canceled: Method 2");
                                    }
                                }
                        ).addOnCompleteListener(
                                new OnCompleteListener<SignInResult>() {
                                    @Override
                                    public void onComplete(Task<SignInResult> task) {
                                        showMessage("Login Complete: Method 2");
                                    }
                                }
                        );
                showMessage("Usuario correcto");
            }).addOnFailureListener((com.google.android.gms.tasks.OnFailureListener) e -> {
                showMessage("Method2 -  ERROR: " + e.getMessage());
            });
        }
        else if(requestCode == RQ_GOOGLE_OID) {
            if (data != null) {
                showMessage("M3: Usuario");
                AuthorizationResponse response = AuthorizationResponse.fromIntent(data);
                AuthorizationException ex = AuthorizationException.fromIntent(data);
                AuthState authState = new AuthState(response, ex);
                if (response != null) {
                    AuthorizationService service = new AuthorizationService(this);
                    service.performTokenRequest(
                            response.createTokenExchangeRequest(),
                            new AuthorizationService.TokenResponseCallback() {
                                @Override
                                public void onTokenRequestCompleted(@Nullable TokenResponse response, @Nullable AuthorizationException ex) {
                                    if (ex != null) {
                                        showMessage("Token Exchange failed " + ex.getMessage());
                                    } else {
                                        if (response != null) {
                                            authState.update(response, ex);
                                            showMessage("Token " + response.accessToken + " - ");
                                            AGConnectAuthCredential credential = GoogleAuthProvider.credentialWithToken(response.idToken);
                                            singIn = agcInstance.signIn(credential)
                                                    .addOnSuccessListener(new OnSuccessListener<SignInResult>() {
                                                        @Override
                                                        public void onSuccess(SignInResult signInResult) {
                                                            user = signInResult.getUser();

                                                            if (user != null)
                                                                onSuccessLogin();
                                                            else
                                                                onFailedLogin();
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(Exception e) {
                                                            onFailedLogin();
                                                        }
                                                    }).addOnCanceledListener(
                                                            new OnCanceledListener() {
                                                                @Override
                                                                public void onCanceled() {
                                                                    showMessage("Login Canceled: Method 2");
                                                                }
                                                            }
                                                    ).addOnCompleteListener(
                                                            new OnCompleteListener<SignInResult>() {
                                                                @Override
                                                                public void onComplete(Task<SignInResult> task) {
                                                                    showMessage("Login Complete: Method 2");
                                                                }
                                                            }
                                                    );
                                        }
                                    }
                                }
                            }
                    );
                }

            }
            else {
                showMessage("M3 Error: Surgio un error");
            }
        }
        else  {
            user = agcInstance.getCurrentUser();

            if (user != null)
                onSuccessLogin();
            else
                onFailedLogin();
        }
    }

    private void loginWithGoogleMethod1() {
        agcInstance.addTokenListener(
                new OnTokenListener() {
                    @Override
                    public void onChanged(TokenSnapshot tokenSnapshot) {
                        showMessage(tokenSnapshot.getState().toString());
                    }
                }
        );
        singIn = agcInstance.signIn(this, AGConnectAuthCredential.Google_Provider)
                .addOnSuccessListener(new OnSuccessListener<SignInResult>() {
                    @Override
                    public void onSuccess(SignInResult signInResult) {
                        user = signInResult.getUser();

                        if (user != null)
                            onSuccessLogin();
                        else
                            onFailedLogin();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        onFailedLogin();
                    }
                }).addOnCanceledListener(
                        new OnCanceledListener() {
                            @Override
                            public void onCanceled() {
                                showMessage("Login Canceled: Method 1");
                            }
                        }
                ).addOnCompleteListener(
                        new OnCompleteListener<SignInResult>() {
                            @Override
                            public void onComplete(Task<SignInResult> task) {
                                showMessage("Login Complete: Method 1");
                            }
                        }
                );
    }

    private void loginWithGoogleMethod2() {
        AGConnectUser user = AGConnectAuth.getInstance().getCurrentUser();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(this.getString(R.string.google_client_id))
                .requestProfile()
                .build();
        GoogleSignInClient client = GoogleSignIn.getClient(this, gso);

        Intent signInIntent = client.getSignInIntent();
        startActivityForResult(signInIntent, RQ_GOOGLE);
    }

    private void loginWithGoogleMethod3() {
        AuthorizationServiceConfiguration serviceConfig = new AuthorizationServiceConfiguration(
                Uri.parse("https://accounts.google.com/o/oauth2/auth"), // authorization endpoint
                Uri.parse("https://oauth2.googleapis.com/token")
        );
        AuthorizationRequest.Builder authRequestBuilder = new AuthorizationRequest.Builder(
                serviceConfig,  // the authorization service configuration
                this.getString(R.string.google_client_id_oid),  // the client ID, typically pre-registered and static
                ResponseTypeValues.CODE,  //
                Uri.parse("com.dtse.oliverbotello.loginwithgoogle.huawei:/oauth2redirect")
        );
        //com.dtse.oliverbotello.loginwithgoogle.huawei
        //$PACKAGE_NAME
        authRequestBuilder.setScope("openid email profile");
        AuthorizationRequest authRequest = authRequestBuilder.build();
        AuthorizationService authService = new AuthorizationService(this);
        Intent authIntent = authService.getAuthorizationRequestIntent(authRequest);
        startActivityForResult(authIntent, RQ_GOOGLE_OID);
    }

    private void onSuccessLogin() {
        showMessage("Login Success: " + user.getDisplayName());
        showDataUser();
    }

    private void showDataUser() {
        loadImage();
        ((TextView)findViewById(R.id.txtVw_DisplayName)).setText(user.getDisplayName());
    }

    private void loadImage() {
        new AsyncTask<Object, Object, Object> () {
            Bitmap bmp = null;

            @Override
            protected Object doInBackground(Object... objects) {
                try {
                    URL url = new URL(((AGConnectDefaultUser)user).getPhotoUrl());
                    bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                } catch (Exception ex) {
                    showMessage("Load Image Fail");
                }

                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                ((ImageView)findViewById(R.id.imgVw_Picture)).setImageBitmap(bmp);
            }
        }.execute();
    }

    private void onFailedLogin() {
        showMessage("Login Fail");
    }

    private void showMessage(String message) {
        Log.e("Prueba", message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}