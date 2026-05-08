package com.github.frimtec.idea.plugin.todoinspection;

import com.github.frimtec.ideatodoinspectionplugin.library.jira.ApiVersion;

record InspectionOptions(
        boolean allowFixme,
        String jiraUrl,
        ApiVersion apiVersion,
        String jiraUsername,
        Encoder jiraApiToken,
        String jiraProjectKeys,
        String jiraClosedStates
) {

    static InspectionOptions of(
            boolean allowFixme,
            String jiraUrl,
            ApiVersion apiVersion,
            String jiraUsername,
            Encoder jiraApiToken,
            String jiraProjectKeys,
            String jiraClosedStates
    ) {
        return new InspectionOptions(
                allowFixme,
                jiraUrl,
                apiVersion,
                jiraUsername,
                jiraApiToken,
                jiraProjectKeys,
                jiraClosedStates
        );
    }
}