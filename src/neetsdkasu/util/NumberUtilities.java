package neetsdkasu.util;

import neetsdkasu.util.OptionalDouble;
import neetsdkasu.util.OptionalLong;

public final class NumberUtilities
{
    public static OptionalDouble tryParseDouble(String str)
    {
        try
        {
            return OptionalDouble.of(Double.parseDouble(str));
        }
        catch (NumberFormatException ex)
        {
            return OptionalDouble.empty();
        }
    }
    
    public static OptionalLong tryParseLong(String str)
    {
        try
        {
            return OptionalLong.of(Long.parseLong(str));
        }
        catch (NumberFormatException ex)
        {
            return OptionalLong.empty();
        }
    }
    
}