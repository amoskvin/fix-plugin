/*
 * Copyright (C) 2023  Alec Moskvin <code@netreq.com>
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.netreq.fix.plugin;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

public class FixColorSettingsPage implements ColorSettingsPage {

    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
            new AttributesDescriptor("Tag", FixSyntaxHighlighter.TAG),
            new AttributesDescriptor("Equals", FixSyntaxHighlighter.EQ),
            new AttributesDescriptor("Value", FixSyntaxHighlighter.VALUE),
            new AttributesDescriptor("Separator", FixSyntaxHighlighter.SEP),
            new AttributesDescriptor("Other text", FixSyntaxHighlighter.JUNK),
    };

    @Nullable
    @Override
    public Icon getIcon() {
        return FixIcons.FILE;
    }

    @NotNull
    @Override
    public SyntaxHighlighter getHighlighter() {
        return new FixSyntaxHighlighter();
    }

    @NotNull
    @Override
    public String getDemoText() {
        return """
                Example messages:
                8=FIX.4.1\u00019=61\u000135=A\u000134=1\u000149=EXEC\u000152=20121105-23:24:06\u000156=BANZAI\u000198=0\u0001108=30\u000110=003\u0001
                8=FIX.4.1|9=61|35=A|34=1|49=BANZAI|52=20121105-23:24:06|56=EXEC|98=0|108=30|10=003|
                8=FIX.4.1^A9=49^A35=0^A34=2^A49=BANZAI^A52=20121105-23:24:37^A56=EXEC^A10=228^A
                8=FIX.4.1\u00019=103\u000135=D\u000134=3\u000149=BANZAI\u000152=20121105-23:24:42\u000156=EXEC\u000111=1352157882577\u000121=1\u000138=10000\u000140=1\u000154=1\u000155=MSFT\u000159=0\u000110=062\u0001""";
    }

    @Nullable
    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return null;
    }

    @Override
    public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @Override
    public ColorDescriptor @NotNull [] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "FIX";
    }
}
