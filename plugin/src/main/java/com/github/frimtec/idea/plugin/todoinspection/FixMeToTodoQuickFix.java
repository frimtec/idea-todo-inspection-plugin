package com.github.frimtec.idea.plugin.todoinspection;

import com.github.frimtec.ideatodoinspectionplugin.library.model.Todo.TextRange;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

class FixMeToTodoQuickFix implements LocalQuickFix {

    private final Document fileDocument;
    private final int commentTextOffset;
    private final TextRange todoTextRange;

    public FixMeToTodoQuickFix(@NotNull Document fileDocument, int commentTextOffset, @NotNull TextRange todoTextRange) {
        this.fileDocument = fileDocument;
        this.commentTextOffset = commentTextOffset;
        this.todoTextRange = todoTextRange;
    }

    @NotNull
    @Override
    public String getName() {
        return "Convert FIXME to TODO";
    }

    @NotNull
    @Override
    public @IntentionFamilyName String getFamilyName() {
        return "Convert";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        int startOffset = commentTextOffset + this.todoTextRange.startOffset();
        int endOffset = commentTextOffset + this.todoTextRange.endOffset();
        String originalText = fileDocument.getText();
        String prefix = originalText.substring(0, startOffset);
        String suffix = originalText.substring(endOffset);
        String textToModify = originalText.substring(startOffset, endOffset);
        String modifiedText = textToModify.replace("FIXME", "TODO");
        fileDocument.setText(prefix + modifiedText + suffix);
    }
}
