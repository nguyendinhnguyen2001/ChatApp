package model;

import java.io.Serializable;

/**
 * STATUS:
 * 0 : đang chờ kết bạn
 * 1: bạn bè
 */
public class Mutual implements Serializable{
        private static final long serialVersionUID = 5L;

    private int mutual_id;
    private int user1_id;
    private int user2_id;
    private int status;
    private String user1_nickname;
    private String user2_nickname;

    public Mutual(int user1_id, int user2_id, int status) {
        this.user1_id = user1_id;
        this.user2_id = user2_id;
        this.status = status;
    }

    public int getMutual_id() {
        return mutual_id;
    }

    public void setMutual_id(int mutual_id) {
        this.mutual_id = mutual_id;
    }

    public int getUser1_id() {
        return user1_id;
    }

    public void setUser1_id(int user1_id) {
        this.user1_id = user1_id;
    }

    public int getUser2_id() {
        return user2_id;
    }

    public void setUser2_id(int user2_id) {
        this.user2_id = user2_id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getUser1_nickname() {
        return user1_nickname;
    }

    public void setUser1_nickname(String user1_nickname) {
        this.user1_nickname = user1_nickname;
    }

    public String getUser2_nickname() {
        return user2_nickname;
    }

    public void setUser2_nickname(String user2_nickname) {
        this.user2_nickname = user2_nickname;
    }

    @Override
    public String toString() {
        return "Mutual{" +
                "mutual_id=" + mutual_id +
                ", user1_id=" + user1_id +
                ", user2_id=" + user2_id +
                ", status=" + status +
                ", user1_nickname='" + user1_nickname + '\'' +
                ", user2_nickname='" + user2_nickname + '\'' +
                '}';
    }
}
