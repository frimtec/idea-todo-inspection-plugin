package com.github.frimtec.idea.plugin.todoinspection;

import com.github.frimtec.ideatodoinspectionplugin.library.jira.JiraService;
import com.github.frimtec.ideatodoinspectionplugin.library.model.Ticket;
import com.github.frimtec.ideatodoinspectionplugin.library.model.Todo;
import com.github.frimtec.ideatodoinspectionplugin.library.model.Todo.TodoStatus;
import com.github.frimtec.ideatodoinspectionplugin.library.scanner.TodoScanner;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.frimtec.idea.plugin.todoinspection.OptionDialogHelper.*;

public class TodoInspection extends LocalInspectionTool {

    private final static Logger LOGGER = Logger.getInstance(MethodHandles.lookup().lookupClass());

    private static final Set<TodoStatus> STATES_WITH_EXISTING_TICKET = Set.of(
            TodoStatus.INCONSISTENT_TICKET_DONE,
            TodoStatus.CONSISTENT
    );

    private record ScannerEntry(InspectionOptions options, TodoScanner scanner) {
    }

    private final AtomicReference<ScannerEntry> scannerEntry = new AtomicReference<>();

    @SuppressWarnings({"WeakerAccess", "PublicField"})
    @NonNls
    public boolean allowFixme = false;

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
            secretOption("Jira API-Token", () -> new Encoder(this.jiraApiToken).plain(), (value) -> {
                this.jiraApiToken = Encoder.fromPlain(value).encodedValue();
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
        this.jiraApiToken = this.inspectionOptions.jiraApiToken().encodedValue();
        this.jiraProjectKeys = this.inspectionOptions.jiraProjectKeys();
        this.jiraClosedStates = this.inspectionOptions.jiraClosedStates();
        super.writeSettings(node);
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {

            @Override
            public void visitComment(@NotNull PsiComment comment) {
                var scannerEntry = TodoInspection.this.scannerEntry.get();
                if (scannerEntry == null || !TodoInspection.this.inspectionOptions.equals(scannerEntry.options)) {
                    CertificateManager certificateManager = CertificateManager.getInstance();
                    LOGGER.info("Initialize new TodoScanner with options: " + inspectionOptions);
                    scannerEntry = new ScannerEntry(
                            inspectionOptions,
                            new TodoScanner(
                                    new JiraService(
                                            inspectionOptions.jiraUrl(),
                                            inspectionOptions.jiraUsername(),
                                            inspectionOptions.jiraApiToken().plain(),
                                            new JiraService.TlsTrust(
                                                    certificateManager.getSslContext(),
                                                    certificateManager.getTrustManager()
                                            )
                                    ),
                                    split(TodoInspection.this.jiraProjectKeys),
                                    Ticket.statusMapper(split(TodoInspection.this.jiraClosedStates)))
                    );
                    TodoInspection.this.scannerEntry.set(scannerEntry);
                }
                scannerEntry.scanner().parseTodo(comment.getText()).forEach(todo -> {
                    List<LocalQuickFix> quickFixes = new ArrayList<>();
                    todo.ticket().ifPresent(
                            ticket ->
                                    quickFixes.add(STATES_WITH_EXISTING_TICKET.contains(todo.status()) ?
                                            new OpenTicketInBrowserQuickFix(TodoInspection.this.inspectionOptions.jiraUrl(), ticket.key()) : null)
                    );
                    quickFixes.add(new DeleteTodoQuickFix(comment.getContainingFile().getFileDocument(), comment.getTextOffset(), todo.textRange(), todo.type()));
                    if (todo.type() == Todo.Type.FIXME && !TodoInspection.this.allowFixme) {
                        quickFixes.add(new FixMeToTodoQuickFix(comment.getContainingFile().getFileDocument(), comment.getTextOffset(), todo.textRange()));
                        holder.registerProblem(
                                comment,
                                convertToTextRange(todo.textRange()),
                                "FIXME not allowed",
                                quickFixes.toArray(LocalQuickFix[]::new)
                        );
                    }
                    if (todo.status() != TodoStatus.CONSISTENT) {
                        holder.registerProblem(
                                comment,
                                convertToTextRange(todo.textRange()),
                                formatMessage(todo),
                                quickFixes.toArray(LocalQuickFix[]::new)
                        );
                    }
                });
            }
        };
    }

    private @NotNull @InspectionMessage String formatMessage(Todo todo) {
        return switch (todo.status()) {
            case INCONSISTENT_TICKET_DONE -> "%s references a ticket which is already done".formatted(todo.type());
            case INCONSISTENT_TICKET_NOT_EXISTING ->
                    "%s references a ticket that does not exist".formatted(todo.type());
            case NO_TICKET_REFERENCE -> "%s does not reference a ticket".formatted(todo.type());
            case UNKNOWN_TICKET_STATUS ->
                    "%s references a ticket for which the ticket status is currently unknown".formatted(todo.type());
            case JIRA_CONFIGURATION_ERROR -> "Missing or invalid Jira configuration";
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
                new Encoder(this.jiraApiToken),
                this.jiraProjectKeys,
                this.jiraClosedStates
        );
    }

    private @NotNull Set<String> split(@NonNls String jiraProjectKeys) {
        return Set.copyOf(Arrays.asList(jiraProjectKeys.split(",")));
    }

}
