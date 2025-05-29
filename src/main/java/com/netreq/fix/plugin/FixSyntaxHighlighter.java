/*
 * Copyright (C) 2023  Alec Moskvin <code@netreq.com>
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.netreq.fix.plugin;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import com.netreq.fix.plugin.psi.FixTypes;
import org.jetbrains.annotations.NotNull;

public class FixSyntaxHighlighter extends SyntaxHighlighterBase {
    public static final TextAttributesKey TAG = TextAttributesKey.createTextAttributesKey("FIX_TAG", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey EQ = TextAttributesKey.createTextAttributesKey("FIX_EQ", TAG);
    public static final TextAttributesKey VALUE = TextAttributesKey.createTextAttributesKey("FIX_VALUE", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey SEP = TextAttributesKey.createTextAttributesKey("FIX_SEP", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey JUNK = TextAttributesKey.createTextAttributesKey("FIX_JUNK", HighlighterColors.NO_HIGHLIGHTING);

    private static final TextAttributesKey[] TAG_KEYS = new TextAttributesKey[]{TAG};
    private static final TextAttributesKey[] EQ_KEYS = new TextAttributesKey[]{EQ};
    private static final TextAttributesKey[] VALUE_KEYS = new TextAttributesKey[]{VALUE};
    private static final TextAttributesKey[] SEP_KEYS = new TextAttributesKey[]{SEP};
    private static final TextAttributesKey[] JUNK_KEYS = new TextAttributesKey[]{JUNK};
    private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new FixLexer();
    }

    @NotNull
    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        if (tokenType.equals(FixTypes.TAG)) {
            return TAG_KEYS;
        }
        if (tokenType.equals(FixTypes.EQ)) {
            return EQ_KEYS;
        }
        if (tokenType.equals(FixTypes.VALUE)) {
            return VALUE_KEYS;
        }
        if (tokenType.equals(FixTypes.SEP)) {
            return SEP_KEYS;
        }
        if (tokenType.equals(FixTypes.JUNK)) {
            return JUNK_KEYS;
        }
        return EMPTY_KEYS;
    }
}
