package com.fintech.inspection;

public record SourceCodeFile(String name, String module, int linesOfCode, int complexity,
                             boolean hardcodedCredentials, boolean highRisk, boolean documented) implements ProjectElement {
    public SourceCodeFile {
        Checks.text(name, "name");
        Checks.text(module, "module");
        Checks.nonNegative(linesOfCode, "linesOfCode");
        Checks.nonNegative(complexity, "complexity");
    }

    @Override
    public void accept(ProjectVisitor visitor) {
        visitor.visit(this);
    }
}
