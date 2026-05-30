package com.example.bizdir;

import android.os.Handler;
import android.os.Looper;

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

    // Paste your values from the Supabase dashboard:
    //   Project Settings -> API -> Project URL  (e.g. https://abcdefg.supabase.co)
    //   Project Settings -> API -> anon public  (a long string starting with eyJ...)
    private static final String SUPABASE_URL = "https://cflvicujcrvpzjdfwgug.supabase.co";
    private static final String SUPABASE_ANON_KEY = "sb_publishable_ELxmVz4cuitPzTZ1u9fbGQ_iW_bfkjn";

    // Name of the Supabase Storage bucket where company logos are saved.
    // Created by the SQL in supabase_setup.sql.
    private static final String LOGO_BUCKET = "logos";

    private static Retrofit retrofit = null;
    private static OkHttpClient httpClient = null;

    private static OkHttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request request = chain.request().newBuilder()
                                .header("apikey", SUPABASE_ANON_KEY)
                                .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                                .build();
                        return chain.proceed(request);
                    })
                    .build();
        }
        return httpClient;
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(SUPABASE_URL + "/rest/v1/")
                    .client(getHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static CompanyService getCompanyService() {
        return getClient().create(CompanyService.class);
    }

    /**
     * Builds a PostgREST filter like "ilike.*Fun*" so the server returns rows
     * whose category column contains the given text (case-insensitive).
     * Returns null when no category is requested.
     */
    public static String categoryFilter(String category) {
        if (category == null || category.isEmpty()) {
            return null;
        }
        return "ilike.*" + category + "*";
    }

    public interface UploadCallback {
        void onSuccess(String publicUrl);
        void onError(String message);
    }

    /**
     * Uploads raw image bytes to the "logos" bucket in Supabase Storage and
     * calls back on the main thread with the public URL of the saved file.
     */
    public static void uploadImage(byte[] imageBytes, String mimeType, UploadCallback cb) {
        String filename = "logo_" + System.currentTimeMillis() + extensionFor(mimeType);
        String uploadUrl = SUPABASE_URL + "/storage/v1/object/" + LOGO_BUCKET + "/" + filename;
        String publicUrl = SUPABASE_URL + "/storage/v1/object/public/" + LOGO_BUCKET + "/" + filename;

        RequestBody body = RequestBody.create(MediaType.parse(mimeType), imageBytes);
        Request request = new Request.Builder()
                .url(uploadUrl)
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
                try {
                    if (response.isSuccessful()) {
                        main.post(() -> cb.onSuccess(publicUrl));
                    } else {
                        String msg = "Upload failed (HTTP " + response.code() + ")";
                        main.post(() -> cb.onError(msg));
                    }
                } finally {
                    response.close();
                }
            }
        });
    }

    private static String extensionFor(String mimeType) {
        if (mimeType == null) return ".jpg";
        if (mimeType.contains("png")) return ".png";
        if (mimeType.contains("webp")) return ".webp";
        if (mimeType.contains("gif")) return ".gif";
        return ".jpg";
    }
}
