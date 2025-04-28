package com.konnectnet.core.search;

import java.io.IOException;

public interface SearchService {
    void indexDocument(String id, String content) throws IOException;
    String search(String query) throws IOException;
    void deleteDocument(String id) throws IOException;
}
