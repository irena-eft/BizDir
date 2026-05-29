package com.example.bizdir;

import java.util.List;
public class ApiResponse {
    private boolean success;
    private String message;
    private List<Company> data;
    private int id;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public List<Company> getData() { return data; }
    public int getId() { return id; }
}
