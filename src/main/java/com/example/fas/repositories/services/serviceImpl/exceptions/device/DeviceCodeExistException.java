package com.example.fas.repositories.services.serviceImpl.exceptions.device;

public class DeviceCodeExistException extends RuntimeException {
    public DeviceCodeExistException(String message) {
        super(message);
    }
}
