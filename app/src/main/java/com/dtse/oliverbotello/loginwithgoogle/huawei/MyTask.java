package com.dtse.oliverbotello.loginwithgoogle.huawei;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyTask extends AsyncTask<Void, Void, JSONObject> {
    String accessToken;

    public MyTask(String token) {
        super();
        this.accessToken = token;
    }

    @Override
    protected JSONObject doInBackground(Void... voids) {
        try {
            URL url = new URL("https://www.googleapis.com/oauth2/v3/userinfo");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", String.format("Bearer %s", accessToken));
            String userInfo = convertStreamToString(conn.getInputStream());

            return new JSONObject(userInfo);
        } catch (Exception ex){
            return null;
        }
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);
        if(jsonObject != null){
            Log.e("JSON",jsonObject.toString());
        }
    }

    public String convertStreamToString(InputStream input){
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        StringBuilder sb = new StringBuilder();
        String line = null;

        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        } finally {
            try {
                input.close();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return sb.toString();
    }
}
