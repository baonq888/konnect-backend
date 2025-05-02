package com.konnectnet.core.e2e.utils;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@RequiredArgsConstructor
public class DatabaseCleaner {

    @BeforeAll
    public void resetDatabase() throws Exception {
        String url = "jdbc:postgresql://localhost:5432/konnect_db";
        String username = "postgres";
        String password = "password";

        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                DO $$
                DECLARE
                    r RECORD;
                BEGIN
                    FOR r IN (
                        SELECT tablename
                        FROM pg_tables
                        WHERE schemaname = 'public'
                          AND tablename != 'role'
                    ) LOOP
                        EXECUTE 'TRUNCATE TABLE ' || quote_ident(r.tablename) || ' CASCADE';
                    END LOOP;
                END $$;
            """);
        }
    }

    @Test
    public void init() {
    }
}