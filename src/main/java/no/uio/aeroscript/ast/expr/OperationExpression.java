package no.uio.aeroscript.ast.expr;

public class OperationExpression extends Expression {
    private final String operation;
    private final Expression left;
    private final Expression right;

    public OperationExpression(String operation, Expression left, Expression right) {
        this.operation = operation;
        this.left = left;
        this.right = right;
    }

    @Override
    public void print(){
        System.out.print("OperationNode(");
        this.left.print();
        System.out.print(" " + this.operation + " ");
        this.right.print();
        System.out.print(")\n");
    }

    @Override
    public Object evaluate() {
        switch (operation) {
            case "NEG":
                NegNumberExpression negExpr = new NegNumberExpression(left);
                return negExpr.evaluate();

            case "POINT":
                PointExpression pointExpr = new PointExpression(left, right);
                return pointExpr.evaluate();

            case "RANDOM":
                RangeExpression rangeExpr = new RangeExpression(left, right);
                return rangeExpr.evaluate();
        }

        // Handle standard arithmetic operations
        Object val1 = left.evaluate();
        Object val2 = right.evaluate();
        if (val1 instanceof Float f1 && val2 instanceof Float f2) {
            return switch (operation) {
                // Keeping old names such as "PLUS", "MINUS", "TIMES" for matching with handed out tests.
                case "PLUS", "SUM" -> f1 + f2;
                case "MINUS", "SUB" -> f1 - f2;
                case "TIMES", "MUL" -> f1 * f2;
                case "DIV" -> f1 / f2;
                case "MOD" -> f1 % f2;
                default -> throw new IllegalArgumentException("Invalid operation: " + operation);
            };
        } else {
            throw new IllegalArgumentException("Invalid arguments to arithmetic operation");
        }
    }
}
