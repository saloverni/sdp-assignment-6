package com.fintech.inspection;

public record ConfigurationFile(String name, int linesOfCode, boolean exposedCredentials, boolean secure) implements ProjectElement {
    public ConfigurationFile {
        Checks.text(name, "name");
        Checks.nonNegative(linesOfCode, "linesOfCode");
    }

    @Override
    public void accept(ProjectVisitor visitor) {
        visitor.visit(this);
    }
}
