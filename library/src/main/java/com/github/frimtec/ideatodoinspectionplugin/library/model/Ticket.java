package com.github.frimtec.ideatodoinspectionplugin.library.model;


public interface Ticket {
    enum Status {
        OPEN,
        DONE,
        NOT_EXISTING,
        UNKNOWN
    }

    String key();
    Status status();
    String summary();
}
