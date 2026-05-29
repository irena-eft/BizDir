package com.example.bizdir;

public class Company {

    // Boxed Integer so that when we create a new Company on the phone the id
    // stays null and Gson omits it from the JSON. Supabase then assigns the
    // real id from its sequence.
    private Integer id;
    private String name;
    private String address;
    private double latitude;
    private double longitude;
    private String email;
    private String telephone;
    private String website;
    private String category;
    private String icon_url;

    public Company() {}

    public int getId() { return id == null ? 0 : id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getIconUrl() { return icon_url; }
    public void setIconUrl(String icon_url) { this.icon_url = icon_url; }
}
