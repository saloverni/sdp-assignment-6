package com.fintech.inspection;

public record Dependency(String name, String version, boolean vulnerable) {
    public Dependency {
        Checks.text(name, "name");
        version = version == null ? "" : version.trim();
    }
}
