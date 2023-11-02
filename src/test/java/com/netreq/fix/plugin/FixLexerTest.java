/*
 * Copyright (C) 2023  Alec Moskvin <code@netreq.com>
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.netreq.fix.plugin;

import com.intellij.psi.tree.IElementType;
import org.intellij.lang.annotations.Language;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.netreq.fix.plugin.psi.FixTypes.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FixLexerTest {
    private FixLexer fixLexer;

    @Before
    public void setup() {
        fixLexer = new FixLexer();
    }

    @Test
    public void testEmpty() {
        assertParsesTo("", 0);
    }

    @Test
    public void testOne8() {
        assertParsesTo("8", 0, JUNK);
    }

    @Test
    public void test8Eq() {
        assertParsesTo("8=", 0, JUNK);
    }

    @Test
    public void test8EqFIX() {
        assertParsesTo("8=FIX", 0, JUNK);
    }

    @Test
    public void test8EqFIXVer() {
        assertParsesTo("8=FIX.4.4", 0, JUNK);
    }

    @Test
    public void testFieldSOH() {
        assertParsesTo("8=FIX.4.4\001", 0, fields(1));
    }

    @Test
    public void testFieldPipe() {
        assertParsesTo("8=FIX.4.4|", 0, fields(1));
    }

    @Test
    public void testFieldCtrl() {
        assertParsesTo("8=FIX.4.4^", 0, JUNK);
    }

    @Test
    public void testFieldCtrlA() {
        assertParsesTo("8=FIX.4.4^A", 0, fields(1));
    }

    @Test
    public void testFieldCtrlC() {
        assertParsesTo("8=FIX.4.4^C", 0, JUNK);
    }

    @Test
    public void testFIXTSep() {
        assertParsesTo("8=FIXT.1.1|", 0, fields(1));
    }

    @Test
    public void testIncompleteTag() {
        assertParsesTo("8=FIX.4.4|9", 0, fields(1, JUNK));
    }

    @Test
    public void testIncompleteTagEq() {
        assertParsesTo("8=FIX.4.4|9=", 0, TAG, EQ, VALUE, SEP, TAG, EQ, VALUE);
    }

    @Test
    public void testBadTagStart() {
        assertParsesTo("8=FIX.4.1|?9=153|", 0, fields(1, JUNK));
    }

    @Test
    public void testBadTagEnd() {
        assertParsesTo("8=FIX.4.1|9?=153|", 0, fields(1, JUNK));
    }

    @Test
    public void testMultipleSeparators() {
        assertParsesTo("8=FIX.4.4\00149=value|with^A\001", 0, fields(2));
    }

    @Test
    public void testTag10() {
        assertParsesTo("8=FIX.4.4|10=000|", 0, fields(2));
    }

    @Test
    public void testTag10NoSep() {
        assertParsesTo("8=FIX.4.4|10=000", 0, TAG, EQ, VALUE, SEP, TAG, EQ, VALUE);
    }

    @Test
    public void testTag10EndsMessage() {
        assertParsesTo("8=FIX.4.2|10=001|49=ABC|56=XYZ|", 0, fields(2, JUNK));
    }

    @Test
    public void testTag10Incomplete() {
        assertParsesTo("8=FIX.4.2|10=0,8=FIX.4.2^A10=000", 0, TAG, EQ, VALUE, SEP, TAG, EQ, VALUE, JUNK, TAG, EQ, VALUE, SEP, TAG, EQ, VALUE);
    }

    @Test
    public void testTag10Invalid() {
        assertParsesTo("8=FIX.4.2|10=?,8=FIX.4.2^A10=000", 0, TAG, EQ, VALUE, SEP, TAG, EQ, VALUE, JUNK, TAG, EQ, VALUE, SEP, TAG, EQ, VALUE);
    }

    @Test
    public void testVersionWithNewline() {
        assertParsesTo("8=FIX.4.4\n|9=12|", 0, JUNK);
    }

    @Test
    public void testLFInValueEndsMessage() {
        assertParsesTo("8=FIX.4.4|9=\n12|10=000", 0, TAG, EQ, VALUE, SEP, TAG, EQ, VALUE, JUNK);
    }

    @Test
    public void testCRLFInValueEndsMessage() {
        assertParsesTo("8=FIX.4.4|9=1\n2|10=000", 0, TAG, EQ, VALUE, SEP, TAG, EQ, VALUE, JUNK);
    }

    @Test
    public void testAllowLFBeforeTag() {
        assertParsesTo("8=FIX.4.4|\n9=12|", 0, TAG, EQ, VALUE, SEP, WHITE_SPACE, TAG, EQ, VALUE, SEP);
    }

    @Test
    public void testAllowCRLFBeforeTag() {
        assertParsesTo("8=FIX.4.4|\r\n9=12|", 0, TAG, EQ, VALUE, SEP, WHITE_SPACE, TAG, EQ, VALUE, SEP);
    }

    @Test
    public void testDisallowMultipleLFBeforeTag() {
        assertParsesTo("8=FIX.4.4|\n\n9=12|", 0, fields(1, JUNK));
    }

    @Test
    public void testDisallowMultipleCRLFBeforeTag() {
        assertParsesTo("8=FIX.4.4|\r\n\r\n9=12|", 0, fields(1, JUNK));
    }

    @Test
    public void testFullMessage() {
        assertParsesTo("8=FIX.4.2|9=65|35=A|49=SERVER|56=CLIENT|34=177|52=20090107-18:15:16|98=0|108=30|10=062|", 0, fields(10));
    }

    @Test
    public void testMultipleMessages() {
        assertParsesTo("8=FIX.4.2|10=000|8=FIX.4.2^A10=000^A", 0, fields(4));
    }

    @Test
    public void testMultipleMessagesNoSep() {
        assertParsesTo("8=FIX.4.2|10=000,8=FIX.4.2^A10=000", 0, TAG, EQ, VALUE, SEP, TAG, EQ, VALUE, JUNK, TAG, EQ, VALUE, SEP, TAG, EQ, VALUE);
    }

    @Test
    public void testFirstMessageWithError() {
        assertParsesTo("8=FIX.4.2|1o=000,8=FIX.4.2|10=000", 0, TAG, EQ, VALUE, SEP, JUNK, TAG, EQ, VALUE, SEP, TAG, EQ, VALUE);
    }

    @Test
    public void testTag10RunOn() {
        assertParsesTo("8=FIX.4.2|10=0018=FIX.4.2|10=000|", 0, fields(1, TAG, EQ, VALUE, JUNK));
    }

    @Test
    public void testFalseStart() {
        assertParsesTo("8=FIX.4.2=8=FIX.4.2|10=001", 0, JUNK, TAG, EQ, VALUE, SEP, TAG, EQ, VALUE);
    }

    private static IElementType[] fields(int fullFields, IElementType... e) {
        return Stream.concat(IntStream.range(0, fullFields)
                        .mapToObj(i -> List.of(TAG, EQ, VALUE, SEP))
                        .flatMap(List::stream),
                Arrays.stream(e)).toArray(IElementType[]::new);
    }

    private void assertParsesTo(@Language("FIX") String buf, int state, IElementType... tokens) {
        int prevPos = 0;
        fixLexer.start(buf, prevPos, buf.length(), state);
        assertEquals(buf, fixLexer.getBufferSequence());
        assertEquals(buf.length(), fixLexer.getBufferEnd());

        for (IElementType token : tokens) {
            IElementType tokenType = fixLexer.getTokenType();
            assertEquals(getContext(), token, tokenType);
            assertEquals(getContext(), prevPos, fixLexer.getTokenStart());
            prevPos = fixLexer.getTokenEnd();
            fixLexer.advance();
        }
        assertNull(getContext(), fixLexer.getTokenType());
    }

    private String getContext() {
        return String.format("At %d:%d [%s] %x",
                fixLexer.getTokenStart(),
                fixLexer.getTokenEnd(),
                fixLexer.getBufferSequence().subSequence(fixLexer.getTokenStart(), fixLexer.getTokenEnd()),
                fixLexer.getState());
    }
}
