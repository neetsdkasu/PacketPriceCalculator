package neetsdkasu.util;

import java.util.NoSuchElementException;

public final class OptionalLong
{
    private static final OptionalLong EMPTY = new OptionalLong(0L, false);
    private final long value;
    private final boolean present;
    
    public static OptionalLong empty()
    {
        return EMPTY;
    }
    
    public static OptionalLong of(long value)
    {
        return new OptionalLong(value, true);
    }
    
    private OptionalLong(long value, boolean present)
    {
        this.value = value;
        this.present = present;
    }
    
    public boolean isPresent()
    {
        return present;
    }
    
    public long getAsLong()
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
    
    public long orElse(long other)
    {
        return present ? value : other;
    }
}