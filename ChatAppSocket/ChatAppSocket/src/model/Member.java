/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author Hi
 */
public class Member implements Serializable{
        private static final long serialVersionUID = 3L;

    private int userId;
    private int groupId;
    private Date offTime;
    private boolean isAdmin;
    public Member() {
    }

    public Member(int userId, int groupId, boolean admin) {
        this.userId = userId;
        this.groupId = groupId;
        this.isAdmin = admin;
    }
   
    public Member(int userId, int groupId, Date offTime, boolean isAdmin) {
        this.userId = userId;
        this.groupId = groupId;
        this.offTime = offTime;
        this.isAdmin = isAdmin;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public Date getOffTime() {
        return offTime;
    }

    public void setOffTime(Date offTime) {
        this.offTime = offTime;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}