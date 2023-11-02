/*
 * Copyright (C) 2023  Alec Moskvin <code@netreq.com>
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.netreq.fix.plugin;

import com.intellij.testFramework.ParsingTestCase;

import java.io.IOException;

public class FixParsingTest extends ParsingTestCase {

    public FixParsingTest() {
        super("", "fixlog", new FixParserDefinition());
    }

    public void testParsingTestData() {
        doTest(true);
    }

    @Override
    protected void doTest(String suffix) throws IOException {
        super.doTest(suffix);
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/resources";
    }

    @Override
    protected boolean includeRanges() {
        return true;
    }
}
