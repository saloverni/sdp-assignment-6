package com.fintech.inspection;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class InspectionTest {
    private SourceCodeFile source(String module, int complexity) {
        return new SourceCodeFile(module + ".java", module, 100, complexity, false, false, true);
    }

    private Directory project(ProjectElement... elements) {
        return new Directory("project", List.of(elements));
    }

    @Test
    void dispatchesEveryElementToItsTypedOverload() {
        List<String> visited = new ArrayList<>();
        ProjectVisitor visitor = new ProjectVisitor() {
            public void visit(SourceCodeFile file) { visited.add("source"); }
            public void visit(TestFile file) { visited.add("test"); }
            public void visit(ConfigurationFile file) { visited.add("config"); }
            public void visit(DependencyFile file) { visited.add("dependency"); }
            public void visit(Directory directory) { visited.add("directory"); }
        };
        List<ProjectElement> elements = List.of(source("payments", 1),
                new TestFile("Test.java", "payments", 5, 1, 0),
                new ConfigurationFile("app.properties", 5, false, true),
                new DependencyFile("build.gradle", List.of()), project());
        elements.forEach(element -> element.accept(visitor));
        assertEquals(List.of("source", "test", "config", "dependency", "directory"), visited);
    }

    @Test
    void detectsHardcodedCredentialsAndHighRiskSource() {
        SecurityAuditVisitor visitor = new SecurityAuditVisitor();
        new SourceCodeFile("Payment.java", "payments", 20, 15, true, true, false).accept(visitor);
        assertEquals(2, visitor.issues().size());
        assertEquals(1, visitor.criticalIssues());
        assertEquals(65, visitor.score());
    }

    @Test
    void detectsBothConfigurationProblems() {
        SecurityAuditVisitor visitor = new SecurityAuditVisitor();
        new ConfigurationFile("app.properties", 5, true, false).accept(visitor);
        assertEquals(2, visitor.criticalIssues());
        assertEquals(50, visitor.score());
    }

    @Test
    void reportsOnlyVulnerableDependencies() {
        SecurityAuditVisitor visitor = new SecurityAuditVisitor();
        new DependencyFile("build.gradle", List.of(new Dependency("safe", "1", false),
                new Dependency("unsafe", "2", true))).accept(visitor);
        assertEquals(1, visitor.criticalIssues());
        assertTrue(visitor.issues().get(0).message().contains("unsafe"));
    }

    @Test
    void cleanProjectHasPerfectSecurityScore() {
        SecurityAuditVisitor visitor = new SecurityAuditVisitor();
        project(source("payments", 3), new ConfigurationFile("app", 2, false, true)).accept(visitor);
        assertTrue(visitor.issues().isEmpty());
        assertEquals(100, visitor.score());
    }

    @Test
    void securityScoreNeverBecomesNegative() {
        SecurityAuditVisitor visitor = new SecurityAuditVisitor();
        DemoProject.create().accept(visitor);
        assertEquals(4, visitor.criticalIssues());
        assertEquals(0, visitor.score());
    }

    @Test
    void calculatesAllDemoStatistics() {
        ProjectStatisticsVisitor visitor = new ProjectStatisticsVisitor();
        DemoProject.create().accept(visitor);
        assertAll(() -> assertEquals(5, visitor.totalFiles()),
                () -> assertEquals(415, visitor.linesOfCode()),
                () -> assertEquals(8, visitor.numberOfTests()),
                () -> assertEquals(3, visitor.dependencies()),
                () -> assertEquals(2, visitor.sourceFiles()),
                () -> assertEquals(1, visitor.testFiles()),
                () -> assertEquals(10.0, visitor.averageComplexity()),
                () -> assertEquals(0.5, visitor.testSourceRatio()));
    }

    @Test
    void traversesDeeplyNestedDirectoriesExactlyOnce() {
        ProjectElement nested = source("payments", 7);
        for (int i = 0; i < 100; i++) {
            nested = project(nested);
        }
        ProjectStatisticsVisitor visitor = new ProjectStatisticsVisitor();
        nested.accept(visitor);
        assertEquals(1, visitor.totalFiles());
        assertEquals(100, visitor.linesOfCode());
        assertEquals(7.0, visitor.averageComplexity());
    }

    @Test
    void emptyDirectoryHasFiniteZeroStatistics() {
        ProjectStatisticsVisitor visitor = new ProjectStatisticsVisitor();
        project().accept(visitor);
        assertEquals(0, visitor.totalFiles());
        assertEquals(0, visitor.linesOfCode());
        assertEquals(0.0, visitor.averageComplexity());
        assertEquals(0.0, visitor.testSourceRatio());
    }

    @Test
    void qualityGatePassesAtInclusiveThresholds() {
        QualityGateVisitor visitor = new QualityGateVisitor();
        project(source("a", 10), source("b", 10), source("c", 10), source("d", 10), source("e", 10),
                new TestFile("ATest", "a", 10, 1, 0), new TestFile("BTest", "b", 10, 1, 0))
                .accept(visitor);
        assertTrue(visitor.result().passed());
        assertTrue(visitor.result().report().contains("FINAL RESULT: PASSED"));
    }

    @Test
    void qualityGateFailsForCriticalSecurityIssue() {
        QualityGateVisitor visitor = new QualityGateVisitor();
        project(source("a", 5), new TestFile("ATest", "a", 10, 1, 0),
                new ConfigurationFile("app", 1, true, true)).accept(visitor);
        assertFalse(visitor.result().security());
        assertTrue(visitor.result().complexity());
        assertTrue(visitor.result().tests());
        assertFalse(visitor.result().passed());
    }

    @Test
    void qualityGateFailsForExcessiveAverageComplexity() {
        QualityGateVisitor visitor = new QualityGateVisitor();
        project(source("a", 11), new TestFile("ATest", "a", 10, 1, 0)).accept(visitor);
        assertFalse(visitor.result().complexity());
        assertTrue(visitor.result().security());
        assertTrue(visitor.result().tests());
        assertFalse(visitor.result().passed());
    }

    @Test
    void qualityGateFailsWithTooFewTestFiles() {
        QualityGateVisitor visitor = new QualityGateVisitor();
        project(source("a", 3), source("b", 3), source("c", 3),
                new TestFile("ATest", "a", 10, 10, 0)).accept(visitor);
        assertFalse(visitor.result().tests());
        assertFalse(visitor.result().passed());
        assertTrue(visitor.result().report().contains("FINAL RESULT: FAILED"));
    }

    @Test
    void emptyProjectCannotBeReleased() {
        QualityGateVisitor visitor = new QualityGateVisitor();
        project().accept(visitor);
        assertFalse(visitor.result().passed());
    }

    @Test
    void checksVersionsAndProhibitedDependencies() {
        ComplianceVisitor visitor = new ComplianceVisitor(Set.of("legacy"));
        new DependencyFile("build.gradle", List.of(new Dependency("legacy", " ", false))).accept(visitor);
        assertEquals(List.of("build.gradle: missing version for legacy",
                "build.gradle: prohibited dependency legacy"), visitor.violations());
        assertFalse(visitor.compliant());
    }

    @Test
    void detectsCredentialsInSourceAndConfiguration() {
        ComplianceVisitor visitor = new ComplianceVisitor(Set.of());
        project(new SourceCodeFile("A", "a", 1, 1, true, false, true),
                new ConfigurationFile("app", 1, true, true),
                new TestFile("ATest", "a", 1, 1, 0)).accept(visitor);
        assertEquals(List.of("A: exposed credentials", "app: exposed credentials"), visitor.violations());
    }

    @Test
    void matchesTestsToModulesRegardlessOfTraversalOrder() {
        ComplianceVisitor visitor = new ComplianceVisitor(Set.of());
        project(new TestFile("ATest", "a", 1, 1, 0), project(source("a", 2))).accept(visitor);
        assertTrue(visitor.compliant());
    }

    @Test
    void disabledTestsDoNotSatisfyModuleCompliance() {
        ComplianceVisitor visitor = new ComplianceVisitor(Set.of());
        project(source("a", 2), new TestFile("ATest", "a", 1, 2, 2)).accept(visitor);
        assertEquals(List.of("a: production module has no active tests"), visitor.violations());
        assertEquals(visitor.violations(), visitor.violations());
    }

    @Test
    void reportsMissingTestsOncePerModule() {
        ComplianceVisitor visitor = new ComplianceVisitor(Set.of());
        project(source("a", 2), source("a", 3), new TestFile("BTest", "b", 1, 1, 0)).accept(visitor);
        assertEquals(List.of("a: production module has no active tests"), visitor.violations());
    }

    @Test
    void calculatesTechnicalDebtAcrossNestedProject() {
        TechnicalDebtVisitor visitor = new TechnicalDebtVisitor();
        DemoProject.create().accept(visitor);
        assertEquals(145, visitor.totalMinutes());
        assertEquals(List.of(120L, 15L, 10L), visitor.debts().stream()
                .map(TechnicalDebtVisitor.Debt::minutes).toList());
    }

    @Test
    void cleanSourceAndActiveTestsHaveNoTechnicalDebt() {
        TechnicalDebtVisitor visitor = new TechnicalDebtVisitor();
        project(source("a", 10), new TestFile("ATest", "a", 1, 2, 0)).accept(visitor);
        assertEquals(0, visitor.totalMinutes());
        assertTrue(visitor.debts().isEmpty());
    }

    @Test
    void rejectsInvalidElementData() {
        assertThrows(IllegalArgumentException.class, () -> source(" ", 3));
        assertThrows(IllegalArgumentException.class, () -> source("a", -1));
        assertThrows(IllegalArgumentException.class, () -> new TestFile("T", "a", 1, 1, 2));
        assertThrows(IllegalArgumentException.class, () -> new ConfigurationFile("app", -1, false, true));
    }

    @Test
    void collectionsAreDefensivelyCopied() {
        List<ProjectElement> children = new ArrayList<>();
        children.add(source("a", 1));
        Directory directory = new Directory("src", children);
        children.clear();
        assertEquals(1, directory.children().size());
        assertThrows(UnsupportedOperationException.class, () -> directory.children().clear());
        List<Dependency> dependencies = new ArrayList<>(List.of(new Dependency("lib", "1", false)));
        DependencyFile file = new DependencyFile("build.gradle", dependencies);
        dependencies.clear();
        assertEquals(1, file.dependencies().size());
    }

    @Test
    void nullVersionIsReportedAsMissing() {
        ComplianceVisitor visitor = new ComplianceVisitor(Set.of());
        new DependencyFile("build.gradle", List.of(new Dependency("lib", null, false))).accept(visitor);
        assertEquals(List.of("build.gradle: missing version for lib"), visitor.violations());
    }
}
