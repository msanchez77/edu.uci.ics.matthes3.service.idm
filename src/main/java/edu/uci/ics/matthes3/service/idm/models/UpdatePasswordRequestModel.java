package edu.uci.ics.matthes3.service.idm.models;

public class UpdatePasswordRequestModel extends RequestModel {

    private String email;
    private char[] oldpword;
    private char[] newpword;

    public UpdatePasswordRequestModel() {
    }

    public UpdatePasswordRequestModel(String email, char[] oldpword, char[] newpword) {
        this.email = email;
        this.oldpword = oldpword;
        this.newpword = newpword;
    }

    public String getEmail() {
        return email;
    }

    public char[] getOldpword() {
        return oldpword;
    }

    public char[] getNewpword() {
        return newpword;
    }
}
