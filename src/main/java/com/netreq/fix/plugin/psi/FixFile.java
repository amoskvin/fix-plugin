/*
 * Copyright (C) 2023  Alec Moskvin <code@netreq.com>
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.netreq.fix.plugin.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.netreq.fix.plugin.FixFileType;
import com.netreq.fix.plugin.FixLanguage;
import org.jetbrains.annotations.NotNull;

public class FixFile extends PsiFileBase {

    public FixFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, FixLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return FixFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "FIX Log";
    }
}
