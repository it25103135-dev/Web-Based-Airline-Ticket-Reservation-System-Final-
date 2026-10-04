package com.lankawings.model;

import java.sql.Timestamp;

public class User {
    private int userId;
    private String fullName, username, email, phone, role, status;
    private Timestamp createdAt;

    public int getUserId() { return userId; }
    public void setUserId(int v) { userId = v; }
    public String getFullName() { return fullName; }
    public void setFullName(String v) { fullName = v; }
    public String getUsername() { return username; }
    public void setUsername(String v) { username = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { email = v; }
    public String getPhone() { return phone; }
    public void setPhone(String v) { phone = v; }
    public String getRole() { return role; }
    public void setRole(String v) { role = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { status = v; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp v) { createdAt = v; }
}
