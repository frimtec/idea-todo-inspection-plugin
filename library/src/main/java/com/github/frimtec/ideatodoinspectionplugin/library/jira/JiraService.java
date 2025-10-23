package com.github.frimtec.ideatodoinspectionplugin.library.jira;

import com.github.frimtec.ideatodoinspectionplugin.library.model.Ticket;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.Objects;

public class JiraService {

    private static final int SC_NOT_FOUND = 404;

    private record ErrorTicket(String key, Status status, String summary) implements Ticket {
    }

    private final JiraRestService jiraRestService;
    private final String authorization;

    public JiraService(String jiraBaseUrl, String username, String apiToken, @Nullable Path trustStorePath) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(jiraBaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient(trustStorePath))
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

    private @NotNull OkHttpClient httpClient(@Nullable Path trustStorePath) {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        if (trustStorePath != null) {
            try {
                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                try (FileInputStream fis = new FileInputStream(trustStorePath.toFile())) {
                    trustStore.load(fis, "".toCharArray());
                }

                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(trustStore);

                X509TrustManager trustManager = null;
                for (TrustManager tm : trustManagerFactory.getTrustManagers()) {
                    if (tm instanceof X509TrustManager) {
                        trustManager = (X509TrustManager) tm;
                        break;
                    }
                }

                if (trustManager != null) {
                    SSLContext sslContext = SSLContext.getInstance("TLS");
                    sslContext.init(null, new TrustManager[]{trustManager}, null);
                    httpClientBuilder.sslSocketFactory(sslContext.getSocketFactory(), trustManager);
                } else {
                    throw new RuntimeException("No X509TrustManager found in the TrustManagerFactory");
                }

            } catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException |
                     KeyManagementException e) {
                throw new RuntimeException("Cannot configure truststore", e);
            }
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
