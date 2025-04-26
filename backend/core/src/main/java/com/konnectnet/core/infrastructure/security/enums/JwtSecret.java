package com.konnectnet.core.infrastructure.security.enums;

import lombok.Getter;

@Getter
public enum JwtSecret {

    SECRET_KEY("YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXo0NTY3ODkwMTIzNDU2Nzg5MA");

    private final String key;

    JwtSecret(String key) {
        this.key = key;
    }

}
