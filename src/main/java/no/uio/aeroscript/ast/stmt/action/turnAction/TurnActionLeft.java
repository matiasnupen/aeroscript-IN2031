package no.uio.aeroscript.ast.stmt.action.turnAction;

import no.uio.aeroscript.ast.expr.NumberExpression;

public class TurnActionLeft extends BaseTurnAction {

    public TurnActionLeft(NumberExpression numberDegreesExpression, NumberExpression numberSpeedExpression) {
        super(numberDegreesExpression, numberSpeedExpression);
    }

    @Override
    protected Float calculateNewDirection(Float currentDirection, Float degrees) {
        return currentDirection - degrees; // Left turn subtracts degrees
    }

    @Override
    protected String getDirectionName() {
        return "Left";
    }
}