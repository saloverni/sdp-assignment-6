package com.fintech.inspection;

public final class QualityGateVisitor extends AbstractProjectVisitor {
    public record Result(boolean security, boolean complexity, boolean tests) {
        public boolean passed() { return security && complexity && tests; }

        public String report() {
            return "Security: " + status(security) + "\nComplexity: " + status(complexity)
                    + "\nTests: " + status(tests) + "\n\nFINAL RESULT: "
                    + (passed() ? "PASSED" : "FAILED");
        }

        private static String status(boolean passed) { return passed ? "PASS" : "FAIL"; }
    }

    private final SecurityAuditVisitor security = new SecurityAuditVisitor();
    private final ProjectStatisticsVisitor statistics = new ProjectStatisticsVisitor();

    @Override
    public void visit(SourceCodeFile file) {
        file.accept(security);
        file.accept(statistics);
    }

    @Override
    public void visit(TestFile file) {
        file.accept(security);
        file.accept(statistics);
    }

    @Override
    public void visit(ConfigurationFile file) {
        file.accept(security);
        file.accept(statistics);
    }

    @Override
    public void visit(DependencyFile file) {
        file.accept(security);
        file.accept(statistics);
    }

    public Result result() {
        return new Result(security.criticalIssues() == 0, statistics.averageComplexity() <= 10,
                statistics.sourceFiles() > 0 && statistics.testSourceRatio() >= 0.40);
    }
}
