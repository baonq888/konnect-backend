package com.konnectnet.core.search.document;

import lombok.Getter;

@Getter
public enum DocumentField {
    ID("id"),
    CONTENT("content"),
    USER("user");

    private final String fieldName;

    DocumentField(String fieldName) {
        this.fieldName = fieldName;
    }
}