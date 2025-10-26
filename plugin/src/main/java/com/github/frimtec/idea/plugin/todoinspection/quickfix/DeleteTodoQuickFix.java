package com.github.frimtec.idea.plugin.todoinspection.quickfix;

import com.github.frimtec.ideatodoinspectionplugin.library.model.Todo;
import com.github.frimtec.ideatodoinspectionplugin.library.model.Todo.TextRange;
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DeleteTodoQuickFix extends LocalQuickFixAndIntentionActionOnPsiElement {

    private final TextRange todoTextRange;
    private final Todo.Type type;

    public DeleteTodoQuickFix(@NotNull PsiElement element, @NotNull TextRange todoTextRange, Todo.Type type) {
        super(element);
        this.todoTextRange = todoTextRange;
        this.type = type;
    }

    @NotNull
    @Override
    public String getText() {
        return "Delete " + this.type;
    }

    @NotNull
    @Override
    public @IntentionFamilyName String getFamilyName() {
        return "Delete";
    }

    @Override
    public void invoke(@NotNull Project project, @NotNull PsiFile psiFile, @Nullable Editor editor, @NotNull PsiElement psiElement, @NotNull PsiElement psiElement1) {
        int commentTextOffset = psiElement.getTextOffset();
        int startOffset = commentTextOffset + this.todoTextRange.startOffset();
        int endOffset = commentTextOffset + this.todoTextRange.endOffset();
        Document fileDocument = psiFile.getFileDocument();
        String originalText = fileDocument.getText();
        String prefix = originalText.substring(0, startOffset);
        String suffix = originalText.substring(endOffset);
        fileDocument.setText(prefix + suffix);
    }
}