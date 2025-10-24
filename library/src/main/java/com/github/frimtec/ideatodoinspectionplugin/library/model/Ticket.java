package com.github.frimtec.ideatodoinspectionplugin.library.model;


import java.util.Set;
import java.util.function.Function;

public interface Ticket {
    enum Status {
        OPEN,
        DONE,
        NOT_EXISTING,
        UNKNOWN,
        JIRA_CONFIGURATION_ERROR
    }

    String key();
    Status status(Function<String, Status> doneMapping);
    String summary();

    static Function<String, Status> statusMapper(Set<String> closedStates) {
        return name -> closedStates.contains(name) ? Status.DONE : Status.OPEN;
    }
}
