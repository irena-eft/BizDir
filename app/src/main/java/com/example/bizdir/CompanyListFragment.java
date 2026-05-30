package com.example.bizdir;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CompanyListFragment extends Fragment {

    private static final String ARG_CATEGORY = "category";

    private String category;
    private ListView listView;
    private CompanyAdapter adapter;
    private List<Company> companyList;

    public static CompanyListFragment newInstance(String category) {
        CompanyListFragment fragment = new CompanyListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            category = getArguments().getString(ARG_CATEGORY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_company_list, container, false);
        listView = view.findViewById(R.id.listView);
        companyList = new ArrayList<>();
        adapter = new CompanyAdapter(requireContext(), companyList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, itemView, position, id) -> {
            Company company = (Company) adapter.getItem(position);
            Intent intent = new Intent(requireContext(), CompanyDetailActivity.class);
            intent.putExtra(CompanyDetailActivity.EXTRA_COMPANY, company);
            startActivity(intent);
        });

        loadCompanies();
        return view;
    }

    public void loadCompanies() {
        ApiClient.getCompanyService()
                .getCompanies("*", ApiClient.categoryFilter(category), "name.asc")
                .enqueue(new Callback<List<Company>>() {
                    @Override
                    public void onResponse(Call<List<Company>> call, Response<List<Company>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            adapter.updateList(response.body());
                        } else if (getContext() != null) {
                            Toast.makeText(getContext(),
                                    "Server returned " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Company>> call, Throwable t) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(),
                                    "Error: " + t.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void filter(String query) {
        adapter.getFilter().filter(query);
    }
}
