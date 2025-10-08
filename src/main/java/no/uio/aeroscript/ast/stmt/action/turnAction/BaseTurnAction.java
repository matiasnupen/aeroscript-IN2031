package no.uio.aeroscript.ast.stmt.action.turnAction;

import no.uio.aeroscript.ast.expr.NumberExpression;
import no.uio.aeroscript.ast.stmt.Statement;
import no.uio.aeroscript.runtime.DroneState;

public abstract class BaseTurnAction extends Statement {

    protected final NumberExpression numberDegreesExpression;
    protected final NumberExpression numberSpeedExpression;
    protected final NumberExpression numberDurationExpression;
    protected final DroneState droneState;

    public BaseTurnAction(DroneState droneState, NumberExpression numberDegreesExpression, NumberExpression numberSpeedExpression, NumberExpression numberDurationExpression) {
        this.droneState = droneState;
        this.numberDegreesExpression = numberDegreesExpression;
        this.numberSpeedExpression = numberSpeedExpression;
        this.numberDurationExpression = numberDurationExpression;
    }

    @Override
    public final void execute() {
        Object degreeValue = numberDegreesExpression.evaluate();
        Float degrees = Float.parseFloat(degreeValue.toString());
        Float currentPointingDirection = droneState.getCurrentPointingDirection();

        // subclasses define how to calculate new direction
        Float newPointingDirection = calculateNewDirection(currentPointingDirection, degrees);
        droneState.setCurrentPointingDirection(newPointingDirection);

        Float speed = numberSpeedExpression != null ? Float.parseFloat(numberSpeedExpression.evaluate().toString()) : 1.0f;
        Float time = numberDurationExpression != null ? Float.parseFloat(numberDurationExpression.evaluate().toString()) : degrees / speed;
        droneState.setBatteryLevel(droneState.getBatteryLevel() - (degrees * 0.3f + (time * 0.1f) + (speed * 1.0f)));
    }

    protected abstract Float calculateNewDirection(Float currentDirection, Float degrees);
    protected abstract String getDirectionName();

    @Override
    public String toString() {
        return "TurnAction" + getDirectionName() + '{' +
                "numberDegreesExpression=" + numberDegreesExpression.toString() +
                ", numberSpeedExpression=" + numberSpeedExpression.toString() +
                ", numberDurationExpression=" + numberDurationExpression.toString() +
                '}';
    }
}