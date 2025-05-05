package com.konnectnet.core.e2e.utils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LuceneIndexCleaner {

    private static final String INDEX_DIR_PATH = "../lucene-index";

    @BeforeAll
    public void deleteIndexDirContents() throws IOException {
        Path rootDir = Paths.get(INDEX_DIR_PATH);
        if (Files.exists(rootDir)) {
            Files.walkFileTree(rootDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                        throws IOException {
                    if (!dir.equals(rootDir)) {
                        Files.delete(dir);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            // Create the directory if it doesn't exist
            Files.createDirectories(rootDir);
        }
    }

    @Test
    public void init() {
        // This test exists just to trigger the lifecycle
    }
}