package no.uio.aeroscript.ast.stmt.action.turnAction;

import no.uio.aeroscript.ast.expr.NumberExpression;
import no.uio.aeroscript.runtime.DroneState;

public class TurnActionLeft extends BaseTurnAction {

    public TurnActionLeft(DroneState droneState,NumberExpression numberDegreesExpression, NumberExpression numberSpeedExpression, NumberExpression numberDurationExpression) {
        super(droneState, numberDegreesExpression, numberSpeedExpression, numberDurationExpression);
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