package com.example.bizdir;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * The Android app only knows about our PHP endpoints. PHP knows about
 * Supabase. This keeps the Supabase service-role key safely on the server
 * and means we can change the database without touching the app.
 */
public interface CompanyService {

    @GET("get_companies.php")
    Call<List<Company>> getCompanies(@Query("category") String category);

    @POST("add_company.php")
    Call<List<Company>> addCompany(@Body Company company);

    @PATCH("update_company.php")
    Call<List<Company>> updateCompany(@Query("id") int id, @Body Company company);

    @DELETE("delete_company.php")
    Call<List<Company>> deleteCompany(@Query("id") int id);
}
