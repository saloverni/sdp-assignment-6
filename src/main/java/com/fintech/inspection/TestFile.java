package com.fintech.inspection;

public record TestFile(String name, String module, int linesOfCode, int numberOfTests, int disabledTests) implements ProjectElement {
    public TestFile {
        Checks.text(name, "name");
        Checks.text(module, "module");
        Checks.nonNegative(linesOfCode, "linesOfCode");
        Checks.nonNegative(numberOfTests, "numberOfTests");
        Checks.nonNegative(disabledTests, "disabledTests");
        if (disabledTests > numberOfTests) {
            throw new IllegalArgumentException("Disabled tests exceed total tests");
        }
    }

    @Override
    public void accept(ProjectVisitor visitor) {
        visitor.visit(this);
    }
}
