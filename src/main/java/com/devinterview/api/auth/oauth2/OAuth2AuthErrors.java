package com.devinterview.api.auth.oauth2;

import com.devinterview.api.common.exception.CustomException;
import com.devinterview.api.common.exception.ErrorCode;

public final class OAuth2AuthErrors {

    private OAuth2AuthErrors() {
    }

    public static String resolveLoginErrorCode(Throwable throwable) {
        CustomException customException = findCustomException(throwable);
        if (customException == null) {
            return "oauth2_failed";
        }
        return mapErrorCode(customException.getErrorCode());
    }

    private static CustomException findCustomException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof CustomException customException) {
                return customException;
            }
            current = current.getCause();
        }
        return null;
    }

    private static String mapErrorCode(ErrorCode errorCode) {
        if (errorCode == ErrorCode.EMAIL_ALREADY_EXISTS) {
            return "email_already_exists";
        }
        if (errorCode == ErrorCode.ACCOUNT_WITHDRAWN) {
            return "account_withdrawn";
        }
        return "oauth2_failed";
    }
}
