package com.github.frimtec.ideatodoinspectionplugin.library.model;

import java.util.Optional;

public class Todo {

    public enum Type {
        TODO,
        FIXME
    }

    public enum TodoStatus {
        CONSISTENT,
        NO_TICKET_REFERENCE,
        INCONSISTENT_TICKET_NOT_EXISTING,
        INCONSISTENT_TICKET_DONE,
        UNKNOWN_TICKET_STATUS
    }

    public record TextRange(int startOffset, int endOffset) {

    }

    private final Type type;
    private final TextRange textRange;
    private final TodoStatus status;
    private final Ticket ticket;

    public Todo(Type type, TextRange textRange, Ticket ticket) {
        this.type = type;
        this.textRange = textRange;
        this.ticket = ticket;
        this.status = switch (ticket.status()) {
            case OPEN -> TodoStatus.CONSISTENT;
            case DONE -> TodoStatus.INCONSISTENT_TICKET_DONE;
            case NOT_EXISTING -> TodoStatus.INCONSISTENT_TICKET_NOT_EXISTING;
            case UNKNOWN -> TodoStatus.UNKNOWN_TICKET_STATUS;
        };
    }

    public Todo(Type type, TextRange textRange, TodoStatus status) {
        this.type = type;
        this.textRange = textRange;
        this.status = status;
        this.ticket = null;
    }

    public Type type() {
        return type;
    }

    public TodoStatus status() {
        return status;
    }

    public Optional<Ticket> ticket() {
        return Optional.ofNullable(ticket);
    }

    public TextRange textRange() {
        return textRange;
    }

    @Override
    public String toString() {
        return "Todo{" +
                "type=" + type +
                ", textRange=" + textRange +
                ", status=" + status +
                ", ticket=" + ticket +
                '}';
    }
}
