package com.github.frimtec.idea.plugin.todoinspection;

record InspectionOptions(
        String allowFixme,
        String jiraUrl,
        String jiraUsername,
        String jiraApiToken,
        String jiraProjectKeys,
        String jiraClosedStates
) {

    static InspectionOptions of(
            String allowFixme,
            String jiraUrl,
            String jiraUsername,
            String jiraApiToken,
            String jiraProjectKeys,
            String jiraClosedStates
    ) {
        return new InspectionOptions(
                allowFixme,
                jiraUrl,
                jiraUsername,
                jiraApiToken,
                jiraProjectKeys,
                jiraClosedStates
        );
    }
}