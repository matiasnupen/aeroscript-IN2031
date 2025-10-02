package no.uio.aeroscript.ast.expr;

import no.uio.aeroscript.type.Point;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OperationExpressionTest {
    Expression left = new NumberExpression(2.0f);
    Expression right = new NumberExpression(3.0f);

    @Test
    void evaluateSum() {
        Expression node = new OperationExpression("PLUS", left, right);
        assertEquals(5.0f, node.evaluate());
    }

    @Test
    void evaluateSub() {
        Expression node = new OperationExpression("MINUS", left, right);
        assertEquals(-1.0f, node.evaluate());
    }

    @Test
    void evaluateMul() {
        Expression node = new OperationExpression("TIMES", left, right);
        assertEquals(6.0f, node.evaluate());
    }

    @Test
    void evaluateNeg() {
        Expression node = new OperationExpression("NEG", left, right);
        assertEquals(2.0f * -1, node.evaluate());
    }

    @Test
    void evaluateRandom() {
        Expression node = new OperationExpression("RANDOM", left, right);
        float value = (Float) node.evaluate();
        assertTrue(value >= 2.0f && value <= 3.0f);
    }

    @Test
    void evaluatePoint() {
        Expression node = new OperationExpression("POINT", left, right);
        Point point = (Point) node.evaluate();
        assertEquals(2.0f, point.getX());
        assertEquals(3.0f, point.getY());
    }
}