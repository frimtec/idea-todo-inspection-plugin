package com.github.frimtec.ideatodoinspectionplugin.library.jira;

import java.util.List;

@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
public record JiraSearchResult(List<JiraTicket> issues) {
}
