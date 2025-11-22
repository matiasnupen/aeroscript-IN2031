package no.uio.aeroscript.ast.expr;

import no.uio.aeroscript.type.Type;

public abstract class Expression {
    public abstract void print();
    public abstract Object evaluate();
    
    public abstract Type typeCheck();
}
