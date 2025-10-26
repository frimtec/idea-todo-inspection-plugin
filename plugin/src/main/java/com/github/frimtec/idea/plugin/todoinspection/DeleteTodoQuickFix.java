package com.github.frimtec.idea.plugin.todoinspection;

import com.github.frimtec.ideatodoinspectionplugin.library.model.Todo;
import com.github.frimtec.ideatodoinspectionplugin.library.model.Todo.TextRange;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

class DeleteTodoQuickFix implements LocalQuickFix {

    private final Document fileDocument;
    private final int commentTextOffset;
    private final TextRange todoTextRange;
    private final Todo.Type type;

    public DeleteTodoQuickFix(@NotNull Document fileDocument, int commentTextOffset, @NotNull TextRange todoTextRange, Todo.Type type) {
        this.fileDocument = fileDocument;
        this.commentTextOffset = commentTextOffset;
        this.todoTextRange = todoTextRange;
        this.type = type;
    }

    @NotNull
    @Override
    public String getName() {
        return "Delete " + this.type;
    }

    @NotNull
    @Override
    public @IntentionFamilyName String getFamilyName() {
        return "Delete";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        int startOffset = commentTextOffset + this.todoTextRange.startOffset();
        int endOffset = commentTextOffset + this.todoTextRange.endOffset();
        String originalText = fileDocument.getText();
        String prefix = originalText.substring(0, startOffset);
        String suffix = originalText.substring(endOffset);
        fileDocument.setText(prefix + suffix);
    }
}
