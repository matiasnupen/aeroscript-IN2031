package no.uio.aeroscript.ast.expr;

import no.uio.aeroscript.type.Type;

public class NumberExpression extends Expression {
    // Implement the NumberNode class to handle numbers
    private final Float number;
    public NumberExpression(Float number){ this.number = number;}

    public void print(){
        System.out.print("NumberNode(" + number.toString() + ")\n");
    }

    @Override
    public Float evaluate(){
        return this.number;
    }

    @Override
    public Type typeCheck() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'typeCheck'");
    }
}

