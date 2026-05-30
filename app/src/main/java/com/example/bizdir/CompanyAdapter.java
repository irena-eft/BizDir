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

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class CompanyAdapter extends BaseAdapter implements Filterable {

    private final Context context;
    private final List<Company> companyList;
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

        loadCompanyIcon(holder.icon, company);

        return convertView;
    }

    /**
     * If the company has a real image URL stored, load it with Glide.
     * Otherwise fall back to one of our built-in category icons.
     */
    static void loadCompanyIcon(ImageView target, Company company) {
        String url = company.getIconUrl();
        int fallback = iconForCategory(company.getCategory());

        if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
            Glide.with(target.getContext())
                    .load(url)
                    .placeholder(fallback)
                    .error(fallback)
                    .into(target);
        } else {
            Glide.with(target.getContext()).clear(target);
            target.setImageResource(fallback);
        }
    }

    private static int iconForCategory(String category) {
        if (category == null) return R.drawable.ic_default;
        String lower = category.toLowerCase();
        if (lower.contains("services")) return R.drawable.ic_services;
        if (lower.contains("fun")) return R.drawable.ic_fun;
        if (lower.contains("industry")) return R.drawable.ic_industry;
        if (lower.contains("education")) return R.drawable.ic_education;
        return R.drawable.ic_default;
    }

    @Override
    public Filter getFilter() {
        return companyFilter;
    }

    private final Filter companyFilter = new Filter() {
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
