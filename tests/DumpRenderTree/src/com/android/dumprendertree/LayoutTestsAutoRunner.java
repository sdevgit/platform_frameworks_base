/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.dumprendertree;

import junit.framework.TestSuite;
import com.android.dumprendertree.LayoutTestsAutoTest;

import android.test.InstrumentationTestRunner;
import android.test.InstrumentationTestSuite;
import android.util.Log;
import android.content.Intent;
import android.os.Bundle;


/**
 * Instrumentation Test Runner for all MediaPlayer tests.
 * 
 * Running all tests:
 *
 * adb shell am instrument \
 *   -w com.android.dumprendertree.LayoutTestsAutoRunner
 */

public class LayoutTestsAutoRunner extends InstrumentationTestRunner {
    @Override
    public TestSuite getAllTests() {
        TestSuite suite = new InstrumentationTestSuite(this);
        suite.addTestSuite(LayoutTestsAutoTest.class);
        return suite;
    }

    @Override
    public ClassLoader getLoader() {
        return LayoutTestsAutoRunner.class.getClassLoader();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        String path = (String) icicle.get("path");
        LayoutTestsAutoTest.setLayoutTestDir(path);
        String timeout_str = (String) icicle.get("timeout");
        int timeout = 0;  // default value
        if (timeout_str != null) {
            try {
                timeout = Integer.parseInt(timeout_str);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        LayoutTestsAutoTest.setTimeoutInMillis(timeout);
    }
}

