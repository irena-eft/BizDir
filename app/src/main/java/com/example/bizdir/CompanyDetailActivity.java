package com.example.bizdir;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CompanyDetailActivity extends AppCompatActivity {

    public static final String EXTRA_COMPANY = "company";

    private Company currentCompany;

    private final ActivityResultLauncher<Intent> editLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    setResult(RESULT_OK);
                    if (result.getData() != null) {
                        Company updated = (Company) result.getData()
                                .getSerializableExtra(AddCompanyActivity.EXTRA_UPDATED_COMPANY);
                        if (updated != null) {
                            currentCompany = updated;
                            bindCompany(updated);
                            if (getSupportActionBar() != null) {
                                getSupportActionBar().setTitle(updated.getName());
                            }
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_detail);

        Toolbar toolbar = findViewById(R.id.detailToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        currentCompany = (Company) getIntent().getSerializableExtra(EXTRA_COMPANY);
        if (currentCompany == null) {
            finish();
            return;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(currentCompany.getName());
        }

        bindCompany(currentCompany);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit) {
            Intent intent = new Intent(this, AddCompanyActivity.class);
            intent.putExtra(AddCompanyActivity.EXTRA_EDIT_COMPANY, currentCompany);
            editLauncher.launch(intent);
            return true;
        }
        if (id == R.id.action_delete) {
            confirmDelete();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete " + currentCompany.getName() + "?")
                .setMessage("This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> doDelete())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void doDelete() {
        ApiClient.getCompanyService()
                .deleteCompany(currentCompany.getId())
                .enqueue(new Callback<java.util.List<Company>>() {
                    @Override
                    public void onResponse(Call<java.util.List<Company>> call,
                                           Response<java.util.List<Company>> response) {
                        boolean rowActuallyDeleted = response.isSuccessful()
                                && response.body() != null
                                && !response.body().isEmpty();

                        if (rowActuallyDeleted) {
                            Toast.makeText(CompanyDetailActivity.this,
                                    "Company deleted", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else if (response.isSuccessful()) {
                            // 2xx but empty body = Supabase RLS silently blocked it.
                            Toast.makeText(CompanyDetailActivity.this,
                                    "Delete was blocked. Re-run supabase_setup.sql to add the delete policy.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(CompanyDetailActivity.this,
                                    "Delete failed (HTTP " + response.code() + ")",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<java.util.List<Company>> call, Throwable t) {
                        Toast.makeText(CompanyDetailActivity.this,
                                "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void bindCompany(Company company) {
        ImageView icon = findViewById(R.id.detailIcon);
        TextView name = findViewById(R.id.detailName);
        TextView category = findViewById(R.id.detailCategory);
        TextView address = findViewById(R.id.detailAddress);
        TextView phone = findViewById(R.id.detailPhone);
        TextView email = findViewById(R.id.detailEmail);
        TextView website = findViewById(R.id.detailWebsite);

        String iconUrl = company.getIconUrl();
        int fallback = iconForCategory(company.getCategory());
        if (iconUrl != null && (iconUrl.startsWith("http://") || iconUrl.startsWith("https://"))) {
            Glide.with(this).load(iconUrl).placeholder(fallback).error(fallback).into(icon);
        } else {
            icon.setImageResource(fallback);
        }
        name.setText(company.getName());
        category.setText(company.getCategory());

        setupRow(address, company.getAddress(),
                v -> openMap(company));

        setupRow(phone, company.getTelephone(),
                v -> openExternal(Intent.ACTION_DIAL,
                        Uri.parse("tel:" + company.getTelephone())));

        setupRow(email, company.getEmail(),
                v -> openExternal(Intent.ACTION_SENDTO,
                        Uri.parse("mailto:" + company.getEmail())));

        setupRow(website, company.getWebsite(),
                v -> openExternal(Intent.ACTION_VIEW,
                        Uri.parse(normalizeUrl(company.getWebsite()))));
    }

    private void setupRow(TextView row, String value, View.OnClickListener onClick) {
        if (value == null || value.trim().isEmpty()) {
            row.setVisibility(View.GONE);
        } else {
            row.setVisibility(View.VISIBLE);
            row.setText(value);
            row.setOnClickListener(onClick);
        }
    }

    private void openExternal(String action, Uri uri) {
        try {
            startActivity(new Intent(action, uri));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No app available to handle this", Toast.LENGTH_SHORT).show();
        }
    }

    private void openMap(Company company) {
        Uri geoUri;
        if (company.getLatitude() != 0 || company.getLongitude() != 0) {
            geoUri = Uri.parse("geo:" + company.getLatitude() + "," + company.getLongitude()
                    + "?q=" + company.getLatitude() + "," + company.getLongitude()
                    + "(" + Uri.encode(company.getName()) + ")");
        } else {
            geoUri = Uri.parse("geo:0,0?q=" + Uri.encode(company.getAddress()));
        }
        openExternal(Intent.ACTION_VIEW, geoUri);
    }

    private String normalizeUrl(String url) {
        String trimmed = url.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        return "http://" + trimmed;
    }

    private int iconForCategory(String category) {
        if (category == null) return R.drawable.ic_default;
        String lower = category.toLowerCase();
        if (lower.contains("services")) return R.drawable.ic_services;
        if (lower.contains("fun")) return R.drawable.ic_fun;
        if (lower.contains("industry")) return R.drawable.ic_industry;
        if (lower.contains("education")) return R.drawable.ic_education;
        return R.drawable.ic_default;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
