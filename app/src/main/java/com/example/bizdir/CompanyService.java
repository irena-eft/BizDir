package com.example.bizdir;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
public interface CompanyService {
    @GET("get_companies.php")
    Call<ApiResponse> getCompanies(
            @Query("category") String category,
            @Query("search") String search
    );

    @POST("add_company.php")
    Call<ApiResponse> addCompany(@Body Company company);
}
