package no.uio.aeroscript.ast.stmt.action.turnAction;

import no.uio.aeroscript.ast.expr.NumberExpression;
import no.uio.aeroscript.ast.stmt.Statement;
import no.uio.aeroscript.runtime.DroneState;

public abstract class BaseTurnAction extends Statement {

    protected final NumberExpression numberDegreesExpression;
    protected final NumberExpression numberSpeedExpression;
    protected final DroneState droneState;

    public BaseTurnAction(DroneState droneState, NumberExpression numberDegreesExpression, NumberExpression numberSpeedExpression) {
        this.droneState = droneState;
        this.numberDegreesExpression = numberDegreesExpression;
        this.numberSpeedExpression = numberSpeedExpression != null ? numberSpeedExpression : new NumberExpression(1.0f);
    }

    @Override
    public final void execute() {
        Object degreeValue = numberDegreesExpression.evaluate();
        Float degrees = Float.parseFloat(degreeValue.toString());

        Float currentPointingDirection = droneState.getCurrentPointingDirection();

        // Template method - subclasses define how to calculate new direction
        Float newPointingDirection = calculateNewDirection(currentPointingDirection, degrees);
        droneState.setCurrentPointingDirection(newPointingDirection);

        // Common battery calculation logic
        Float time = degrees / numberSpeedExpression.evaluate();
        Float batteryCost = (degrees * 0.3f) + (time * 0.1f) + (numberSpeedExpression.evaluate() * 1.0f);
        Float currentBattery = droneState.getBatteryLevel();
        droneState.setBatteryLevel(currentBattery - batteryCost);
    }

    protected abstract Float calculateNewDirection(Float currentDirection, Float degrees);
    protected abstract String getDirectionName();

    @Override
    public String toString() {
        return "TurnAction" + getDirectionName() + ": " + numberDegreesExpression.toString() + 
               (numberSpeedExpression != null ? " at speed " + numberSpeedExpression.toString() : "");
    }
}