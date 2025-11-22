package no.uio.aeroscript.ast.expr;

import no.uio.aeroscript.type.interfaces.PointInterface;
import no.uio.aeroscript.type.Type;
import no.uio.aeroscript.type.TypeFactory;

public class PointExpression extends Expression {
    private final Expression left;
    private final Expression right;

    public PointExpression(Expression l, Expression r){
        left = l;
        right = r;
    }

    @Override
    public void print(){
        System.out.print("PointNode(");
        this.left.print();
        System.out.print(", ");
        this.right.print();
        System.out.print(")\n");
    }

    @Override
    public PointInterface evaluate(){
        Object lval = this.left.evaluate();
        Object rval = this.right.evaluate();
        if (lval instanceof Float f1 && rval instanceof Float f2) {
            return TypeFactory.createPoint(f1,f2);
        }
        else throw new IllegalArgumentException("Point coordinates must be numbers, got: " + lval + ", " + rval);
    }

    @Override
    public Type typeCheck() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'typeCheck'");
    }
}
