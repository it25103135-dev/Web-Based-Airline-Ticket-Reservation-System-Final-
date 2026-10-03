package com.lankawings.dao;

import com.lankawings.config.DBConnection;
import com.lankawings.model.User;
import com.lankawings.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Minimal account access needed by this function: authentication and passenger lookup only. */
public class AccountDAO {
    public static final class SessionInfo {
        public final String username, fullName, role, status;
        public SessionInfo(String username,String fullName,String role,String status){this.username=username;this.fullName=fullName;this.role=role;this.status=status;}
    }

    public User authenticate(String login,String password) throws SQLException {
        String q="SELECT UserID,FullName,Username,Email,Phone,PasswordHash,Role,Status FROM Users WHERE Username=? OR Email=?";
        try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(q)){
            p.setString(1,login); p.setString(2,login);
            try(ResultSet r=p.executeQuery()){
                if(!r.next()){ PasswordUtil.dummyVerify(password); return null; }
                String hash=r.getString("PasswordHash");
                if(!PasswordUtil.verify(password,hash) || !"ACTIVE".equals(r.getString("Status"))) return null;
                return map(r);
            }
        }
    }

    public SessionInfo sessionInfo(int userId) throws SQLException {
        try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement("SELECT Username,FullName,Role,Status FROM Users WHERE UserID=?")){
            p.setInt(1,userId); try(ResultSet r=p.executeQuery()){
                return r.next()?new SessionInfo(r.getString(1),r.getString(2),r.getString(3),r.getString(4)):null;
            }
        }
    }

    public List<User> activePassengers() throws SQLException {
        List<User> list=new ArrayList<>();
        try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement("SELECT UserID,FullName,Username,Email,Phone,Role,Status FROM Users WHERE Role='PASSENGER' AND Status='ACTIVE' ORDER BY FullName"); ResultSet r=p.executeQuery()){
            while(r.next()) list.add(mapPublic(r));
        }
        return list;
    }

    public User findActivePassenger(int id) throws SQLException {
        try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement("SELECT UserID,FullName,Username,Email,Phone,Role,Status FROM Users WHERE UserID=? AND Role='PASSENGER' AND Status='ACTIVE'")){
            p.setInt(1,id); try(ResultSet r=p.executeQuery()){ return r.next()?mapPublic(r):null; }
        }
    }

    private User map(ResultSet r) throws SQLException { return mapPublic(r); }
    private User mapPublic(ResultSet r) throws SQLException {
        User u=new User(); u.setUserId(r.getInt("UserID")); u.setFullName(r.getString("FullName")); u.setUsername(r.getString("Username")); u.setEmail(r.getString("Email")); u.setPhone(r.getString("Phone")); u.setRole(r.getString("Role")); u.setStatus(r.getString("Status")); return u;
    }
}
