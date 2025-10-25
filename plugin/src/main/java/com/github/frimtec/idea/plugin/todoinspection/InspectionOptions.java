package com.github.frimtec.idea.plugin.todoinspection;

record InspectionOptions(
        boolean allowFixme,
        String jiraUrl,
        String jiraUsername,
        Encoder jiraApiToken,
        String jiraProjectKeys,
        String jiraClosedStates
) {

    static InspectionOptions of(
            boolean allowFixme,
            String jiraUrl,
            String jiraUsername,
            Encoder jiraApiToken,
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