package com.lankawings.model;

import java.sql.Timestamp;

public class UserAudit {
    private int auditId, actorUserId, targetUserId;
    private String actorUsername, targetUsername, action, details;
    private Timestamp createdAt;

    public int getAuditId() { return auditId; }
    public void setAuditId(int v) { auditId = v; }
    public int getActorUserId() { return actorUserId; }
    public void setActorUserId(int v) { actorUserId = v; }
    public int getTargetUserId() { return targetUserId; }
    public void setTargetUserId(int v) { targetUserId = v; }
    public String getActorUsername() { return actorUsername; }
    public void setActorUsername(String v) { actorUsername = v; }
    public String getTargetUsername() { return targetUsername; }
    public void setTargetUsername(String v) { targetUsername = v; }
    public String getAction() { return action; }
    public void setAction(String v) { action = v; }
    public String getDetails() { return details; }
    public void setDetails(String v) { details = v; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp v) { createdAt = v; }
}
