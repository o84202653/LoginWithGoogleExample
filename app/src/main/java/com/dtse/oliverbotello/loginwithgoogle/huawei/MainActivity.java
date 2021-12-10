package com.dtse.oliverbotello.loginwithgoogle.huawei;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectAuthCredential;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.auth.SignInResult;
import com.huawei.agconnect.auth.internal.user.AGConnectDefaultUser;
import com.huawei.agconnect.core.service.auth.OnTokenListener;
import com.huawei.agconnect.core.service.auth.TokenSnapshot;
import com.huawei.hmf.tasks.OnCanceledListener;
import com.huawei.hmf.tasks.OnCompleteListener;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;

import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private Button btnMethod1;
    private Button btnMethod2;
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

        btnMethod1.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showMessage("Sing In With Google Method 1");
                        loginWithGoogleMethod1();
                    }
                }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        user = agcInstance.getCurrentUser();

        if (user != null)
            onSuccessLogin();
        else
            onFailedLogin();
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