package com.konnectnet.core.search;

import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class LuceneSearchService implements SearchService{
    @Override
    public void indexDocument(String id, String content) throws IOException {

    }

    @Override
    public String search(String query) throws IOException {
        return "";
    }

    @Override
    public void deleteDocument(String id) throws IOException {

    }
}
