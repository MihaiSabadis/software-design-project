package com.andrei.demo.model;

public record LoginResponse(
        Boolean success,
        String role,
        String errorMessage,
        String token,
        String userId
) {
    public LoginResponse(String errorMessage){
        this(false, null, errorMessage, null, null);
    }
    public LoginResponse(String role, String token,String userId){
        this(true, role, null, token,userId);
    }
}