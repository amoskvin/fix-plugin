// This is a generated file. Not intended for manual editing.
package com.netreq.fix.plugin.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;

public class FixVisitor extends PsiElementVisitor {

  public void visitField(@NotNull FixField o) {
    visitPsiElement(o);
  }

  public void visitMessage(@NotNull FixMessage o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
