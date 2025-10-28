package com.github.frimtec.idea.plugin.todoinspection.quickfix;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

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
        return "Open ticket";
    }

    @NotNull
    @Override
    public @IntentionFamilyName String getFamilyName() {
        return "Open in browser";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        BrowserUtil.browse(this.jiraUrl + "/browse/" + this.ticketKey);
    }
}
