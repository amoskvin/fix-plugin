// This is a generated file. Not intended for manual editing.
package com.netreq.fix.plugin.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.netreq.fix.plugin.psi.impl.*;

public interface FixTypes {

  IElementType FIELD = new FixElementType("FIELD");
  IElementType MESSAGE = new FixElementType("MESSAGE");

  IElementType EQ = new FixTokenType("EQ");
  IElementType JUNK = new FixTokenType("JUNK");
  IElementType SEP = new FixTokenType("SEP");
  IElementType TAG = new FixTokenType("TAG");
  IElementType VALUE = new FixTokenType("VALUE");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == FIELD) {
        return new FixFieldImpl(node);
      }
      else if (type == MESSAGE) {
        return new FixMessageImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
