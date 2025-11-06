package no.uio.aeroscript.runtime.exceptions;

public class TypeError extends RuntimeException {
    public TypeError(String message) {
        super("TypeError: " + message);
    }
}