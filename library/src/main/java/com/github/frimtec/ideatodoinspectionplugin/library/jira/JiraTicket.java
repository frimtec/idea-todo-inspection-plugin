package com.github.frimtec.ideatodoinspectionplugin.library.jira;


import com.github.frimtec.ideatodoinspectionplugin.library.model.Ticket;

import java.util.Set;

record JiraTicket(String key, Fields fields) implements Ticket {

    @Override
    public Status status() {
        return jiraStatus().done() ? Status.DONE : Status.OPEN;
    }

    public JiraStatus jiraStatus() {
        return fields().status();
    }

    public String summary() {
        return fields().summary();
    }

    public boolean done() {
        return this.jiraStatus().done();
    }

    private record Fields(JiraStatus status, String summary) {
    }

    public record JiraStatus(String name) {
        private final static Set<String> CLOSED_STATES = Set.of("Closed", "Done", "Resolved");

        public boolean done() {
            return CLOSED_STATES.contains(this.name());
        }
    }
}
