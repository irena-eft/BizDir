package com.example.bizdir;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddCompanyActivity extends AppCompatActivity {

    private EditText editName, editAddress, editLatitude, editLongitude,
            editEmail, editTelephone, editWebsite;
    private CheckBox checkIndustry, checkFun, checkEducation, checkServices;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_company);

        editName = findViewById(R.id.editName);
        editAddress = findViewById(R.id.editAddress);
        editLatitude = findViewById(R.id.editLatitude);
        editLongitude = findViewById(R.id.editLongitude);
        editEmail = findViewById(R.id.editEmail);
        editTelephone = findViewById(R.id.editTelephone);
        editWebsite = findViewById(R.id.editWebsite);

        checkIndustry = findViewById(R.id.checkIndustry);
        checkFun = findViewById(R.id.checkFun);
        checkEducation = findViewById(R.id.checkEducation);
        checkServices = findViewById(R.id.checkServices);

        btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> saveCompany());
    }

    private void saveCompany() {
        if (editName.getText().toString().trim().isEmpty()) {
            editName.setError("Name is required");
            return;
        }

        Double latitude = parseDoubleOrNull(editLatitude.getText().toString());
        Double longitude = parseDoubleOrNull(editLongitude.getText().toString());
        if (latitude == null) {
            editLatitude.setError("Enter a valid latitude");
            return;
        }
        if (longitude == null) {
            editLongitude.setError("Enter a valid longitude");
            return;
        }

        List<String> categories = new ArrayList<>();
        if (checkIndustry.isChecked()) categories.add("Industry");
        if (checkFun.isChecked()) categories.add("Fun");
        if (checkEducation.isChecked()) categories.add("Education");
        if (checkServices.isChecked()) categories.add("Services");

        if (categories.isEmpty()) {
            Toast.makeText(this, "Select at least one category", Toast.LENGTH_SHORT).show();
            return;
        }

        Company company = new Company();
        company.setName(editName.getText().toString().trim());
        company.setAddress(editAddress.getText().toString().trim());
        company.setLatitude(latitude);
        company.setLongitude(longitude);
        company.setEmail(editEmail.getText().toString().trim());
        company.setTelephone(editTelephone.getText().toString().trim());
        company.setWebsite(editWebsite.getText().toString().trim());
        company.setCategory(String.join(",", categories));

        ApiClient.getCompanyService().addCompany(company).enqueue(new Callback<List<Company>>() {
            @Override
            public void onResponse(Call<List<Company>> call, Response<List<Company>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddCompanyActivity.this,
                            "Company saved successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddCompanyActivity.this,
                            "Failed to save company (HTTP " + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Company>> call, Throwable t) {
                Toast.makeText(AddCompanyActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Double parseDoubleOrNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
