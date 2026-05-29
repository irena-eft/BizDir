package com.example.bizdir;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class ApiClient {
    // Change YOUR_SERVER_IP to your computer's IP (e.g. 10.0.2.2 for emulator, 192.168.x.x for a real device).
    private static final String BASE_URL = "http://YOUR_SERVER_IP/business_directory/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static CompanyService getCompanyService() {
        return getClient().create(CompanyService.class);
    }
}
