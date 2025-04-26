package com.konnectnet.core;

import com.konnectnet.core.e2e.AuthTest;
import com.konnectnet.core.e2e.OnlineUserStatusTest;
import com.konnectnet.core.e2e.utils.DatabaseCleaner;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        DatabaseCleaner.class,
        AuthTest.class,
        OnlineUserStatusTest.class
})
public class E2ETestSuite {
}
