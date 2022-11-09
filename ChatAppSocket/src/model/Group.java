/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

/**
 *
 * @author Hi
 */
public class Group implements Serializable{
    private static final long serialVersionUID = 2L;
    private int groupId;
    private String nameGroup;
    private ArrayList<Member> listMembers;
    private ArrayList<User> listUser;
    private ArrayList<Message> listMessages;

    public ArrayList<User> getListUser() {
        return listUser;
    }

    public void setListUser(ArrayList<User> listUser) {
        this.listUser = listUser;
    }

    public Group() {
    }

    public Group(String nameGroup) {
        this.nameGroup = nameGroup;
    }

    public Group(int groupId, String nameGroup) {
        this.groupId = groupId;
        this.nameGroup = nameGroup;
    }
    
    public Group(int groupId, String nameGroup, ArrayList<Member> listMembers) {
        this.groupId = groupId;
        this.nameGroup = nameGroup;
        this.listMembers = listMembers;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setListMessages(ArrayList<Message> listMessages) {
        this.listMessages = listMessages;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getNameGroup() {
        return nameGroup;
    }

    public void setNameGroup(String nameGroup) {
        this.nameGroup = nameGroup;
    }

    public ArrayList<Member> getListMembers() {
        return listMembers;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Group other = (Group) obj;
        if (!Objects.equals(this.listMessages, other.listMessages)) {
            return false;
        }
        return true;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public ArrayList<Message> getListMessages() {
        return listMessages;
    }

    @Override
    public String toString() {
        return "Group{" + "groupId=" + groupId + ", nameGroup=" + nameGroup + '}';
    }

    public void setListMembers(ArrayList<Member> listMembers) {
        this.listMembers = listMembers;
    }


}
