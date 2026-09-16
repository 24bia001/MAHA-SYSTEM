package com.codelearn.houseselling.dto;

public class ManagementLoginResponse {

    private Long managementId;
    private String name;
    private String email;
    private String role;
    private String token;
    private String tokenType;
    private String message;

    public ManagementLoginResponse() {
    }

    public ManagementLoginResponse(
            Long managementId,
            String name,
            String email,
            String role,
            String token,
            String tokenType,
            String message) {

        this.managementId =
                managementId;

        this.name = name;

        this.email = email;

        this.role = role;

        this.token = token;

        this.tokenType =
                tokenType;

        this.message = message;
    }

    public Long getManagementId() {
        return managementId;
    }

    public void setManagementId(
            Long managementId) {

        this.managementId =
                managementId;
    }

    public String getName() {
        return name;
    }

    public void setName(
            String name) {

        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(
            String email) {

        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(
            String role) {

        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(
            String token) {

        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(
            String tokenType) {

        this.tokenType =
                tokenType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(
            String message) {

        this.message = message;
    }
}