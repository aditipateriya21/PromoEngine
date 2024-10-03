package com.nextuple.promoengine.dto;
import lombok.Data;

@Data
public class RegistrationResponse {
    private String message;

    public RegistrationResponse(String message) {
        this.message = message;
    }
}