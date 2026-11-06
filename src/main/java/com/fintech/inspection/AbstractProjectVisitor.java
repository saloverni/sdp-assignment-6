package com.fintech.inspection;

public abstract class AbstractProjectVisitor implements ProjectVisitor {
    @Override
    public void visit(SourceCodeFile file) {}

    @Override
    public void visit(TestFile file) {}

    @Override
    public void visit(ConfigurationFile file) {}

    @Override
    public void visit(DependencyFile file) {}

    @Override
    public void visit(Directory directory) {
        for (ProjectElement child : directory.children()) {
            child.accept(this);
        }
    }
}
