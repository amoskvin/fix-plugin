/*
 * Copyright (C) 2023  Alec Moskvin <code@netreq.com>
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.netreq.fix.plugin;

import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.Consumer;
import com.netreq.fix.plugin.psi.FixField;
import com.netreq.fix.plugin.psi.FixFile;
import com.netreq.fix.plugin.psi.FixMessage;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class FixHighlightUsagesHandler extends HighlightUsagesHandlerBase<FixField> {

    private final FixField selectedField;

    protected FixHighlightUsagesHandler(@NotNull Editor editor, @NotNull PsiFile file, final FixField target) {
        super(editor, file);
        selectedField = target;
    }

    @Override
    public @NotNull List<FixField> getTargets() {
        return Collections.singletonList(selectedField);
    }

    @Override
    protected void selectTargets(@NotNull List<? extends FixField> targets, @NotNull Consumer<? super List<? extends FixField>> selectionConsumer) {
        selectionConsumer.consume(targets);
    }

    @Override
    public void computeUsages(@NotNull List<? extends FixField> targets) {
        String selectedTag = selectedField.getTagStr();
        String selectedValue = selectedField.getValue();

        FixFile file = (FixFile) selectedField.getParent().getParent();

        for (PsiElement child : file.getChildren()) {
            if (child instanceof FixMessage message) {
                for (PsiElement grandChild : message.getChildren()) {
                    if (grandChild instanceof FixField field) {
                        if (selectedTag.equals(field.getTagStr())) {
                            if (selectedValue.equals(field.getValue())) {
                                myWriteUsages.add(field.getTextRange());
                            } else {
                                myReadUsages.add(field.getTextRange());
                            }
                        }
                    }
                }
            }
        }
    }
}
