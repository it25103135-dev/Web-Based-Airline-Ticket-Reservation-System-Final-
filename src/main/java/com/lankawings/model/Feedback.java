package com.lankawings.model;
import java.sql.Timestamp;
public class Feedback {
    private int feedbackId,userId,rating; private String username,subject,message,adminResponse,status; private Timestamp createdAt;
    public int getFeedbackId(){return feedbackId;} public void setFeedbackId(int v){feedbackId=v;}
    public int getUserId(){return userId;} public void setUserId(int v){userId=v;}
    public int getRating(){return rating;} public void setRating(int v){rating=v;}
    public String getUsername(){return username;} public void setUsername(String v){username=v;}
    public String getSubject(){return subject;} public void setSubject(String v){subject=v;}
    public String getMessage(){return message;} public void setMessage(String v){message=v;}
    public String getAdminResponse(){return adminResponse;} public void setAdminResponse(String v){adminResponse=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public Timestamp getCreatedAt(){return createdAt;} public void setCreatedAt(Timestamp v){createdAt=v;}
}
