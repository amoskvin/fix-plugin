/*
 * Copyright (C) 2023  Alec Moskvin <code@netreq.com>
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.netreq.fix.plugin;

import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase;
import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerFactoryBase;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.netreq.fix.plugin.psi.FixField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FixHighlightUsagesHandlerFactory extends HighlightUsagesHandlerFactoryBase {
    @Override
    public @Nullable HighlightUsagesHandlerBase<FixField> createHighlightUsagesHandler(@NotNull Editor editor, @NotNull PsiFile file, @NotNull PsiElement target) {
        FixField field = PsiTreeUtil.getParentOfType(target, FixField.class);
        if (field != null) {
            return new FixHighlightUsagesHandler(editor, file, field);
        }
        return null;
    }
}
