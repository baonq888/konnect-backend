package com.konnectnet.core.search.index;

import com.konnectnet.core.search.document.DocumentInfo;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public interface IndexService {
    Directory getDirectory() throws IOException;
    IndexWriter getIndexWriter() throws IOException;
    void indexDocument(DocumentInfo documentInfo) throws IOException;
    void deleteDocument(String id) throws IOException;
}
