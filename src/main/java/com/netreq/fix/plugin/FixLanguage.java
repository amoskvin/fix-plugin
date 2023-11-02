/*
 * Copyright (C) 2023  Alec Moskvin <code@netreq.com>
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.netreq.fix.plugin;

import com.intellij.lang.Language;

public class FixLanguage extends Language {
    public static final FixLanguage INSTANCE = new FixLanguage();

    private FixLanguage() {
        super("FIX");
    }
}
