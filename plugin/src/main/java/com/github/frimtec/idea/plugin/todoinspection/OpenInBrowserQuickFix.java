package com.github.frimtec.idea.plugin.todoinspection;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

record OpenInBrowserQuickFix(String jiraUrl, String ticketKey) implements LocalQuickFix {

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
