/* vim: set ft=lpc : */

private int sum(int array v...)
{
    int sum = 0;
    foreach ( int e in v )
    {
        sum += e;
    }
    return sum;
}

public int a1()
{
    return sum( 1 );
}

public int a2()
{
    return sum( 1, 0, 1);
}

public int a3()
{
    return sum( 5, 4, 1, -7);
}

