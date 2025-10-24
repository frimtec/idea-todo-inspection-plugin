package com.github.frimtec.ideatodoinspectionplugin.library.jira;


import com.github.frimtec.ideatodoinspectionplugin.library.model.Ticket;

import java.util.function.Function;

record JiraTicket(String key, Fields fields) implements Ticket {

    @Override
    public Status status(Function<String, Status> doneMapping) {
        return doneMapping.apply(this.jiraStatus().name());
    }

    public JiraStatus jiraStatus() {
        return fields().status();
    }

    public String summary() {
        return fields().summary();
    }

    private record Fields(JiraStatus status, String summary) {
    }

    public record JiraStatus(String name) {
    }
}
