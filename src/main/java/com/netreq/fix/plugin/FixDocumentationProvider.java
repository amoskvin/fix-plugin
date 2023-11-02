/*
 * Copyright (C) 2023  Alec Moskvin <code@netreq.com>
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.netreq.fix.plugin;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.netreq.fix.plugin.psi.FixField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FixDocumentationProvider extends AbstractDocumentationProvider {
    @Override
    public @Nullable String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        if (element instanceof FixField field) {
            return renderFullDoc(field);
        }
        return null;
    }

    private String renderFullDoc(FixField field) {
        int tag = field.getTag();
        FixDictionaryService service = ApplicationManager.getApplication().getService(FixDictionaryService.class);
        FixDictionaryService.FieldDetails details = service.getFieldDetails(field);
        if (details == null) {
            return null;
        }
        String desc = details.fieldDescription();
        if (desc != null) {
            desc = desc.replace("\n", "<p>");
        }
        String valueDesc = null;
        if (details.valueDescription() != null) {
            valueDesc = details.valueDescription().replace("\n", "<p>");
        }

        StringBuilder sb = new StringBuilder();
        sb.append(DocumentationMarkup.DEFINITION_START);
        sb.append("Tag ").append(tag).append(" (").append(details.fieldName()).append(')');
        String valueName = details.valueName();
        if (valueName != null) {
            sb.append(" = ").append(field.getValue()).append(" (").append(valueName).append(')');
        }
        sb.append(DocumentationMarkup.DEFINITION_END);
        sb.append(DocumentationMarkup.CONTENT_START);
        sb.append(details.fieldName());
        sb.append(" – ");
        sb.append(desc);
        sb.append(DocumentationMarkup.CONTENT_END);
        sb.append(DocumentationMarkup.SECTIONS_START);
        if (valueName != null) {
            if (valueDesc == null) {
                addKeyValueSection(sb, "Value:", valueName);
            } else {
                addKeyValueSection(sb, "Value:", valueName, " – ", valueDesc);
            }
        }
        addKeyValueSection(sb, "Type:", details.type());
        addKeyValueSection(sb, "Spec:", details.spec());
        sb.append(DocumentationMarkup.SECTIONS_END);
        return sb.toString();
    }

    private void addKeyValueSection(StringBuilder sb, String key, String... values) {
        sb.append(DocumentationMarkup.SECTION_HEADER_START);
        sb.append(key);
        sb.append(DocumentationMarkup.SECTION_SEPARATOR);
        sb.append("<p>");
        for (String value : values) {
            sb.append(value);
        }
        sb.append(DocumentationMarkup.SECTION_END);
    }

    @Override
    public @Nullable PsiElement getCustomDocumentationElement(@NotNull Editor editor, @NotNull PsiFile file, @Nullable PsiElement contextElement, int targetOffset) {
        if (contextElement != null && contextElement.getParent() instanceof FixField) {
            return contextElement.getParent();
        }
        return null;
    }
}
