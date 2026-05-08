package com.github.frimtec.ideatodoinspectionplugin.library.jira;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.io.IOException;

interface JiraRestService {
    @GET("rest/api/{apiVersion}/issue/{issueKey}?fields=status,summary")
    Call<JiraTicket> loadTicket(
            @Header("Authorization") String authorization,
            @Path("apiVersion") int apiVersion,
            @Path("issueKey") String issueKey
    ) throws IOException;

    @GET("rest/api/{apiVersion}/search/{searchType}")
    Call<JiraSearchResult> search(
        @Header("Authorization") String authorization,
        @Path("apiVersion") int apiVersion,
        @Path("searchType") String searchType,
        @Query("jql") String jqlQuery,
        @Query("fields") String fields,
        @Query("maxResults") int maxResults
    ) throws IOException;
}
