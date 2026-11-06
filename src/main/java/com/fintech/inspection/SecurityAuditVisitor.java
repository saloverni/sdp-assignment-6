package com.fintech.inspection;

import java.util.ArrayList;
import java.util.List;

public final class SecurityAuditVisitor extends AbstractProjectVisitor {
    public record Issue(String file, String message, boolean critical) {}

    private final List<Issue> issues = new ArrayList<>();

    @Override
    public void visit(SourceCodeFile file) {
        if (file.hardcodedCredentials()) {
            issues.add(new Issue(file.name(), "Hardcoded credentials", true));
        }
        if (file.highRisk()) {
            issues.add(new Issue(file.name(), "High-risk source file", false));
        }
    }

    @Override
    public void visit(ConfigurationFile file) {
        if (file.exposedCredentials()) {
            issues.add(new Issue(file.name(), "Exposed credentials", true));
        }
        if (!file.secure()) {
            issues.add(new Issue(file.name(), "Insecure configuration", true));
        }
    }

    @Override
    public void visit(DependencyFile file) {
        for (Dependency dependency : file.dependencies()) {
            if (dependency.vulnerable()) {
                issues.add(new Issue(file.name(), "Vulnerable dependency: " + dependency.name(), true));
            }
        }
    }

    public List<Issue> issues() {
        return List.copyOf(issues);
    }

    public long criticalIssues() {
        return issues.stream().filter(Issue::critical).count();
    }

    public int score() {
        return Math.max(0, 100 - issues.stream().mapToInt(issue -> issue.critical() ? 25 : 10).sum());
    }
}
