package com.example.bizdir;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddCompanyActivity extends AppCompatActivity {

    public static final String EXTRA_EDIT_COMPANY = "edit_company";
    public static final String EXTRA_UPDATED_COMPANY = "updated_company";

    private static final int LOCATION_PERMISSION_REQUEST = 2001;

    private EditText editName, editAddress, editLatitude, editLongitude,
            editEmail, editTelephone, editWebsite;
    private CheckBox checkIndustry, checkFun, checkEducation, checkServices;
    private Button btnSave, btnPickImage, btnUseMyLocation;
    private ImageView imagePreview;

    private FusedLocationProviderClient fusedLocationClient;
    private byte[] selectedImageBytes;
    private String selectedImageMime;

    /** Non-null while we're editing an existing company; null when adding a new one. */
    private Company editingCompany;

    private final ActivityResultLauncher<PickVisualMediaRequest> photoPicker =
            registerForActivityResult(
                    new ActivityResultContracts.PickVisualMedia(),
                    uri -> {
                        if (uri != null) {
                            loadPickedImage(uri);
                        }
                    });

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
        btnPickImage = findViewById(R.id.btnPickImage);
        btnUseMyLocation = findViewById(R.id.btnUseMyLocation);
        imagePreview = findViewById(R.id.imagePreview);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        editingCompany = (Company) getIntent().getSerializableExtra(EXTRA_EDIT_COMPANY);
        if (editingCompany != null) {
            setTitle("Edit company");
            btnSave.setText("UPDATE");
            prefillFromCompany(editingCompany);
        }

        btnSave.setOnClickListener(v -> saveCompany());

        btnPickImage.setOnClickListener(v -> photoPicker.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()));

        btnUseMyLocation.setOnClickListener(v -> fetchCurrentLocation());
    }

    private void prefillFromCompany(Company c) {
        editName.setText(c.getName());
        editAddress.setText(c.getAddress());
        editLatitude.setText(String.valueOf(c.getLatitude()));
        editLongitude.setText(String.valueOf(c.getLongitude()));
        editEmail.setText(c.getEmail());
        editTelephone.setText(c.getTelephone());
        editWebsite.setText(c.getWebsite());

        String category = c.getCategory();
        if (category != null) {
            checkIndustry.setChecked(category.contains("Industry"));
            checkFun.setChecked(category.contains("Fun"));
            checkEducation.setChecked(category.contains("Education"));
            checkServices.setChecked(category.contains("Services"));
        }

        String iconUrl = c.getIconUrl();
        if (iconUrl != null && (iconUrl.startsWith("http://") || iconUrl.startsWith("https://"))) {
            Glide.with(this).load(iconUrl).placeholder(R.drawable.ic_default).into(imagePreview);
        }
    }

    private void loadPickedImage(Uri uri) {
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            if (in == null) {
                Toast.makeText(this, "Could not read image", Toast.LENGTH_SHORT).show();
                return;
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            selectedImageBytes = out.toByteArray();
            selectedImageMime = getContentResolver().getType(uri);
            if (selectedImageMime == null) selectedImageMime = "image/jpeg";

            Glide.with(this).load(uri).into(imagePreview);
        } catch (IOException e) {
            Toast.makeText(this, "Could not read image: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            return;
        }

        btnUseMyLocation.setEnabled(false);
        btnUseMyLocation.setText("Locating...");

        fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        new CancellationTokenSource().getToken())
                .addOnSuccessListener(location -> {
                    btnUseMyLocation.setEnabled(true);
                    btnUseMyLocation.setText("Use my current location");
                    if (location != null) {
                        editLatitude.setText(String.valueOf(location.getLatitude()));
                        editLongitude.setText(String.valueOf(location.getLongitude()));
                    } else {
                        Toast.makeText(this,
                                "Could not get location. Try going outside or check GPS.",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    btnUseMyLocation.setEnabled(true);
                    btnUseMyLocation.setText("Use my current location");
                    Toast.makeText(this, "Location error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchCurrentLocation();
        }
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

        // When editing, keep the existing icon_url unless the user picked a new
        // image (which will overwrite icon_url after upload below).
        if (editingCompany != null) {
            company.setIconUrl(editingCompany.getIconUrl());
        }

        if (selectedImageBytes != null) {
            btnSave.setEnabled(false);
            btnSave.setText("Uploading image...");
            ApiClient.uploadImage(selectedImageBytes, selectedImageMime,
                    new ApiClient.UploadCallback() {
                        @Override
                        public void onSuccess(String publicUrl) {
                            company.setIconUrl(publicUrl);
                            sendCompanyToServer(company);
                        }

                        @Override
                        public void onError(String message) {
                            resetSaveButton();
                            Toast.makeText(AddCompanyActivity.this,
                                    "Image upload failed: " + message,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            sendCompanyToServer(company);
        }
    }

    private void sendCompanyToServer(Company company) {
        btnSave.setEnabled(false);
        btnSave.setText(editingCompany != null ? "Updating..." : "Saving...");

        if (editingCompany != null) {
            int existingId = editingCompany.getId();
            company.clearId();
            ApiClient.getCompanyService()
                    .updateCompany(existingId, company)
                    .enqueue(saveCallback(/* isUpdate */ true));
        } else {
            ApiClient.getCompanyService()
                    .addCompany(company)
                    .enqueue(saveCallback(/* isUpdate */ false));
        }
    }

    private Callback<List<Company>> saveCallback(boolean isUpdate) {
        return new Callback<List<Company>>() {
            @Override
            public void onResponse(Call<List<Company>> call, Response<List<Company>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddCompanyActivity.this,
                            isUpdate ? "Company updated!" : "Company saved!",
                            Toast.LENGTH_SHORT).show();

                    Intent data = new Intent();
                    if (isUpdate && response.body() != null && !response.body().isEmpty()) {
                        data.putExtra(EXTRA_UPDATED_COMPANY, response.body().get(0));
                    }
                    setResult(RESULT_OK, data);
                    finish();
                } else {
                    resetSaveButton();
                    Toast.makeText(AddCompanyActivity.this,
                            "Failed to save (HTTP " + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Company>> call, Throwable t) {
                resetSaveButton();
                Toast.makeText(AddCompanyActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void resetSaveButton() {
        btnSave.setEnabled(true);
        btnSave.setText(editingCompany != null ? "UPDATE" : "SAVE");
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
