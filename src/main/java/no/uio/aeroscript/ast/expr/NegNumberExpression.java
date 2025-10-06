package no.uio.aeroscript.ast.expr;

public class NegNumberExpression extends Expression {
    // Implement the NumberNode class to handle numbers

    /** The NegNumberExpression is supposed to handle unary negation of numbers
     * for example: -5 or -(3 + 2)
     * It should evaluate to the negative of the number it contains
     * It should only accept expressions that evaluate to numbers
     * If the expression does not evaluate to a number, it should throw an exception
     * If the expression has a left-value, that shoud be ignored (null-value)
     */
    private final Expression val;
    public NegNumberExpression(Expression val) { this.val = val;}

    @Override
    public void print(){
        System.out.print("NegNumberNode(");
        val.print();
        System.out.print(")\n");
    }

    @Override
    public Float evaluate(){
        Object tmp = this.val.evaluate();
        if (tmp instanceof Float floatValue) {
            return floatValue * -1 ;
        } else {
            throw new IllegalArgumentException("Wanted a Number, got a Node " + tmp);
        }
    }
}
