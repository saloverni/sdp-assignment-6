package com.fintech.inspection;

public record DependencyFile(String name, java.util.List<Dependency> dependencies) implements ProjectElement {
    public DependencyFile {
        Checks.text(name, "name");
        dependencies = java.util.List.copyOf(dependencies);
    }

    @Override
    public void accept(ProjectVisitor visitor) {
        visitor.visit(this);
    }
}
