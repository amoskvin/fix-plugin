/*
 * Copyright (C) 2023  Alec Moskvin <code@netreq.com>
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.netreq.fix.plugin.psi;

import com.intellij.psi.tree.IElementType;
import com.netreq.fix.plugin.FixLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class FixElementType extends IElementType {

    public FixElementType(@NotNull @NonNls String debugName) {
        super(debugName, FixLanguage.INSTANCE);
    }
}
