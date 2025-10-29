package com.github.frimtec.ideatodoinspectionplugin.library.scanner;


import com.github.frimtec.ideatodoinspectionplugin.library.jira.JiraService;
import com.github.frimtec.ideatodoinspectionplugin.library.model.Ticket;
import com.github.frimtec.ideatodoinspectionplugin.library.model.Todo;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TodoScanner {

    private record MatchResult(String match, String todoType, int startIndex, int endIndex) {
    }

    private final JiraService jiraService;
    private final Pattern todoPattern;
    private final Pattern jiraPattern;
    private final Function<String, Ticket.Status> doneMapping;

    public TodoScanner(JiraService jiraService, Set<String> jiraProjectsKeys, Function<String, Ticket.Status> doneMapping) {
        this.jiraService = jiraService;
        this.todoPattern = Pattern.compile("(" + String.join("|", Arrays.stream(Todo.Type.values()).map(Todo.Type::name).toList()) + ").*");
        this.jiraPattern = Pattern.compile(".*((" + String.join("|", jiraProjectsKeys) + ")-[0-9]+).*");
        this.doneMapping = doneMapping;
    }

    public boolean testConnection(String testTicketKey) {
        return !Set.of(
                Ticket.Status.JIRA_CONFIGURATION_ERROR,
                Ticket.Status.UNKNOWN
        ).contains(this.jiraService.loadTicket(testTicketKey).status(s -> Ticket.Status.OPEN));
    }

    public List<Todo> parseTodo(String commentBlock) {
        List<Todo> todos = new ArrayList<>();

        List<MatchResult> matchResults = parseMultilineString(this.todoPattern, commentBlock);
        for (MatchResult result : matchResults) {
            Todo.Type type = Todo.Type.valueOf(result.todoType());
            Todo.TextRange textRange = new Todo.TextRange(result.startIndex(), result.endIndex());
            Matcher matcher = this.jiraPattern.matcher(result.match());
            if (matcher.find()) {
                todos.add(new Todo(type, textRange, this.jiraService.loadTicket(matcher.group(1)), this.doneMapping));
            } else {
                todos.add(new Todo(type, textRange, Todo.TodoStatus.NO_TICKET_REFERENCE));
            }
        }
        return todos;
    }

    private static List<MatchResult> parseMultilineString(Pattern pattern, String multilineString) {
        List<MatchResult> results = new ArrayList<>();
        Matcher matcher = pattern.matcher(multilineString);

        while (matcher.find()) {
            results.add(new MatchResult(matcher.group(), matcher.group(1), matcher.start(), matcher.end()));
        }
        return results;
    }
}
