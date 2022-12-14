package com.api.parkingcontrol.exception;

import org.aspectj.bridge.Message;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class BadRequestException extends RuntimeException{
    public BadRequestException(String message){
        super(message);
    }
}
