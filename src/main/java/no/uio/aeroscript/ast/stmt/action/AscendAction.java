package no.uio.aeroscript.ast.stmt.action;

import no.uio.aeroscript.ast.expr.NumberExpression;
import no.uio.aeroscript.ast.stmt.Statement;
import no.uio.aeroscript.runtime.DroneState;

public class AscendAction extends Statement {

    private final NumberExpression ascendingNumberExpression;
    private final NumberExpression ascendingSpeedExpression;
    private final NumberExpression ascendingDurationExpression;
    
    private final DroneState droneState;

    public AscendAction(DroneState droneState,NumberExpression ascendingNumberExpression, NumberExpression ascendingSpeedExpression, NumberExpression ascendingDurationExpression) {
        this.droneState = droneState;
        this.ascendingNumberExpression = ascendingNumberExpression;
        this.ascendingSpeedExpression = ascendingSpeedExpression;
        this.ascendingDurationExpression = ascendingDurationExpression;
    }

    @Override
    public void execute() {
        Object ascendingValue = ascendingNumberExpression.evaluate();
        Float ascending = Float.parseFloat(ascendingValue.toString());

        droneState.setCurrentAltitude(droneState.getCurrentAltitude() + ascending);
        
        Float time = ascendingDurationExpression != null ? Float.parseFloat(ascendingDurationExpression.evaluate().toString()) : null;

    }

    @Override
    public String toString() {
        return "AscendAction";
    }
}
