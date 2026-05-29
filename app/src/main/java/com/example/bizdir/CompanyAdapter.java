package com.example.bizdir;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
public class CompanyAdapter extends BaseAdapter implements Filterable {



    private Context context;
    private List<Company> companyList;
    private List<Company> companyListFull;

    public CompanyAdapter(Context context, List<Company> companyList) {
        this.context = context;
        this.companyList = companyList;
        this.companyListFull = new ArrayList<>(companyList);
    }

    @Override
    public int getCount() { return companyList.size(); }

    @Override
    public Object getItem(int position) { return companyList.get(position); }

    @Override
    public long getItemId(int position) { return companyList.get(position).getId(); }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_company, parent, false);
            holder = new ViewHolder();
            holder.icon = convertView.findViewById(R.id.companyIcon);
            holder.name = convertView.findViewById(R.id.companyName);
            holder.address = convertView.findViewById(R.id.companyAddress);
            holder.phone = convertView.findViewById(R.id.companyPhone);
            holder.website = convertView.findViewById(R.id.companyWebsite);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Company company = companyList.get(position);

        holder.name.setText(company.getName());
        holder.address.setText(company.getAddress());
        holder.phone.setText(company.getTelephone());
        holder.website.setText(company.getWebsite());

        // Постави икона според категорија
        int iconRes = getIconForCategory(company.getCategory());
        holder.icon.setImageResource(iconRes);

        return convertView;
    }

    private int getIconForCategory(String category) {
        if (category == null) return R.drawable.ic_default;
        switch (category.toLowerCase()) {
            case "services": return R.drawable.ic_services;
            case "fun": return R.drawable.ic_fun;
            case "industry": return R.drawable.ic_industry;
            case "education": return R.drawable.ic_education;
            default: return R.drawable.ic_default;
        }
    }

    @Override
    public Filter getFilter() {
        return companyFilter;
    }

    private Filter companyFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Company> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(companyListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Company company : companyListFull) {
                    if (company.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(company);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            companyList.clear();
            @SuppressWarnings("unchecked")
            List<Company> filtered = (List<Company>) results.values;
            companyList.addAll(filtered);
            notifyDataSetChanged();
        }
    };

    public void updateList(List<Company> newList) {
        companyList.clear();
        companyList.addAll(newList);
        companyListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    static class ViewHolder {
        ImageView icon;
        TextView name;
        TextView address;
        TextView phone;
        TextView website;
    }
}
