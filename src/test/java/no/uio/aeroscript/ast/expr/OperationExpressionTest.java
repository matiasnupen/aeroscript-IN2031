package no.uio.aeroscript.ast.expr;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import no.uio.aeroscript.type.impl.Point;

import static org.junit.jupiter.api.Assertions.*;

class OperationExpressionTest {
    Expression left = new NumberExpression(2.0f);
    Expression right = new NumberExpression(3.0f);

    @Nested
    class BasicArithmeticTests {
        @Test
        void testAddition() {
            Expression node = new OperationExpression("PLUS", left, right);
            float result = (Float) node.evaluate();
            System.out.println("testAddition -> " + result);
            assertEquals(5.0f, result);
        }

        @Test
        void testSubtraction() {
            Expression node = new OperationExpression("MINUS", left, right);
            float result = (Float) node.evaluate();
            System.out.println("testSubtraction -> " + result);
            assertEquals(-1.0f, result);
        }

        @Test
        void testMultiplication() {
            Expression node = new OperationExpression("TIMES", left, right);
            float result = (Float) node.evaluate();
            System.out.println("testMultiplication -> " + result);
            assertEquals(6.0f, result);
        }

        @Test
        void testDivision() {
            Expression node = new OperationExpression("DIV", new NumberExpression(6.0f), right);
            float result = (Float) node.evaluate();
            System.out.println("testDivision -> " + result);
            assertEquals(2.0f, result);
        }

        @Test
        void testModulo() {
            Expression node = new OperationExpression("MOD", new NumberExpression(7.0f), right);
            float result = (Float) node.evaluate();
            System.out.println("testModulo -> " + result);
            assertEquals(1.0f, result);
        }
    }

    @Nested
    class ComplexArithmeticTests {
        @Test
        void testDecimalNumbers() {
            Expression decimalLeft = new NumberExpression(1.5f);
            Expression decimalRight = new NumberExpression(2.0f);
            
            Expression addNode = new OperationExpression("PLUS", decimalLeft, decimalRight);
            float addResult = (Float) addNode.evaluate();
            System.out.println("testDecimalAddition -> " + addResult);
            assertEquals(3.5f, addResult);
            
            Expression mulNode = new OperationExpression("TIMES", decimalLeft, new NumberExpression(1.5f));
            float mulResult = (Float) mulNode.evaluate();
            System.out.println("testDecimalMultiplication -> " + mulResult);
            assertEquals(2.25f, mulResult, 0.001f);
        }

        @Test
        void testNegation() {
            Expression negNode1 = new OperationExpression("NEG", new NumberExpression(1.0f), null);
            float result1 = (Float) negNode1.evaluate();
            System.out.println("testNegation1 -> " + result1);
            assertEquals(-1.0f, result1);
            
            Expression negNode2 = new OperationExpression("NEG", left, null);
            float result2 = (Float) negNode2.evaluate();
            System.out.println("testNegation2 -> " + result2);
            assertEquals(-2.0f, result2);
        }
    }

    @Nested
    class SpecialOperationTests {
        @Test
        void testRandomRange() {
            Expression node = new OperationExpression("RANDOM", left, right);
            float value = (Float) node.evaluate();
            System.out.println("testRandomRange -> " + value);
            assertTrue(value >= 2.0f && value <= 3.0f, "Random value should be between 2.0 and 3.0");
        }

        @Test
        void testPointCreation() {
            Expression node = new OperationExpression("POINT", left, right);
            Point point = (Point) node.evaluate();
            System.out.println("testPointCreation -> " + point);
            assertEquals(2.0f, point.getX());
            assertEquals(3.0f, point.getY());
        }
    }

    @Nested
    class EdgeCaseTests {
        @Test
        void testDivisionByZero() {
            Expression node = new OperationExpression("DIV", left, new NumberExpression(0.0f));
            float result = (Float) node.evaluate();
            System.out.println("testDivisionByZero -> " + result);
            assertTrue(Float.isInfinite(result), "Division by zero should result in infinity");
        }

        @Test
        void testModuloByZero() {
            Expression node = new OperationExpression("MOD", left, new NumberExpression(0.0f));
            float result = (Float) node.evaluate();
            System.out.println("testModuloByZero -> " + result);
            // Modulo by zero returns NaN in Java, not an exception
            assertTrue(Float.isNaN(result), "Modulo by zero should return NaN");
        }

        @Test
        void testInvalidOperation() {
            assertThrows(IllegalArgumentException.class, () -> {
                Expression node = new OperationExpression("INVALID", left, right);
                Object result = node.evaluate();
                System.out.println("testInvalidOperation -> " + result);
            }, "Invalid operation should throw IllegalArgumentException");
        }

        @Test
        void testTypeMismatchWithPoint() {
            // Create a PointExpression to use as operand
            Expression pointOperand = new PointExpression(left, right);
            
            assertThrows(IllegalArgumentException.class, () -> {
                Expression invalidNode = new OperationExpression("PLUS", left, pointOperand);
                Object result = invalidNode.evaluate();
                System.out.println("testTypeMismatchWithPoint -> " + result);
            }, "Adding number to point should throw IllegalArgumentException");
        }

        @Test
        void testNegativeRandomRange() {
            Expression negLeft = new NumberExpression(-5.0f);
            Expression negRight = new NumberExpression(-2.0f);
            Expression node = new OperationExpression("RANDOM", negLeft, negRight);
            float value = (Float) node.evaluate();
            System.out.println("testNegativeRandomRange -> " + value);
            assertTrue(value >= -5.0f && value <= -2.0f, "Random value should be between -5.0 and -2.0");
        }

        @Test
        void testZeroOperations() {
            Expression zero = new NumberExpression(0.0f);
            
            Expression addNode = new OperationExpression("PLUS", left, zero);
            float addResult = (Float) addNode.evaluate();
            System.out.println("testZeroAddition -> " + addResult);
            assertEquals(2.0f, addResult);
            
            Expression mulNode = new OperationExpression("TIMES", left, zero);
            float mulResult = (Float) mulNode.evaluate();
            System.out.println("testZeroMultiplication -> " + mulResult);
            assertEquals(0.0f, mulResult);
        }

        @Test
        void testVeryLargeNumbers() {
            Expression largeLeft = new NumberExpression(Float.MAX_VALUE);
            Expression largeRight = new NumberExpression(2.0f);
            
            Expression mulNode = new OperationExpression("TIMES", largeLeft, largeRight);
            float result = (Float) mulNode.evaluate();
            System.out.println("testVeryLargeNumbers -> " + result);
            assertTrue(Float.isInfinite(result), "Multiplication of very large numbers should result in infinity");
        }
    }

    // Keep legacy test methods for compatibility
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