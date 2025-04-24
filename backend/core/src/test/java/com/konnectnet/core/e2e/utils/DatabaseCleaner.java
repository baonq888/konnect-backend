package com.konnectnet.core.e2e.utils;

import com.konnectnet.core.config.DatasourceConfig;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@RequiredArgsConstructor
public class DatabaseCleaner {

    private final DatasourceConfig datasourceConfig;

    @BeforeAll
    public void resetDatabase() throws Exception {
        String url = datasourceConfig.getUrl();
        String username = datasourceConfig.getUsername();
        String password = datasourceConfig.getPassword();

        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement()) {

            stmt.execute("DO $$ DECLARE r RECORD BEGIN " +
                    "FOR r IN (SELECT tablename FROM pg_tables WHERE schemaname = 'public') LOOP " +
                    "EXECUTE 'TRUNCATE TABLE ' || quote_ident(r.tablename) || ' CASCADE'; " +
                    "END LOOP; END $$;");
        }
    }

    @Test
    public void init() {
    }
}