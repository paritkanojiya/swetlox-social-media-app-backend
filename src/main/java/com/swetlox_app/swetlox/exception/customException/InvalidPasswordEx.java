package com.swetlox_app.swetlox.exception.customException;

public class InvalidPasswordEx extends RuntimeException {
    public InvalidPasswordEx(String string) {
        super(string);
    }
}
