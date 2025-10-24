package com.github.frimtec.idea.plugin.todoinspection;

import com.github.frimtec.ideatodoinspectionplugin.library.jira.JiraService;
import com.github.frimtec.ideatodoinspectionplugin.library.model.Ticket;
import com.github.frimtec.ideatodoinspectionplugin.library.model.Todo;
import com.github.frimtec.ideatodoinspectionplugin.library.scanner.TodoScanner;
import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElementVisitor;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.github.frimtec.idea.plugin.todoinspection.OptionDialogHelper.*;

public class TodoInspection extends LocalInspectionTool {

    private final static Logger LOGGER = Logger.getInstance(MethodHandles.lookup().lookupClass());

    @SuppressWarnings({"WeakerAccess", "PublicField"})
    @NonNls
    public String allowFixme = "false";

    @SuppressWarnings({"WeakerAccess", "PublicField"})
    @NonNls
    public String jiraUrl = "";

    @SuppressWarnings({"WeakerAccess", "PublicField"})
    @NonNls
    public String jiraUsername = "";
    @SuppressWarnings({"WeakerAccess", "PublicField"})
    @NonNls
    public String jiraApiToken = "";
    @SuppressWarnings({"WeakerAccess", "PublicField"})
    @NonNls
    public String truststoreFilePath = "";
    @SuppressWarnings({"WeakerAccess", "PublicField"})
    @NonNls
    public String jiraProjectKeys = "";
    @SuppressWarnings({"WeakerAccess", "PublicField"})
    @NonNls
    public String jiraClosedStates = "Closed,Done,Resolved";


    private InspectionOptions inspectionOptions = buildInspectionOptions();

    private final List<OptionDialogHelper.Option> options = List.of(
            booleanOption("Allow FIXME", () -> this.allowFixme, (value) -> {
                this.allowFixme = value;
                this.inspectionOptions = buildInspectionOptions();
            }),
            textOption("Jira URL", () -> this.jiraUrl, (value) -> {
                this.jiraUrl = value;
                this.inspectionOptions = buildInspectionOptions();
            }),
            textOption("Jira Username", () -> this.jiraUsername, (value) -> {
                this.jiraUsername = value;
                this.inspectionOptions = buildInspectionOptions();
            }),
            secretOption("Jira API-Token", () -> this.jiraApiToken, (value) -> {
                this.jiraApiToken = value;
                this.inspectionOptions = buildInspectionOptions();
            }),
            textOption("Truststore file path", () -> this.truststoreFilePath, (value) -> {
                this.truststoreFilePath = value;
                this.inspectionOptions = buildInspectionOptions();
            }),
            textOption("Jira Project Keys", () -> this.jiraProjectKeys, (value) -> {
                this.jiraProjectKeys = value;
                this.inspectionOptions = buildInspectionOptions();
            }),
            textOption("Jira Closed States", () -> this.jiraClosedStates, (value) -> {
                this.jiraClosedStates = value;
                this.inspectionOptions = buildInspectionOptions();
            })
    );

    @Override
    public void readSettings(@NotNull Element node) {
        super.readSettings(node);
        this.inspectionOptions = buildInspectionOptions();
    }

    @Override
    public void writeSettings(@NotNull Element node) {
        this.allowFixme = this.inspectionOptions.allowFixme();
        this.jiraUrl = this.inspectionOptions.jiraUrl();
        this.jiraUsername = this.inspectionOptions.jiraUsername();
        this.jiraApiToken = this.inspectionOptions.jiraApiToken();
        this.truststoreFilePath = this.inspectionOptions.truststoreFilePath();
        this.jiraProjectKeys = this.inspectionOptions.jiraProjectKeys();
        this.jiraClosedStates = this.inspectionOptions.jiraClosedStates();
        super.writeSettings(node);
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        LOGGER.warn("Create new visitor: " + this.inspectionOptions);
        return new PsiElementVisitor() {
            final TodoScanner todoScanner = new TodoScanner(
                    new JiraService(
                            TodoInspection.this.jiraUrl,
                            TodoInspection.this.jiraUsername,
                            TodoInspection.this.jiraApiToken,
                            emptyString(TodoInspection.this.truststoreFilePath) ? null : Path.of(TodoInspection.this.truststoreFilePath)
                    ),
                    split(TodoInspection.this.jiraProjectKeys),
                    Ticket.statusMapper(split(TodoInspection.this.jiraClosedStates))
            );

            private boolean emptyString(@Nullable String string) {
                return string == null || string.isBlank();
            }

            @Override
            public void visitComment(@NotNull PsiComment comment) {
                LOGGER.warn("Visiting comment: " + comment.getText());
                todoScanner.parseTodo(comment.getText()).forEach(todo -> {
                    LOGGER.warn("Todo found: " + todo);
                    if (todo.status() != Todo.TodoStatus.CONSISTENT) {
                        holder.registerProblem(comment, convertToTextRange(todo.textRange()), String.format("TODO state: %s".formatted(todo.status())));
                    }
                    if(todo.type() == Todo.Type.FIXME && TodoInspection.this.allowFixme.equals("false")) {
                        holder.registerProblem(comment, convertToTextRange(todo.textRange()), "FIXME not allowed");
                    }
                });
            }
        };
    }

    private static TextRange convertToTextRange(Todo.TextRange textRange) {
        return new TextRange(textRange.startOffset(), textRange.endOffset());
    }

    @Override
    public JComponent createOptionsPanel() {
        return OptionDialogHelper.createOptionsPanel(this.options);
    }

    @Override
    public @NotNull HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }

    private @NotNull InspectionOptions buildInspectionOptions() {
        return InspectionOptions.of(
                this.allowFixme,
                this.jiraUrl,
                this.jiraUsername,
                this.jiraApiToken,
                this.truststoreFilePath,
                this.jiraProjectKeys,
                this.jiraClosedStates
        );
    }

    private @NotNull Set<String> split(@NonNls String jiraProjectKeys) {
        return Set.copyOf(Arrays.asList(jiraProjectKeys.split(",")));
    }

}
