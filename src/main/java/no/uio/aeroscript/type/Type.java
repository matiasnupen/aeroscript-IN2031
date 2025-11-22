package no.uio.aeroscript.type;

/**
 * Represents the types in the AeroScript language.
 * Used for type checking expressions before evaluation.
 */
public enum Type {
    FLOAT,      // Numeric values (e.g., 5.0, 10)
    POINT,      // Point type (e.g., point(5, 10))
    RANGE,      // Range type (e.g., random[0, 100])
    VOID,       // No return value (for actions/statements)
    ERROR;      // Type error (for error handling)

    @Override
    public String toString() {
        return switch (this) {
            case FLOAT -> "Float";
            case POINT -> "Point";
            case RANGE -> "Range";
            case VOID -> "Void";
            case ERROR -> "Error";
        };
    }

    /**
     * Checks if this type is compatible with another type.
     * Used for assignment and operation compatibility.
     */
    public boolean isCompatibleWith(Type other) {
        if (this == ERROR || other == ERROR) {
            return false;
        }
        return this == other;
    }
}
