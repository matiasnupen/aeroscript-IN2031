package no.uio.aeroscript.ast.stmt.action.turnAction;

import no.uio.aeroscript.ast.expr.NumberExpression;
import no.uio.aeroscript.runtime.DroneState;

public class TurnActionRight extends BaseTurnAction {

    public TurnActionRight(DroneState droneState,NumberExpression numberDegreesExpression, NumberExpression numberSpeedExpression) {
        super(droneState, numberDegreesExpression, numberSpeedExpression);
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