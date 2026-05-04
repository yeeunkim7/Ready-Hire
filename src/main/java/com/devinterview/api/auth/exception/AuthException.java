package com.devinterview.api.auth.exception;

import com.devinterview.api.common.exception.CustomException;
import com.devinterview.api.common.exception.ErrorCode;

public class AuthException extends CustomException {

    public AuthException(String message) {
        super(ErrorCode.AUTH_ERROR, message);
    }
}
