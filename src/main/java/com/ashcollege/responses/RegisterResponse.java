
package com.ashcollege.responses;

public class RegisterResponse extends BasicResponse {
    private String token;



    public RegisterResponse(boolean success, Integer errorCode, String token) {
        super(success, errorCode);
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}