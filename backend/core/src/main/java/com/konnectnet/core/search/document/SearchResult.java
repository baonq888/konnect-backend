package com.konnectnet.core.search.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class SearchResult {
    private UUID postId;
    private String content;
    private String user;
    private long totalResults;
    private int currentPage;
    private int totalPages;
}
