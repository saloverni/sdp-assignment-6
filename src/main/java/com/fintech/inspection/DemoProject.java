package com.fintech.inspection;

import java.util.List;

public final class DemoProject {
    private DemoProject() {}

    public static Directory create() {
        return new Directory("fintech-project", List.of(
                new Directory("src", List.of(
                        new SourceCodeFile("PaymentService.java", "payments", 180, 14, true, true, false),
                        new SourceCodeFile("UserService.java", "users", 120, 6, false, false, true))),
                new Directory("tests", List.of(
                        new TestFile("PaymentServiceTest.java", "payments", 90, 8, 1))),
                new Directory("config", List.of(
                        new ConfigurationFile("application.properties", 25, true, false))),
                new DependencyFile("build.gradle", List.of(
                        new Dependency("org.example:payment-sdk", "1.0.0", true),
                        new Dependency("org.example:legacy-auth", "", false),
                        new Dependency("org.example:logging", "2.0.0", false)))));
    }
}
