package com.fintech.inspection;

import java.util.ArrayList;
import java.util.List;

public final class TechnicalDebtVisitor extends AbstractProjectVisitor {
    public record Debt(String file, String reason, long minutes) {}

    private final List<Debt> debts = new ArrayList<>();

    @Override
    public void visit(SourceCodeFile file) {
        if (file.complexity() > 10) {
            debts.add(new Debt(file.name(), "Complexity above 10", (file.complexity() - 10L) * 30));
        }
        if (!file.documented()) {
            debts.add(new Debt(file.name(), "Missing documentation", 15));
        }
    }

    @Override
    public void visit(TestFile file) {
        if (file.disabledTests() > 0) {
            debts.add(new Debt(file.name(), "Disabled tests", file.disabledTests() * 10L));
        }
    }

    public List<Debt> debts() { return List.copyOf(debts); }
    public long totalMinutes() { return debts.stream().mapToLong(Debt::minutes).sum(); }
}
