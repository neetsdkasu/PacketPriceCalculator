package neetsdkasu.util;

import java.util.NoSuchElementException;

public final class OptionalDouble
{
    private static final OptionalDouble EMPTY = new OptionalDouble(0.0, false);
    private final double value;
    private final boolean present;
    
    public static OptionalDouble empty()
    {
        return EMPTY;
    }
    
    public static OptionalDouble of(double value)
    {
        return new OptionalDouble(value, true);
    }
    
    private OptionalDouble(double value, boolean present)
    {
        this.value = value;
        this.present = present;
    }
    
    public boolean isPresent()
    {
        return present;
    }
    
    public double getAsLong()
    {
        if (present)
        {
            return value;
        }
        else
        {
            throw new NoSuchElementException();
        }
    }
    
    public double orElse(double other)
    {
        return present ? value : other;
    }
}