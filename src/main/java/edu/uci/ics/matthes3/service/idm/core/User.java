package edu.uci.ics.matthes3.service.idm.core;

public class User {
    private int id;
    private String email;
    private int status;
    private int plevel;
    private String salt;
    private String pword;

    public User() {
    }

    public User(int id, String email, int status, int plevel, String salt, String pword) {
        this.id = id;
        this.email = email;
        this.status = status;
        this.plevel = plevel;
        this.salt = salt;
        this.pword = pword;
    }

    public String toString() {
        return "ID: " + id + ", email: " + email + ", status: " + status + ", privilege level: " + plevel;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getPlevel() {
        return plevel;
    }

    public void setPlevel(int plevel) {
        this.plevel = plevel;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getPword() {
        return pword;
    }

    public void setPword(String pword) {
        this.pword = pword;
    }
}
