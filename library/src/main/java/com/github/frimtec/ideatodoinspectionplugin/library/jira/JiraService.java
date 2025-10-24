package com.github.frimtec.ideatodoinspectionplugin.library.jira;

import com.github.frimtec.ideatodoinspectionplugin.library.model.Ticket;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.util.Base64;
import java.util.Objects;
import java.util.function.Function;

public class JiraService {

    private static final int SC_NOT_FOUND = 404;

    private record ErrorTicket(String key, Status status, String summary) implements Ticket {
        @Override
        public Status status(Function<String, Status> doneMapping) {
            return status;
        }
    }

    private final JiraRestService jiraRestService;
    private final String authorization;

    public JiraService(
            String jiraBaseUrl,
            String username,
            String apiToken,
            @Nullable TlsTrust tlsTrust
    ) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(jiraBaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient(tlsTrust))
                .build();
        this.jiraRestService = retrofit.create(JiraRestService.class);
        this.authorization = authorization(username, apiToken);
    }

    JiraService(JiraRestService jiraRestService, String username, String apiToken) {
        this.jiraRestService = jiraRestService;
        this.authorization = authorization(username, apiToken);
    }

    @NotNull
    private static String authorization(String username, String apiToken) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + apiToken).getBytes());
    }

    public record TlsTrust(SSLContext sslContext, X509TrustManager trustManager) {

    }

    private @NotNull OkHttpClient httpClient(@Nullable TlsTrust tlsTrust) {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        if (tlsTrust != null) {
            httpClientBuilder.sslSocketFactory(tlsTrust.sslContext().getSocketFactory(), tlsTrust.trustManager());
        }
        return httpClientBuilder.build();
    }

    public Ticket loadTicket(String issueKey) {
        try {
            Response<JiraTicket> response = callRestService(issueKey);
            if (response.isSuccessful()) {
                return Objects.requireNonNull(response.body());
            } else if (response.code() == SC_NOT_FOUND) {
                return new ErrorTicket(issueKey, Ticket.Status.NOT_EXISTING, "Ticket does not exist");
            } else {
                return new ErrorTicket(issueKey, Ticket.Status.UNKNOWN, "Error in jira remote call: %d %s".formatted(response.code(), response.message()));
            }
        } catch (IOException e) {
            return new ErrorTicket(issueKey, Ticket.Status.UNKNOWN, "Error in jira remote call: %s".formatted(e.getMessage()));
        }
    }

    @NotNull
    private Response<JiraTicket> callRestService(String issueKey) throws IOException {
        return this.jiraRestService.loadTicket(authorization, issueKey).execute();
    }
}
