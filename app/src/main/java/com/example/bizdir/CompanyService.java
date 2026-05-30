package com.example.bizdir;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CompanyService {

    /**
     * GET /rest/v1/companies?select=*&order=name.asc[&category=ilike.*Fun*]
     * Supabase / PostgREST returns a plain JSON array of rows.
     */
    @GET("companies")
    Call<List<Company>> getCompanies(
            @Query("select") String select,
            @Query("category") String categoryFilter,
            @Query("order") String order
    );

    /**
     * POST /rest/v1/companies
     * "Prefer: return=representation" tells Supabase to send back the inserted
     * row (with its new id) so we can confirm the save worked.
     */
    @Headers({
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    @POST("companies")
    Call<List<Company>> addCompany(@Body Company company);

    /**
     * PATCH /rest/v1/companies?id=eq.123
     * Use ApiClient.idFilter(id) for the idFilter argument.
     */
    @Headers({
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    @PATCH("companies")
    Call<List<Company>> updateCompany(
            @Query("id") String idFilter,
            @Body Company company
    );

    /**
     * DELETE /rest/v1/companies?id=eq.123
     * "Prefer: return=representation" makes Supabase send back the deleted
     * row. If the response body is empty, we know Row Level Security blocked
     * the delete - which is the most common reason "delete looks like it
     * worked but the row is still there".
     */
    @Headers({"Prefer: return=representation"})
    @DELETE("companies")
    Call<List<Company>> deleteCompany(@Query("id") String idFilter);
}
