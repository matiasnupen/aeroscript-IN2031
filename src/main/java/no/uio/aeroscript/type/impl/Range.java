package no.uio.aeroscript.type.impl;

import java.util.Random;
import no.uio.aeroscript.type.interfaces.RangeInterface;

public class Range implements RangeInterface {
    private final float start;
    private final float end;

    public Range(float start, float end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public Float getRandomNumber() {
        Random r = new Random();
        return r.nextFloat((this.end-this.start)) + this.start;
    }

    @Override
    public Float getStart() {
        return start;
    }

    @Override
    public Float getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return "[" + start + ", " + end + "]";
    }

}
