package com.github.frimtec.ideatodoinspectionplugin.library.jira;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

import java.io.IOException;

interface JiraRestService {
    @GET("rest/api/2/issue/{issueKey}?fields=status,summary")
    Call<JiraTicket> loadTicket(
            @Header("Authorization") String authorization,
            @Path("issueKey") String issueKey
    ) throws IOException;
}
