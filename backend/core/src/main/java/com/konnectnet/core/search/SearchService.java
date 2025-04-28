package com.konnectnet.core.search;

import com.konnectnet.core.search.document.SearchResult;
import java.io.IOException;
import java.util.List;

public interface SearchService {
    List<SearchResult> search(String searchTerm, int pageNumber, int pageSize) throws IOException;
}
