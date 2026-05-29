package com.example.bizdir;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    // Paste your values from the Supabase dashboard:
    //   Project Settings -> API -> Project URL  (e.g. https://abcdefg.supabase.co)
    //   Project Settings -> API -> anon public  (a long string starting with eyJ...)
    private static final String SUPABASE_URL = "https://cflvicujcrvpzjdfwgug.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImNmbHZpY3VqY3J2cHpqZGZ3Z3VnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODAwNTYzMTQsImV4cCI6MjA5NTYzMjMxNH0.0Q-D-WQg1bmgUGhbE6XiicxUs_8xJnyp3tcWneMf3JA";

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        okhttp3.Request request = chain.request().newBuilder()
                                .header("apikey", SUPABASE_ANON_KEY)
                                .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                                .build();
                        return chain.proceed(request);
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(SUPABASE_URL + "/rest/v1/")
                    .client(httpClient)
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
}
