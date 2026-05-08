package com.github.frimtec.ideatodoinspectionplugin.library.jira;

import java.io.IOException;
import java.util.Set;
import java.util.function.Function;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import com.github.frimtec.ideatodoinspectionplugin.library.model.Ticket;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

class JiraServiceTest {

  private final static Set<String> CLOSED_STATES = Set.of("Closed", "Done", "Resolved");
  private static final Function<String, Ticket.Status> DONE_MAPPING = Ticket.statusMapper(CLOSED_STATES);

  private static final String TICKET_ID = "TICKET-123";

  private static final String JIRA_ISSUE_URL_TEMPLATE = "/rest/api/2/issue/%s?fields=status,summary";
  private static final int TEST_PORT = 8080;
  private static final String TEST_HOST = "localhost";
  @SuppressWarnings("HttpUrlsUsage")

  private final JiraService serviceV2 = new JiraService(
      "http://%s:%d/".formatted(TEST_HOST, TEST_PORT),
      ApiVersion.V2,
      "user",
      "token",
      null
  );

  private final JiraService serviceV3 = new JiraService(
      "http://%s:%d/".formatted(TEST_HOST, TEST_PORT),
      ApiVersion.V3,
      "user",
      "token",
      null
  );

  private WireMockServer wireMockServer;

  @BeforeEach
  public void setup() {
    wireMockServer = new WireMockServer(options().port(TEST_PORT)); // Choose any available port
    wireMockServer.start();
    WireMock.configureFor(TEST_HOST, TEST_PORT);
  }

  @AfterEach
  public void tearDown() {
    wireMockServer.stop();
  }


