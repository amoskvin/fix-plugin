/*
 * Copyright (C) 2023  Alec Moskvin <code@netreq.com>
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.netreq.fix.plugin;

import com.intellij.codeInsight.hints.HintInfo;
import com.intellij.codeInsight.hints.InlayInfo;
import com.intellij.codeInsight.hints.InlayParameterHintsProvider;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.netreq.fix.plugin.psi.impl.FixFieldImpl;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public class FixInlayParameterHintsProvider implements InlayParameterHintsProvider {
    @Override
    public @NotNull List<InlayInfo> getParameterHints(@NotNull PsiElement element, @NotNull PsiFile file) {
        if (element instanceof FixFieldImpl field) {
            FixDictionaryService service = ApplicationManager.getApplication().getService(FixDictionaryService.class);
            FixDictionaryService.FieldInfo data = service.getFieldInfo(field);
            if (data != null && data.fieldName() != null) {
                StringBuilder sb = new StringBuilder();
                boolean isMsgType = "35".equals(field.getTagStr());
                if (!isMsgType) {
                    // for MsgType just show the value
                    sb.append(data.fieldName());
                }

                if (data.valueName() != null) {
                    if (!isMsgType) {
                        sb.append(": ");
                    }
                    sb.append(data.valueName());
                }

                if (!sb.isEmpty()) {
                    InlayInfo info = new InlayInfo(sb.toString(), element.getTextOffset() + element.getTextLength(), false, true, true);
                    return Collections.singletonList(info);
                }
            }
        }
        return Collections.emptyList();
    }

    @Override
    public @NotNull String getInlayPresentation(@NotNull String inlayText) {
        return inlayText;
    }

    @Override
    public @Nullable HintInfo getHintInfo(@NotNull PsiElement element) {
        if (element instanceof FixFieldImpl field) {
            return new TagInfo("tag" , Collections.singletonList(field.getTagStr()));
        }
        return null;
    }

    @Override
    public @NotNull Set<String> getDefaultBlackList() {
        // skip BeginString, BodyLength and CheckSum by default
        return Set.of("tag(8)", "tag(9)", "tag(10)");
    }

    @Override
    public @Nls String getDescription() {
        return "Shows decoded tag names and values";
    }


    @Override
    public @Nls String getBlacklistExplanationHTML() {
        return "To disable hints for certain tags, list them one per line:" +
                "<p><code><b>tag(8)</b></code> - hides hints for Tag 8";
    }

    private static class TagInfo extends HintInfo.MethodInfo {
        public TagInfo(@NotNull String fullyQualifiedName, @NotNull List<String> paramNames) {
            super(fullyQualifiedName, paramNames);
        }

        @NotNull
        @Override
        public String getDisableHintText() {
            return "Disable hints for Tag " + getParamNames().get(0);
        }
    }
}
