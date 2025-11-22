package no.uio.aeroscript.type;

/**
 * Exception thrown when a type checking error occurs.
 * This is thrown during the type checking phase, before evaluation.
 */
public class TypeCheckException extends RuntimeException {
    
    public TypeCheckException(String message) {
        super("Type Check Error: " + message);
    }
    
    public TypeCheckException(String expected, String actual) {
        super(String.format("Type Check Error: Expected %s but got %s", expected, actual));
    }
    
    public TypeCheckException(Type expected, Type actual) {
        super(String.format("Type Check Error: Expected %s but got %s", expected, actual));
    }
}
