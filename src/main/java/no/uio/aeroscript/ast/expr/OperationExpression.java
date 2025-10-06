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

                // These are created to match the names in OperationExpressionTest
                case "PLUS" -> f1 + f2;
                case "MINUS" -> f1 - f2;
                case "TIMES" -> f1 * f2;

                // Previous variable names for arithmetic operations
                case "SUM" -> f1 + f2;
                case "SUB" -> f1 - f2;
                case "MUL" -> f1 * f2;
                case "DIV" -> f1 / f2;
                case "MOD" -> f1 % f2;
                default -> throw new IllegalArgumentException("Invalid operation: " + operation);
            };
        } else {
            throw new IllegalArgumentException("Invalid arguments to arithmetic operation");
        }
    }
}
