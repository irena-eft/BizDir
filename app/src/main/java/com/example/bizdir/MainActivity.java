package com.example.bizdir;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private ViewPager2 viewPager;
    private ViewPagerAdapter pagerAdapter;
    private EditText searchEditText;

    private final List<Company> companiesForProximity = new ArrayList<>();
    private final Set<Integer> notifiedCompanyIds = new HashSet<>();

    private final ActivityResultLauncher<Intent> addCompanyLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    refreshAllTabs();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.app_name));
            getSupportActionBar().setLogo(R.drawable.ic_app_logo);
        }

        viewPager = findViewById(R.id.viewPager);
        pagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(pagerAdapter.getPageTitle(position));
            switch (position) {
                case 0:
                    tab.setIcon(R.drawable.ic_tab_services);
                    break;
                case 1:
                    tab.setIcon(R.drawable.ic_tab_fun);
                    break;
                case 2:
                    tab.setIcon(R.drawable.ic_tab_industry);
                    break;
                case 3:
                    tab.setIcon(R.drawable.ic_tab_education);
                    break;
            }
        }).attach();

        searchEditText = findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                CompanyListFragment fragment = pagerAdapter.getFragment(viewPager.getCurrentItem());
                if (fragment != null) {
                    fragment.filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setupLocationUpdates();
        loadCompaniesForProximity();
        checkLocationPermission();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            addCompanyLauncher.launch(new Intent(this, AddCompanyActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Re-fetches the list for every visible tab and the proximity cache. */
    public void refreshAllTabs() {
        for (int i = 0; i < pagerAdapter.getItemCount(); i++) {
            CompanyListFragment fragment = pagerAdapter.getFragment(i);
            if (fragment != null) {
                fragment.loadCompanies();
            }
        }
        loadCompaniesForProximity();
    }

    private void loadCompaniesForProximity() {
        ApiClient.getCompanyService()
                .getCompanies(null)
                .enqueue(new Callback<List<Company>>() {
                    @Override
                    public void onResponse(Call<List<Company>> call, Response<List<Company>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            companiesForProximity.clear();
                            companiesForProximity.addAll(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Company>> call, Throwable t) {
                        // Proximity alerts are optional; tabs show their own errors.
                    }
                });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        } else {
            startLocationUpdates();
        }
    }

    private void setupLocationUpdates() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                for (Location location : result.getLocations()) {
                    checkNearbyCompanies(location);
                }
            }
        };
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY, 10000)
                .setMinUpdateIntervalMillis(5000)
                .build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback,
                    Looper.getMainLooper());
        }
    }

    private void checkNearbyCompanies(Location userLocation) {
        for (Company company : companiesForProximity) {
            float[] results = new float[1];
            Location.distanceBetween(
                    userLocation.getLatitude(), userLocation.getLongitude(),
                    company.getLatitude(), company.getLongitude(), results);

            if (results[0] < 50 && !notifiedCompanyIds.contains(company.getId())) {
                notifiedCompanyIds.add(company.getId());
                Toast.makeText(this,
                        "You are near " + company.getName() + "!",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}
