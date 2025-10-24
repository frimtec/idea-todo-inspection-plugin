package com.github.frimtec.ideatodoinspectionplugin.library.scanner;

import com.github.frimtec.ideatodoinspectionplugin.library.jira.JiraService;
import com.github.frimtec.ideatodoinspectionplugin.library.model.Ticket;
import com.github.frimtec.ideatodoinspectionplugin.library.model.Todo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodoScannerTest {

    @Mock
    private JiraService jiraService;

    @Test
    void parseTodoForTodoWithNoTicketReference() {
        List<Todo> todos = scanner().parseTodo("""
                // TODO bla fasel
                """);
        assertThat(todos).hasSize(1);
        Todo todo = todos.getFirst();
        assertThat(todo.type()).isEqualTo(Todo.Type.TODO);
        assertThat(todo.status()).isEqualTo(Todo.TodoStatus.NO_TICKET_REFERENCE);
        assertThat(todo.ticket()).isEmpty();
        assertThat(todo.textRange()).isEqualTo(new Todo.TextRange(3, 17));
    }

    @Test
    void parseTodoForTodoWithMultiline() {
        mockTicket("SICIV-17425", Ticket.Status.DONE);
        mockTicket("TOOL-453944", Ticket.Status.NOT_EXISTING);

        List<Todo> todos = scanner().parseTodo("""
                /* bla
                 * TODO tkf9m SICIV-17425 Bla bla
                 * bla
                 * FIXME TOOL-453944
                 * TODO fasel no ticket
                 */
                """);
        assertThat(todos).hasSize(3);

        Todo todo = todos.getFirst();
        assertThat(todo.type()).isEqualTo(Todo.Type.TODO);
        assertThat(todo.status()).isEqualTo(Todo.TodoStatus.INCONSISTENT_TICKET_DONE);
        assertThat(todo.ticket()).isNotEmpty();
        assertThat(todo.textRange()).isEqualTo(new Todo.TextRange(10, 40));

        todo = todos.get(1);
        assertThat(todo.type()).isEqualTo(Todo.Type.FIXME);
        assertThat(todo.status()).isEqualTo(Todo.TodoStatus.INCONSISTENT_TICKET_NOT_EXISTING);
        assertThat(todo.ticket()).isNotEmpty();
        assertThat(todo.textRange()).isEqualTo(new Todo.TextRange(51, 68));

        todo = todos.get(2);
        assertThat(todo.type()).isEqualTo(Todo.Type.TODO);
        assertThat(todo.status()).isEqualTo(Todo.TodoStatus.NO_TICKET_REFERENCE);
        assertThat(todo.ticket()).isEmpty();
        assertThat(todo.textRange()).isEqualTo(new Todo.TextRange(72, 92));
    }

    private void mockTicket(String ticketReference, Ticket.Status status) {
        Ticket ticket = mock(Ticket.class);
        when(ticket.status(any())).thenReturn(status);
        when(jiraService.loadTicket(ticketReference)).thenReturn(ticket);
    }

    private TodoScanner scanner() {
        return new TodoScanner(
                jiraService,
                Set.of("SICIV", "SIC5", "TOOL"),
                Ticket.statusMapper(Set.of("Closed", "Done", "Resolved"))
        );
    }

}