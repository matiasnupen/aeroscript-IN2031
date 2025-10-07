package no.uio.aeroscript.ast.stmt.action.moveAction;

import no.uio.aeroscript.ast.expr.PointExpression;
import no.uio.aeroscript.ast.stmt.Statement;
import no.uio.aeroscript.type.Point;

public class MoveActionByPoint extends Statement {

    private final PointExpression pointExpression;
    private final Point pointToMove;

    public MoveActionByPoint(PointExpression expression) {
        this.pointExpression = expression;
    }

    @Override
    public void execute() {

    }

    @Override
    public String toString() {
        return "MoveActionByPoint: " + pointExpression.toString();
        // TODO: implement toString method
    }

}
