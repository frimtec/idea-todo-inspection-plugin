package com.github.frimtec.idea.plugin.todoinspection;

import com.github.frimtec.idea.plugin.todoinspection.OptionDialogHelper.Option;
import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElementVisitor;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.lang.invoke.MethodHandles;
import java.util.List;

public class TodoInspection extends LocalInspectionTool {

    private final static Logger LOGGER = Logger.getInstance(MethodHandles.lookup().lookupClass());

    @SuppressWarnings({"WeakerAccess", "PublicField"})
    @NonNls
    public String jiraUrl = "";

    private InspectionOptions inspectionOptions = InspectionOptions.of(this.jiraUrl);

    private final List<Option> options = List.of(
            Option.create("Jira URL", () -> this.jiraUrl, (value) -> {
                this.jiraUrl = value;
                this.inspectionOptions = InspectionOptions.of(this.jiraUrl);
            })
    );

    @Override
    public void readSettings(@NotNull Element node) {
        super.readSettings(node);
        this.inspectionOptions = InspectionOptions.of(this.jiraUrl);
    }

    @Override
    public void writeSettings(@NotNull Element node) {
        this.jiraUrl = this.inspectionOptions.getJiraUrl();
        super.writeSettings(node);
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {

            @Override
            public void visitComment(@NotNull PsiComment comment) {
                LOGGER.warn("Visiting comment: " + comment.getText());
            }
        };
    }

    @Override
    public JComponent createOptionsPanel() {
        return OptionDialogHelper.createOptionsPanel(this.options);
    }

    @Override
    public @NotNull HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }
}
