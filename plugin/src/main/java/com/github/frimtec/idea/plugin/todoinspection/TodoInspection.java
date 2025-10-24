package com.github.frimtec.idea.plugin.todoinspection;

import com.github.frimtec.ideatodoinspectionplugin.library.jira.JiraService;
import com.github.frimtec.ideatodoinspectionplugin.library.model.Ticket;
import com.github.frimtec.ideatodoinspectionplugin.library.model.Todo;
import com.github.frimtec.ideatodoinspectionplugin.library.scanner.TodoScanner;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.util.InspectionMessage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.util.net.ssl.CertificateManager;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.lang.invoke.MethodHandles;
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
        this.jiraProjectKeys = this.inspectionOptions.jiraProjectKeys();
        this.jiraClosedStates = this.inspectionOptions.jiraClosedStates();
        super.writeSettings(node);
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        LOGGER.warn("Create new visitor: " + this.inspectionOptions);
        CertificateManager certificateManager = CertificateManager.getInstance();
        JiraService jiraService = new JiraService(
                TodoInspection.this.jiraUrl,
                TodoInspection.this.jiraUsername,
                TodoInspection.this.jiraApiToken,
                new JiraService.TlsTrust(
                        certificateManager.getSslContext(),
                        certificateManager.getTrustManager()
                )
        );
        return new PsiElementVisitor() {
            final TodoScanner todoScanner = new TodoScanner(
                    jiraService,
                    split(TodoInspection.this.jiraProjectKeys),
                    Ticket.statusMapper(split(TodoInspection.this.jiraClosedStates))
            );

            @Override
            public void visitComment(@NotNull PsiComment comment) {
                todoScanner.parseTodo(comment.getText()).forEach(todo -> {
                    if (todo.type() == Todo.Type.FIXME && TodoInspection.this.allowFixme.equals("false")) {
                        holder.registerProblem(comment, convertToTextRange(todo.textRange()), "FIXME not allowed");
                    }
                    if (todo.status() != Todo.TodoStatus.CONSISTENT) {
                        holder.registerProblem(
                                comment,
                                convertToTextRange(todo.textRange()),
                                formatWarnMessage(todo)
                        );
                    }
                });
            }
        };
    }

    private @NotNull @InspectionMessage String formatWarnMessage(Todo todo) {
        return switch (todo.status()) {
            case INCONSISTENT_TICKET_DONE -> "%s references a ticket which is already done".formatted(todo.type());
            case INCONSISTENT_TICKET_NOT_EXISTING ->
                    "%s references a ticket that does not exist".formatted(todo.type());
            case NO_TICKET_REFERENCE -> "%s does not reference a ticket".formatted(todo.type());
            case UNKNOWN_TICKET_STATUS ->
                    "%s references a ticket for which the ticket status currently unknown".formatted(todo.type());
            case CONSISTENT -> throw new IllegalStateException("Unexpected todo status: " + todo.status());
        };
    }

    private static TextRange convertToTextRange(Todo.TextRange textRange) {
        return new TextRange(textRange.startOffset(), textRange.endOffset());
    }

    @Override
    public JComponent createOptionsPanel() {
        return OptionDialogHelper.createOptionsPanel(this.options);
    }

    private @NotNull InspectionOptions buildInspectionOptions() {
        return InspectionOptions.of(
                this.allowFixme,
                this.jiraUrl,
                this.jiraUsername,
                this.jiraApiToken,
                this.jiraProjectKeys,
                this.jiraClosedStates
        );
    }

    private @NotNull Set<String> split(@NonNls String jiraProjectKeys) {
        return Set.copyOf(Arrays.asList(jiraProjectKeys.split(",")));
    }

}
