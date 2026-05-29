package com.example.bizdir;

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

        loadCompanies();
        return view;
    }

    public void loadCompanies() {
        CompanyService service = ApiClient.getCompanyService();
        Call<ApiResponse> call = service.getCompanies(category, null);

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    companyList.clear();
                    companyList.addAll(response.body().getData());
                    adapter.updateList(companyList);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void filter(String query) {
        adapter.getFilter().filter(query);
    }
}
