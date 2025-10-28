package com.github.frimtec.idea.plugin.todoinspection.quickfix;

enum QuickFixPriority {
    HIGH('\u200A'),
    MEDIUM('\u200B'),
    LOW('\u200C');

    private final char prefix;

    QuickFixPriority(char prefix) {
        this.prefix = prefix;
    }

    public static String orderedLabel(QuickFixPriority priority, String label) {
        return priority.prefix + label;
    }
}
