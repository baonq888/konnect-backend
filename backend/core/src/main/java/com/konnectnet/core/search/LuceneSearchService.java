package com.konnectnet.core.search;

import com.konnectnet.core.search.document.DocumentField;
import com.konnectnet.core.search.document.DocumentInfo;
import com.konnectnet.core.search.document.SearchResult;
import com.konnectnet.core.search.index.IndexService;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LuceneSearchService implements SearchService {

    @Value("${search.lucene.dir}")
    private String luceneDir;

    private final IndexService indexService;


    @Override
    public List<SearchResult> search(String searchTerm, int pageNumber, int pageSize) throws IOException {
        try (DirectoryReader reader = DirectoryReader.open(indexService.getDirectory())) {
            IndexSearcher searcher = new IndexSearcher(reader);

            if (pageNumber < 0 || pageSize < 1) {
                throw new IllegalArgumentException("Page number must be >= 0 and page size must be >= 1");
            }
            int page = pageNumber + 1;

            try {
                // Build queries
                Query contentQuery = new QueryParser(DocumentField.CONTENT.getFieldName(), new StandardAnalyzer()).parse(searchTerm);
                Query userQuery = new QueryParser(DocumentField.USER.getFieldName(), new StandardAnalyzer()).parse(searchTerm);

                // Combine the queries using BooleanQuery
                BooleanQuery combinedQuery = new BooleanQuery.Builder()
                        .add(contentQuery, BooleanClause.Occur.SHOULD)
                        .add(userQuery, BooleanClause.Occur.SHOULD)
                        .build();

                // Search and Pagination
                int start = (page - 1) * pageSize;
                int numHits = start + pageSize;
                if (numHits <= 0) {
                    throw new IllegalArgumentException("Invalid pagination parameters: numHits must be > 0");
                }
                TopDocs results = searcher.search(combinedQuery, numHits);
                int end = Math.min(start + pageSize, results.scoreDocs.length);
                long totalResults = results.totalHits.value;
                int totalPages = (int) Math.ceil((double) totalResults / pageSize);


                List<SearchResult> searchResults = new ArrayList<>();

                // Only add results that are within the current page
                for (int i = start; i < end; i++) {
                    ScoreDoc scoreDoc = results.scoreDocs[i];
                    Document doc = searcher.doc(scoreDoc.doc);
                    String idStr = doc.get(DocumentField.ID.getFieldName());
                    String content = doc.get(DocumentField.CONTENT.getFieldName());
                    String user = doc.get(DocumentField.USER.getFieldName());

                    UUID postId = idStr != null ? UUID.fromString(idStr) : null;

                    searchResults.add(new SearchResult(
                            postId,
                            content,
                            user,
                            totalResults,
                            pageNumber,
                            totalPages
                    ));
                }

                return searchResults;
            } catch (Exception e) {
                throw new IOException("Failed to parse search query", e);
            }
        }
    }

}