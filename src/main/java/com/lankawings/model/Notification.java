package com.lankawings.model;
import java.sql.Timestamp;
public class Notification {
    private int notificationId; private String title,message,type; private boolean read; private Timestamp createdAt;
    public int getNotificationId(){return notificationId;} public void setNotificationId(int v){notificationId=v;}
    public String getTitle(){return title;} public void setTitle(String v){title=v;}
    public String getMessage(){return message;} public void setMessage(String v){message=v;}
    public String getType(){return type;} public void setType(String v){type=v;}
    public boolean isRead(){return read;} public void setRead(boolean v){read=v;}
    public Timestamp getCreatedAt(){return createdAt;} public void setCreatedAt(Timestamp v){createdAt=v;}
}
