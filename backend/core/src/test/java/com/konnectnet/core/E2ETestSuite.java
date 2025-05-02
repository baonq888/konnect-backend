package com.konnectnet.core;

import com.konnectnet.core.e2e.AuthTest;
import com.konnectnet.core.e2e.OnlineUserStatusTest;
import com.konnectnet.core.e2e.PostTest;
import com.konnectnet.core.e2e.utils.DatabaseCleaner;
import com.konnectnet.core.e2e.utils.LuceneIndexCleaner;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        DatabaseCleaner.class,
        LuceneIndexCleaner.class,
        AuthTest.class,
        // OnlineUserStatusTest.class,
        PostTest.class
})
public class E2ETestSuite {
}
