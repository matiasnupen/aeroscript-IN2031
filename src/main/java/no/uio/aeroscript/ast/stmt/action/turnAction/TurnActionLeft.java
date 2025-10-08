package no.uio.aeroscript.ast.stmt.action.turnAction;

import no.uio.aeroscript.ast.expr.NumberExpression;
import no.uio.aeroscript.runtime.DroneState;

public class TurnActionLeft extends BaseTurnAction {

    public TurnActionLeft(DroneState droneState,NumberExpression numberDegreesExpression, NumberExpression numberSpeedExpression) {
        super(droneState, numberDegreesExpression, numberSpeedExpression);
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