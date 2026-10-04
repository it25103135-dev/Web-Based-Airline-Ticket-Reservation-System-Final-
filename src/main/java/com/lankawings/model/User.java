package com.lankawings.model;

/** Minimal shared authentication identity. User-management CRUD is deliberately not included. */
public class User {
    private int userId;
    private String fullName, username, email, role, status;
    public int getUserId(){return userId;} public void setUserId(int v){userId=v;}
    public String getFullName(){return fullName;} public void setFullName(String v){fullName=v;}
    public String getUsername(){return username;} public void setUsername(String v){username=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;}
    public String getRole(){return role;} public void setRole(String v){role=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
}
