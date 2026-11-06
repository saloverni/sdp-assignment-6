package com.fintech.inspection;

import java.util.Locale;
import java.util.Set;

public final class Main {
    private Main() {}

    public static void main(String[] args) {
        Directory project = DemoProject.create();
        System.out.println("Project: " + project.name());

        SecurityAuditVisitor security = new SecurityAuditVisitor();
        project.accept(security);
        System.out.println("\nSECURITY AUDIT\nScore: " + security.score() + "/100");
        security.issues().forEach(issue -> System.out.println(
                (issue.critical() ? "CRITICAL" : "WARNING") + " | " + issue.file() + " | " + issue.message()));

        ProjectStatisticsVisitor statistics = new ProjectStatisticsVisitor();
        project.accept(statistics);
        System.out.printf(Locale.ROOT,
                "\nPROJECT STATISTICS\nFiles: %d\nLines of code: %d\nTests: %d\nDependencies: %d"
                        + "\nAverage complexity: %.2f\nTest/source ratio: %.2f%n",
                statistics.totalFiles(), statistics.linesOfCode(), statistics.numberOfTests(),
                statistics.dependencies(), statistics.averageComplexity(), statistics.testSourceRatio());

        QualityGateVisitor quality = new QualityGateVisitor();
        project.accept(quality);
        System.out.println("\nQUALITY GATE\n" + quality.result().report());

        ComplianceVisitor compliance = new ComplianceVisitor(Set.of("org.example:legacy-auth"));
        project.accept(compliance);
        System.out.println("\nCOMPLIANCE: " + (compliance.compliant() ? "PASS" : "FAIL"));
        compliance.violations().forEach(System.out::println);
    }
}
