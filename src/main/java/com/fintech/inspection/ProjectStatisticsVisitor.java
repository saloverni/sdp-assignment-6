package com.fintech.inspection;

public final class ProjectStatisticsVisitor extends AbstractProjectVisitor {
    private int totalFiles;
    private long linesOfCode;
    private int sourceFiles;
    private int testFiles;
    private long numberOfTests;
    private long dependencies;
    private long totalComplexity;

    @Override
    public void visit(SourceCodeFile file) {
        totalFiles++;
        sourceFiles++;
        linesOfCode += file.linesOfCode();
        totalComplexity += file.complexity();
    }

    @Override
    public void visit(TestFile file) {
        totalFiles++;
        testFiles++;
        linesOfCode += file.linesOfCode();
        numberOfTests += file.numberOfTests();
    }

    @Override
    public void visit(ConfigurationFile file) {
        totalFiles++;
        linesOfCode += file.linesOfCode();
    }

    @Override
    public void visit(DependencyFile file) {
        totalFiles++;
        dependencies += file.dependencies().size();
    }

    public int totalFiles() { return totalFiles; }
    public long linesOfCode() { return linesOfCode; }
    public int sourceFiles() { return sourceFiles; }
    public int testFiles() { return testFiles; }
    public long numberOfTests() { return numberOfTests; }
    public long dependencies() { return dependencies; }
    public double averageComplexity() {
        return sourceFiles == 0 ? 0.0 : (double) totalComplexity / sourceFiles;
    }
    public double testSourceRatio() {
        return sourceFiles == 0 ? 0.0 : (double) testFiles / sourceFiles;
    }
}
