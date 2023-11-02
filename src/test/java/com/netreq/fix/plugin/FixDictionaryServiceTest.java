/*
 * Copyright (C) 2023  Alec Moskvin <code@netreq.com>
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.netreq.fix.plugin;

import com.netreq.fix.plugin.psi.FixField;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FixDictionaryServiceTest {
    private final FixDictionaryService service = new FixDictionaryService();

    @Mock
    private FixField fixField;

    @Test
    public void testFieldInfo_NoEnum() {
        when(fixField.getSpecVersionId()).thenReturn(5);
        when(fixField.getTag()).thenReturn(49);

        FixDictionaryService.FieldInfo info = service.getFieldInfo(fixField);

        assertNotNull(info);
        assertEquals("SenderCompID", info.fieldName());
        assertNull(info.valueName());
    }

    @Test
    public void testFieldInfo_WithEnum() {
        when(fixField.getSpecVersionId()).thenReturn(4);
        when(fixField.getTag()).thenReturn(39);
        when(fixField.getValue()).thenReturn("2");

        FixDictionaryService.FieldInfo info = service.getFieldInfo(fixField);

        assertNotNull(info);
        assertEquals("OrdStatus", info.fieldName());
        assertEquals("Filled", info.valueName());
    }

    @Test
    public void testFieldInfo_Unavailable() {
        when(fixField.getSpecVersionId()).thenReturn(4);
        when(fixField.getTag()).thenReturn(20);

        FixDictionaryService.FieldInfo info = service.getFieldInfo(fixField);

        assertNull(info);
    }

    @Test
    public void testFieldDetails_NoEnum() {
        when(fixField.getSpecVersionId()).thenReturn(4);
        when(fixField.getTag()).thenReturn(49);

        FixDictionaryService.FieldDetails details = service.getFieldDetails(fixField);

        assertNotNull(details);
        assertEquals("SenderCompID", details.fieldName());
        assertEquals("FIX.4.4", details.spec());
        assertEquals("String", details.type());
        assertEquals("Assigned value used to identify firm sending message.", details.fieldDescription());
        assertNull(details.valueName());
        assertNull(details.valueDescription());
    }

    @Test
    public void testFieldDetails_Unavailable() {
        when(fixField.getSpecVersionId()).thenReturn(4);
        when(fixField.getTag()).thenReturn(24);

        FixDictionaryService.FieldDetails details = service.getFieldDetails(fixField);

        assertNull(details);
    }

    @Test
    public void testFieldDetails_NoDescription() {
        when(fixField.getSpecVersionId()).thenReturn(3);
        when(fixField.getTag()).thenReturn(24);

        FixDictionaryService.FieldDetails details = service.getFieldDetails(fixField);

        assertNotNull(details);
        assertEquals("IOIOthSvc", details.fieldName());
        assertEquals("FIX.4.3", details.spec());
        assertEquals("char", details.type());
        assertNull(details.fieldDescription());
        assertNull(details.valueName());
        assertNull(details.valueDescription());
    }

    @Test
    public void testFieldDetails_WithEnum() {
        when(fixField.getSpecVersionId()).thenReturn(2);
        when(fixField.getTag()).thenReturn(39);
        when(fixField.getValue()).thenReturn("2");

        FixDictionaryService.FieldDetails details = service.getFieldDetails(fixField);

        assertNotNull(details);
        assertEquals("OrdStatus", details.fieldName());
        assertEquals("FIX.4.2", details.spec());
        assertEquals("char", details.type());
        assertEquals("Identifies current status of order.", details.fieldDescription());
        assertEquals("Filled", details.valueName());
        assertNull(details.valueDescription());
    }

    @Test
    public void testFieldDetails_WithEnumWithDescription() {
        when(fixField.getSpecVersionId()).thenReturn(5);
        when(fixField.getTag()).thenReturn(269);
        when(fixField.getValue()).thenReturn("E");

        FixDictionaryService.FieldDetails details = service.getFieldDetails(fixField);

        assertNotNull(details);
        assertEquals("MDEntryType", details.fieldName());
        assertEquals("FIX.5.0SP2", details.spec());
        assertEquals("char", details.type());
        assertEquals("Type Market Data entry.", details.fieldDescription());
        assertEquals("SimulatedSellPrice", details.valueName());
        assertEquals("Simulated Sell Price", details.valueDescription());
    }
}
