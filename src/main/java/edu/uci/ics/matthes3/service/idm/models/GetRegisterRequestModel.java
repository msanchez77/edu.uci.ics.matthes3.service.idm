package edu.uci.ics.matthes3.service.idm.models;

public class GetRegisterRequestModel {
    private String email;
    private char[] password;

    public GetRegisterRequestModel() {
        this.email = null;
        this.password = null;
    }

    public GetRegisterRequestModel(String email, char[] password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public char[] getPassword() {
        return password;
    }

    public void setPassword(char[] password) {
        this.password = password;
    }
}
