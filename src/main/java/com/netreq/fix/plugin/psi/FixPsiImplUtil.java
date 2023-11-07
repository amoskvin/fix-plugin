/*
 * Copyright (C) 2023  Alec Moskvin <code@netreq.com>
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.netreq.fix.plugin.psi;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Key;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.netreq.fix.plugin.FixDictionaryService;

public class FixPsiImplUtil {
    private static final Key<CachedValue<Integer>> VERSION_KEY = Key.create("spec version");

    public static int getTag(FixField element) {
        try {
            return Integer.parseInt(getTagStr(element));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String getTagStr(FixField element) {
        ASTNode keyNode = element.getNode().getFirstChildNode();
        return keyNode.getText();
    }

    public static String getValue(FixField element) {
        ASTNode valueNode = element.getNode().getLastChildNode();
        return valueNode.getText();
    }

    public static Integer getSpecVersionId(FixField element) {
        return element.getParent().getNode().getPsi(FixMessage.class).getSpecVersionId();
    }

    public static Integer getSpecVersionId(FixMessage element) {
        return CachedValuesManager.getCachedValue(element, VERSION_KEY, () -> {
                    FixField field = element.getNode().getFirstChildNode().getPsi(FixField.class);
                    FixDictionaryService service = ApplicationManager.getApplication().getService(FixDictionaryService.class);
                    return CachedValueProvider.Result.create(service.getSpecVersionId(field.getValue()), PsiModificationTracker.MODIFICATION_COUNT);
                }
        );
    }
}
