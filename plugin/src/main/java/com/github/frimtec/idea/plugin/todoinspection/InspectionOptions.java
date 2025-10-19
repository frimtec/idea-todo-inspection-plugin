package com.github.frimtec.idea.plugin.todoinspection;

@SuppressWarnings({"AssignmentOrReturnOfFieldWithMutableType"})
final class InspectionOptions {
    private final String jiraUrl;

    public InspectionOptions(String jiraUrl) {
        this.jiraUrl = jiraUrl;
    }

    static InspectionOptions of(String jiraUrl) {
        return new InspectionOptions(jiraUrl);
    }

    public String getJiraUrl() {
        return jiraUrl;
    }
}