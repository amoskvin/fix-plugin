/*
 * Copyright (C) 2023  Alec Moskvin <code@netreq.com>
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.netreq.fix.plugin;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.netreq.fix.plugin.psi.FixTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntPredicate;

/**
 * <li> A FIX message starts with "8=FIX". But there can be other things in the file, e.g. log messages, notes, etc.
 * <li> Messages consist of fields joined by a separator, which can be either an SOH character, a "|" or "^A".
 * <li> A field consists of a numeric tag, followed by '=', followed by value. Otherwise, assume end of message.
 * <li> The value is assumed not to have any newline. If a newline is found, it's assumed to be the end of the message.
 * <li> Messages can be broken up with one newline between the separator and the next tag. Anything else signals end of message.
 * <li> Tag 10 signals end of message after the 3 character value followed by an optional separator.
 */
public class FixLexer extends LexerBase {

    private static final int TOK_START = 0;
    private static final int TOK_TAG = 1;
    private static final int TOK_EQ = 2;
    private static final int TOK_VAL = 3;
    private static final int TOK_SEP = 4;
    private static final int TOK_LAST_EQ = 5;
    private static final int TOK_CHECKSUM = 6;
    private static final int TOK_LAST_SEP = 7;
    public static final int SEP_SOH = 0x10;
    public static final int SEP_PIPE = 0x20;
    public static final int SEP_CTRLA = 0x30;

    private final State state = new State();
    private CharSequence buffer;
    private int endOffset;
    private int tokenStart;
    private int tokenEnd;

