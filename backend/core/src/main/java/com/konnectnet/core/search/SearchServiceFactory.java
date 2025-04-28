package com.konnectnet.core.search;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SearchServiceFactory {

    private final LuceneSearchService luceneSearchService;

    @Value("${search.engine:lucene}")
    private String searchEngine;

    public SearchServiceFactory(LuceneSearchService luceneSearchService) {
        this.luceneSearchService = luceneSearchService;
    }

    public SearchService getSearchService() {
        if ("lucene".equalsIgnoreCase(searchEngine)) {
            return luceneSearchService;
        } else {
            // Later add ElasticsearchSearchService logic here
            throw new UnsupportedOperationException("Elasticsearch support not implemented yet.");
        }
    }
}
