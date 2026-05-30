package com.example.bizdir;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    // Where your PHP web services live. Change this to:
    //   http://10.0.2.2/business_directory/        for the Android emulator
    //   http://192.168.x.x/business_directory/     for a real phone on Wi-Fi
    // The trailing slash is required.
    private static final String BASE_URL = "http://10.0.2.2/business_directory/";

    private static Retrofit retrofit = null;
    private static OkHttpClient httpClient = null;

    private static OkHttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = new OkHttpClient.Builder().build();
        }
        return httpClient;
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(getHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static CompanyService getCompanyService() {
        return getClient().create(CompanyService.class);
    }

    public interface UploadCallback {
        void onSuccess(String publicUrl);
        void onError(String message);
    }

    /**
     * Uploads raw image bytes to upload_logo.php, which forwards them to
     * Supabase Storage. The PHP script replies with a JSON object that
     * contains the publicly accessible URL of the saved image.
     */
    public static void uploadImage(byte[] imageBytes, String mimeType, UploadCallback cb) {
        String url = BASE_URL + "upload_logo.php";

        RequestBody body = RequestBody.create(MediaType.parse(mimeType), imageBytes);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Handler main = new Handler(Looper.getMainLooper());
        getHttpClient().newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                main.post(() -> cb.onError(e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) {
                String responseBody = null;
                try {
                    responseBody = response.body() != null ? response.body().string() : "";
                } catch (IOException e) {
                    main.post(() -> cb.onError(e.getMessage()));
                    response.close();
                    return;
                } finally {
                    response.close();
                }

                if (!response.isSuccessful()) {
                    final String msg = "Upload failed (HTTP " + response.code() + ")";
                    main.post(() -> cb.onError(msg));
                    return;
                }

                try {
                    JSONObject json = new JSONObject(responseBody);
                    String publicUrl = json.getString("public_url");
                    main.post(() -> cb.onSuccess(publicUrl));
                } catch (Exception e) {
                    main.post(() -> cb.onError("Bad response from server: " + e.getMessage()));
                }
            }
        });
    }
}
