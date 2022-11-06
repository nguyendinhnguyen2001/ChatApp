/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Hi
 */
public class User implements Serializable{
    
    private static final long serialVersionUID = 1L;
    private int userId;
    private String name;
    private String userName;
    private String passWord;
    private String address;
    private int activeStatus;
    private List<User> listFriend;
    private List<Group> listGroup;
    

    public User() {
        listFriend = new ArrayList<>();
    }

    public User(String name, String userName, String passWord, String address, int activeStatus) {
        this.name = name;
        this.userName = userName;
        this.passWord = passWord;
        this.address = address;
        this.activeStatus = activeStatus;
    }

    public User(int userId, String name, String userName, String passWord, String address, int activeStatus) {
        this.userId = userId;
        this.name = name;
        this.userName = userName;
        this.passWord = passWord;
        this.address = address;
        this.activeStatus = activeStatus;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(int activeStatus) {
        this.activeStatus = activeStatus;
    }

    public List<User> getListFriend() {
        return listFriend;
    }

    public void setListFriend(List<User> listFriend) {
        this.listFriend = listFriend;
    }

    public List<Group> getListGroup() {
        return listGroup;
    }

    public void setListGroup(List<Group> listGroup) {
        this.listGroup = listGroup;
    }
    


    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", userName='" + userName + '\'' +
                ", passWord='" + passWord + '\'' +
                ", address='" + address + '\'' +
                ", activeStatus=" + activeStatus +
                '}';
    }  
}

