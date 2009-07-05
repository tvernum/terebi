/* vim: set ft=lpc : */

public int a1()
{
    return 1 * 1 ;
}

public int a2()
{
    return 1 + 1 ;
}

public int b1()
{
    return 1 / 1 ;
}

public int b2()
{
    return 23 % 7 ;
}

public int a0()
{
    return 5 - 5 ;
}

public int a56()
{
    /* 2 * 4 * 7 => 56 */
    return ( 1 + 1 ) * ( 2 + 2 ) * ( 3 + 4 );
}

public int a11()
{
    /* 15 + 0 - 4 = 11 */
    return 3 * 5 + 2 * 0 - 1 * 4 ;
}

public float a0_4()
{
    /* ( 1.5 + 2.5 ) / 10 = 0.4 */
    return ( 1.5 * 1 + 5 / 2.0 ) / 10;
}

public float a5_0()
{
    float f = 1.1;
    f *= 20.0; // 22.0
    f -= 9.5; // 12.5
    f += 2.5; // 15.0
    f /= 3.0; // 5.0
    return f;
}

public float a0_625()
{
    float a = 10;
    while(a > 1)
    {
        a = a/2;
    }
    // 10, 5, 2.5, 1.25, 0.625 
    return a;
}

