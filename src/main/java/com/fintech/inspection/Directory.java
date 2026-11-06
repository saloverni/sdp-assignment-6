package com.fintech.inspection;

public record Directory(String name, java.util.List<ProjectElement> children) implements ProjectElement {
    public Directory {
        Checks.text(name, "name");
        children = java.util.List.copyOf(children);
    }

    @Override
    public void accept(ProjectVisitor visitor) {
        visitor.visit(this);
    }
}
