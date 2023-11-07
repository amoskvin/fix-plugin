/*
 * Copyright (C) 2023  Alec Moskvin <code@netreq.com>
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.netreq.fix.plugin;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class FixFileType extends LanguageFileType {

    public static final FixFileType INSTANCE = new FixFileType();

    private FixFileType() {
        super(FixLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "FIX Log";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "FIX";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "fixlog";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return FixIcons.FILE;
    }
}
