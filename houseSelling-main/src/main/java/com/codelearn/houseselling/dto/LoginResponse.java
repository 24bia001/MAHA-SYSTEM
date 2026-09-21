package com.codelearn.houseselling.dto;

public class LoginResponse {

    private Long sellerId;
    private String name;
    private String email;
    private String token;
    private String tokenType;
    private String message;
    private String image;

    public LoginResponse() {
    }

    public LoginResponse(
            Long sellerId,
            String name,
            String email,
            String token,
            String tokenType,
            String message) {

        this.sellerId = sellerId;
        this.name = name;
        this.email = email;
        this.token = token;
        this.tokenType = tokenType;
        this.message = message;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }


    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}