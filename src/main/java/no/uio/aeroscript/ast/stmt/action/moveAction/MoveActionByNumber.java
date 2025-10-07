package no.uio.aeroscript.ast.stmt.action.moveAction;

import no.uio.aeroscript.ast.expr.NumberExpression;
import no.uio.aeroscript.ast.stmt.Statement;
import no.uio.aeroscript.runtime.DroneState;

public class MoveActionByNumber extends Statement {

    private final NumberExpression numberExpression;

    public MoveActionByNumber(NumberExpression numberExpression) {
        this.numberExpression = numberExpression;
    }

    @Override
    public void execute() { // TODO: fix comments and javadoc
        // Evaluate the number expression to get the distance to move
        Object distanceValue = numberExpression.evaluate();
        float distance = Float.parseFloat(distanceValue.toString());

        // Get the drone state and move forward by the specified distance
        DroneState drone = DroneState.getInstance();
        drone.moveForward(distance);
    }

    @Override
    public String toString() {
        return "MoveActionByNumber: " + numberExpression.toString();
    }
}
