package no.uio.aeroscript.ast.stmt.action.turnAction;

import no.uio.aeroscript.ast.expr.NumberExpression;

public class TurnActionRight extends BaseTurnAction {

    public TurnActionRight(NumberExpression numberDegreesExpression, NumberExpression numberSpeedExpression) {
        super(numberDegreesExpression, numberSpeedExpression);
    }

    @Override
    protected Float calculateNewDirection(Float currentDirection, Float degrees) {
        return currentDirection + degrees; // Right turn adds degrees
    }

    @Override
    protected String getDirectionName() {
        return "Right";
    }
}