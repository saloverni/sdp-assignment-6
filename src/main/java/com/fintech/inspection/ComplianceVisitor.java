package com.fintech.inspection;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class ComplianceVisitor extends AbstractProjectVisitor {
    private final Set<String> prohibitedDependencies;
    private final List<String> violations = new ArrayList<>();
    private final Set<String> productionModules = new LinkedHashSet<>();
    private final Set<String> testedModules = new LinkedHashSet<>();

    public ComplianceVisitor(Set<String> prohibitedDependencies) {
        this.prohibitedDependencies = Set.copyOf(prohibitedDependencies);
    }

    @Override
    public void visit(SourceCodeFile file) {
        productionModules.add(file.module());
        if (file.hardcodedCredentials()) {
            violations.add(file.name() + ": exposed credentials");
        }
    }

    @Override
    public void visit(TestFile file) {
        if (file.numberOfTests() > file.disabledTests()) {
            testedModules.add(file.module());
        }
    }

    @Override
    public void visit(ConfigurationFile file) {
        if (file.exposedCredentials()) {
            violations.add(file.name() + ": exposed credentials");
        }
    }

    @Override
    public void visit(DependencyFile file) {
        for (Dependency dependency : file.dependencies()) {
            if (dependency.version().isBlank()) {
                violations.add(file.name() + ": missing version for " + dependency.name());
            }
            if (prohibitedDependencies.contains(dependency.name())) {
                violations.add(file.name() + ": prohibited dependency " + dependency.name());
            }
        }
    }

    public List<String> violations() {
        List<String> result = new ArrayList<>(violations);
        for (String module : productionModules) {
            if (!testedModules.contains(module)) {
                result.add(module + ": production module has no active tests");
            }
        }
        return List.copyOf(result);
    }

    public boolean compliant() { return violations().isEmpty(); }
}