    private IElementType tokenType;

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        this.buffer = buffer;
        this.endOffset = endOffset;
        this.tokenStart = tokenEnd = startOffset;
        this.state.setState(initialState);
        this.tokenType = null;
    }

    @Override
    public int getState() {
        locateToken();
        return state.getState();
    }

    @Override
    public @Nullable IElementType getTokenType() {
        locateToken();
        return tokenType;
    }

    @Override
    public int getTokenStart() {
        locateToken();
        return tokenStart;
    }

    @Override
    public int getTokenEnd() {
        locateToken();
        return tokenEnd;
    }

    @Override
    public void advance() {
        locateToken();
        tokenType = null;
    }

    @Override
    public @NotNull CharSequence getBufferSequence() {
        return buffer;
    }

    @Override
    public int getBufferEnd() {
        return endOffset;
    }

    private void locateToken() {
        if (tokenType != null) {
            // already located
            return;
        }

        tokenStart = tokenEnd;
        switch (state.getTok()) {
            case TOK_START:
                parseStart();
                break;
            case TOK_TAG:
                parseTag();
                break;
            case TOK_EQ:
                consumeEq();
                state.setTok(TOK_VAL);
                break;
            case TOK_VAL:
                parseValue();
                break;
            case TOK_SEP:
                consumeSep();
                state.setTok(TOK_TAG);
                break;
            case TOK_LAST_EQ:
                consumeEq();
                state.setTok(TOK_CHECKSUM);
                break;
            case TOK_CHECKSUM:
                parseChkSum();
                break;
            case TOK_LAST_SEP:
                parseLastSep();
                state.clearState();
                break;
        }
    }

    private void parseStart() {
        if (!parseJunk()) {
            parseTag();
        }
    }

    private boolean parseJunk() {
        if (endOffset == tokenStart) {
            return true;
        }

        state.clearState();
        tokenType = FixTypes.JUNK;
        while (tokenEnd < endOffset) {
            consumeWhile(ch -> ch != '8');
            if (tokenEnd == endOffset || findMessageStart()) {
                break;
            }
            tokenEnd++;
        }
        return tokenStart != tokenEnd;
    }

    private boolean findMessageStart() {
        String signature = "8=FIX";
        int sigLen = signature.length();
        int maxRange = Math.max(1, Math.min(8, endOffset - tokenEnd - sigLen));
        if (startsWith(tokenEnd, signature, maxRange)) {
            // make sure there's actually a separator, otherwise parser will complain
            for (int i = tokenEnd + sigLen; i < tokenEnd + sigLen + maxRange; i++) {
                if (findSep(i)) {
                    return true;
                } else if (isNewLine(buffer.charAt(i))) {
                    break;
                }
            }
            tokenEnd += sigLen;
        }
        return false;
    }

    private boolean parseSingleNewline() {
        if (startsWith(tokenStart, "\n", 1)) {
            tokenEnd++;
        } else if (startsWith(tokenStart, "\r\n", 1)) {
            tokenEnd += 2;
        } else {
            return false;
        }

        if (isNewLine(buffer.charAt(tokenEnd))) {
            return false;
        }

        tokenType = TokenType.WHITE_SPACE;
        return true;
    }

    private void parseTag() {
        if (parseSingleNewline()) {
            return;
        }

        consumeWhile(FixLexer::isDigit);
        if (startsWith(tokenEnd, "=", 0)) {
            if (startsWith(tokenStart, "10=", 1)) {
                state.setTok(TOK_LAST_EQ);
            } else {
                state.setTok(TOK_EQ);
            }
            tokenType = FixTypes.TAG;
        } else {
            parseJunk();
        }
    }

    private void parseValue() {
        String sep = getSep();
        while (tokenEnd < endOffset) {
            if (startsWith(tokenEnd, sep, 0)) {
                state.setTok(TOK_SEP);
                break;
            } else if (isNewLine(buffer.charAt(tokenEnd))) {
                state.clearState();
                break;
            }
            tokenEnd++;
        }

        if (tokenEnd == endOffset) {
            state.clearState();
        }

        tokenType = FixTypes.VALUE;
    }

    private void consumeEq() {
        tokenType = FixTypes.EQ;
        tokenEnd++;
    }

    private void consumeSep() {
        tokenEnd += getSep().length();
        tokenType = FixTypes.SEP;
    }

    private void parseLastSep() {
        if (startsWith(tokenEnd, getSep(), 0)) {
            consumeSep();
        } else if (tokenEnd != endOffset) {
            tokenEnd++;
            parseJunk();
        }
    }

    private void parseChkSum() {
        tokenType = FixTypes.VALUE;
        while (tokenEnd - tokenStart < 3 && tokenEnd < endOffset) {
            if (!isDigit(buffer.charAt(tokenEnd))) {
                state.clearState();
                return;
            }
            tokenEnd++;
        }

        state.setTok(TOK_LAST_SEP);
    }

    private void consumeWhile(IntPredicate p) {
        tokenEnd += (int) buffer.subSequence(tokenEnd, endOffset)
                .chars()
                .takeWhile(p)
                .count();
    }

    private boolean startsWith(int start, @NotNull CharSequence prefix, int extra) {
        int end = start + prefix.length();
        return buffer.length() >= end + extra &&
                0 == CharSequence.compare(prefix, buffer.subSequence(start, end));
    }

    private boolean findSep(int pos) {
        switch (buffer.charAt(pos)) {
            case '\001':
                state.setSep(SEP_SOH);
                return true;
            case '|':
                state.setSep(SEP_PIPE);
                return true;
            case '^':
                if (startsWith(pos, "^A", 0)) {
                    state.setSep(SEP_CTRLA);
                    return true;
                }
                break;
        }
        return false;
    }

    private String getSep() {
        return switch (state.getSep()) {
            case SEP_SOH -> "\001";
            case SEP_PIPE -> "|";
            case SEP_CTRLA -> "^A";
            default -> throw new IllegalStateException("Unknown separator");
        };
    }

    private static boolean isNewLine(char ch) {
        return ch == '\n' || ch == '\r';
    }

    private static boolean isDigit(int ch) {
        return ch >= '0' && ch <= '9';
    }

    /**
     * Since the separator needs to be consistent for each message, and the lexer
     * can start anywhere in the document, we need to have different states for messages
     * with each separator type.
     */
    private static class State {
        final int TOK_MASK = 0x000F;
        final int SEP_MASK = 0x00F0;

        private int state = 0;

        int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }

        public void clearState() {
            this.state = 0;
        }

        int getTok() {
            return state & TOK_MASK;
        }

        void setTok(int tok) {
            state = (state & ~TOK_MASK) | (tok & TOK_MASK);
        }

        int getSep() {
            return state & SEP_MASK;
        }

        void setSep(int sep) {
            state = (state & ~SEP_MASK) | (sep & SEP_MASK);
        }
    }
}
