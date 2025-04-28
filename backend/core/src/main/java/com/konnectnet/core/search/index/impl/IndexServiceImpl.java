package com.konnectnet.core.search.index.impl;

import com.konnectnet.core.search.document.DocumentInfo;
import com.konnectnet.core.search.index.IndexService;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.springframework.stereotype.Service;
import com.konnectnet.core.search.document.DocumentField;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import java.io.File;
import java.io.IOException;

@Service
public class IndexServiceImpl implements IndexService{

    @Value("${search.lucene.dir}")
    private String luceneDir;

    public Directory getDirectory() throws IOException {
        File indexDir = new File(luceneDir);
        if (!indexDir.exists()) {
            boolean created = indexDir.mkdirs();
            if (!created) {
                System.err.println("Warning: Directory creation failed for " + indexDir.getAbsolutePath());
            }
        }
        return FSDirectory.open(indexDir.toPath());
    }

    public IndexWriter getIndexWriter() throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        return new IndexWriter(getDirectory(), config);
    }

    public void indexDocument(DocumentInfo documentInfo) throws IOException {
        String id = documentInfo.getId();
        String content = documentInfo.getContent();
        String user = documentInfo.getUser();

        try (IndexWriter writer = getIndexWriter()) {
            Document document = new Document();
            document.add(new StringField(DocumentField.ID.getFieldName(), id, Field.Store.YES));
            document.add(new TextField(DocumentField.CONTENT.getFieldName(), content, Field.Store.YES));
            document.add(new TextField(DocumentField.USER.getFieldName(), user, Field.Store.YES));
            writer.updateDocument(new Term(DocumentField.ID.getFieldName(), id), document);
        }
    }

    public void deleteDocument(String id) throws IOException {
        try (IndexWriter writer = getIndexWriter()) {
            writer.deleteDocuments(new Term(DocumentField.ID.getFieldName(), id));
        }
    }
}