  @ParameterizedTest
  @CsvSource({
      "Open,OPEN",
      "In Progress,OPEN",
      "Resolved,DONE",
      "Done,DONE",
      "Closed,DONE",

  })
  void loadExistingTicket(String jiraStatus, String expectedTicketStatus) {
    stubFor(get(urlEqualTo(JIRA_ISSUE_URL_TEMPLATE.formatted(TICKET_ID)))
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_OK)
            .withHeader("Content-Type", "application/json")
            .withBody("""
                {
                  "expand": "renderedFields,names,schema,operations,editmeta,changelog,versionedRepresentations",
                  "id": "308991",
                  "self": "http://localhost:8080/jira/rest/api/2/issue/308991",
                  "key": "%s",
                  "fields": {
                    "summary": "Ticket summary",
                    "status": {
                      "self": "http://localhost:8080/jira/rest/api/2/status/1",
                      "name": "%s",
                      "id": "1",
                      "statusCategory": {
                        "self": "http://localhost:8080/jira/rest/api/2/statuscategory/2",
                        "id": 2,
                        "key": "new",
                        "colorName": "default",
                        "name": "To Do"
                      }
                    }
                  },
                  "renderedFields": null
                }"""
                .formatted(TICKET_ID, jiraStatus)
            )
        )
    );

    Ticket ticket = serviceV2.loadTicket(TICKET_ID);

    assertThat(ticket.key()).isEqualTo(TICKET_ID);
    assertThat(ticket.summary()).isEqualTo("Ticket summary");
    assertThat(ticket.status(DONE_MAPPING)).isEqualTo(Ticket.Status.valueOf(expectedTicketStatus));
  }

  @Test
  void loadNotExistingTicket() {
    stubFor(get(urlEqualTo(JIRA_ISSUE_URL_TEMPLATE.formatted(TICKET_ID)))
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_NOT_FOUND)
        )
    );

    Ticket ticket = serviceV2.loadTicket(TICKET_ID);

    assertThat(ticket.key()).isEqualTo(TICKET_ID);
    assertThat(ticket.summary()).isEqualTo("Ticket does not exist");
    assertThat(ticket.status(DONE_MAPPING)).isEqualTo(Ticket.Status.NOT_EXISTING);
  }

  @Test
  void loadForInternalServerError() {
    stubFor(get(urlEqualTo(JIRA_ISSUE_URL_TEMPLATE.formatted(TICKET_ID)))
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR)
        )
    );

    Ticket ticket = serviceV2.loadTicket(TICKET_ID);

    assertThat(ticket.key()).isEqualTo(TICKET_ID);
    assertThat(ticket.summary()).isEqualTo("Error in jira remote call: 500 Server Error");
    assertThat(ticket.status(DONE_MAPPING)).isEqualTo(Ticket.Status.UNKNOWN);
  }

  @Test
  void loadForIoException() throws IOException {
    JiraRestService restService = Mockito.mock(JiraRestService.class);
    JiraService service = new JiraService(restService, ApiVersion.V2, "user", "token");
    Mockito.when(restService.loadTicket(Mockito.anyString(), Mockito.eq(2), Mockito.anyString()))
        .thenThrow(new IOException("Test exception"));

    Ticket ticket = service.loadTicket(TICKET_ID);

    assertThat(ticket.key()).isEqualTo(TICKET_ID);
    assertThat(ticket.summary()).isEqualTo("Error in jira remote call: Test exception");
    assertThat(ticket.status(DONE_MAPPING)).isEqualTo(Ticket.Status.UNKNOWN);
  }

  @Test
  void pingV2() {
    stubFor(get(WireMock.urlMatching("/rest/api/2/search/\\?jql=project%3DPRJ1&fields=status%2Csummary&maxResults=1"))
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_OK)
            .withHeader("Content-Type", "application/json")
            .withBody("""
                {
                  "issues": [
                    {
                      "expand": "renderedFields,names,schema,operations,editmeta,changelog,versionedRepresentations",
                      "id": "308991",
                      "self": "http://localhost:8080/jira/rest/api/2/issue/308991",
                      "key": "%s",
                      "fields": {
                        "summary": "Ticket summary",
                        "status": {
                          "self": "http://localhost:8080/jira/rest/api/2/status/1",
                          "name": "%s",
                          "id": "1",
                          "statusCategory": {
                            "self": "http://localhost:8080/jira/rest/api/2/statuscategory/2",
                            "id": 2,
                            "key": "new",
                            "colorName": "default",
                            "name": "To Do"
                          }
                        }
                      },
                      "renderedFields": null
                    }
                  ]
                }"""
                .formatted(TICKET_ID, "Open")
            )
        )
    );

    Ticket ticket = serviceV2.ping(Set.of("PRJ1"));
    assertThat(ticket.key()).isEqualTo(TICKET_ID);
    assertThat(ticket.summary()).isEqualTo("Ticket summary");
  }

  @Test
  void pingV3() {
    stubFor(get(WireMock.urlMatching("/rest/api/3/search/jql\\?jql=project%3DPRJ1&fields=status%2Csummary&maxResults=1"))
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_OK)
            .withHeader("Content-Type", "application/json")
            .withBody("""
                {
                  "issues": [
                    {
                      "expand": "renderedFields,names,schema,operations,editmeta,changelog,versionedRepresentations",
                      "id": "308991",
                      "self": "http://localhost:8080/jira/rest/api/2/issue/308991",
                      "key": "%s",
                      "fields": {
                        "summary": "Ticket summary",
                        "status": {
                          "self": "http://localhost:8080/jira/rest/api/2/status/1",
                          "name": "%s",
                          "id": "1",
                          "statusCategory": {
                            "self": "http://localhost:8080/jira/rest/api/2/statuscategory/2",
                            "id": 2,
                            "key": "new",
                            "colorName": "default",
                            "name": "To Do"
                          }
                        }
                      },
                      "renderedFields": null
                    }
                  ]
                }"""
                .formatted(TICKET_ID, "Open")
            )
        )
    );

    Ticket ticket = serviceV3.ping(Set.of("PRJ1"));
    assertThat(ticket.key()).isEqualTo(TICKET_ID);
    assertThat(ticket.summary()).isEqualTo("Ticket summary");
  }

}