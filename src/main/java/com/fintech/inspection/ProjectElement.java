package com.fintech.inspection;

public interface ProjectElement {
    void accept(ProjectVisitor visitor);
}
