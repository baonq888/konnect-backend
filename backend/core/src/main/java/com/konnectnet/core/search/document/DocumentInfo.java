package com.konnectnet.core.search.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DocumentInfo {
    private String id;
    private String content;
    private String user;
}
