package com.github.frimtec.idea.plugin.todoinspection.quickfix;

import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import static com.github.frimtec.idea.plugin.todoinspection.quickfix.QuickFixPriority.orderedLabel;

public class OpenTicketInBrowserQuickFix implements LocalQuickFix {

    private final String jiraUrl;
    private final String ticketKey;

    public OpenTicketInBrowserQuickFix(String jiraUrl, String ticketKey) {
        this.jiraUrl = jiraUrl;
        this.ticketKey = ticketKey;
    }

    @NotNull
    @Override
    public String getName() {
        return orderedLabel(QuickFixPriority.HIGH, "Open ticket");
    }

    @NotNull
    @Override
    public @IntentionFamilyName String getFamilyName() {
        return getName();
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        ApplicationManager.getApplication().invokeLater(
                () -> BrowserUtil.browse(this.jiraUrl + "/browse/" + this.ticketKey)
        );
    }

    @Override
    public @NotNull IntentionPreviewInfo generatePreview(@NotNull Project project, @NotNull ProblemDescriptor previewDescriptor) {
        return IntentionPreviewInfo.EMPTY;
    }
}
