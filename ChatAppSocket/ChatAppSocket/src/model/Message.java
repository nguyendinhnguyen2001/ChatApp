/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Hi
 */
public class Message implements Serializable{
        private static final long serialVersionUID = 4L;
    private int messId;
    private User user;
    private String message;
    private Date timeSend;
    private int groupId;
    public Message() {
    }

    public Message(int messId, User fromUser, String message, int groupId) {
        this.messId = messId;
        this.user = fromUser;
        this.message = message;
        this.groupId = groupId;
    }

    public Message(int messId, User fromUser, String message, Date timeSend, int groupId) {
        this.messId = messId;
        this.user = fromUser;
        this.message = message;
        this.timeSend = timeSend;
        this.groupId = groupId;
    }

    public int getMessId() {
        return messId;
    }

    public void setMessId(int messId) {
        this.messId = messId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getTimeSend() {
        return timeSend;
    }

    public void setTimeSend(Date timeSend) {
        this.timeSend = timeSend;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    
    @Override
    public String toString() {
        return "Message{" +
                "messId=" + messId +
                ", fromUser=" + user +
                ", message='" + message + '\'' +
                ", timeSend=" + timeSend +
                ", groupId=" + groupId +
                '}';
    }
}
