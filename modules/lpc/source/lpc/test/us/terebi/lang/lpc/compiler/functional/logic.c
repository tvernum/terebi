/* vim: set ft=lpc : */

public int a1()
{
    return 1 || 2 ;
}

public int a2()
{
    return 1 && 2 ;
}

public int a3()
{
    return ( 0 ? 1 : 3 ) ;
}

public int a4()
{
    return ( 5 ? 4 : 3 ) ;
}

public int a5()
{
    if( 0 )
    {
        return 4 ;
    }
    else
    {
        return 5 ;
    }
}

public int a6()
{
    if( this_object() == 0 )
    {
        return 5 ;
    }
    else
    {
        return 6 ;
    }
}

public int a7()
{
    if( this_object() != 0 )
    {
        return 7 ;
    }
    else
    {
        return 8 ;
    }
}

public int a8()
{
    int i = 2;
    int j = 1;
    int k = 0;

    if( i && j && k )
    {
        return 9;
    }
    return 8;
}

public int a9()
{
    int i = 2;
    int j = 1;
    int k = 0;

    if( i || j || k )
    {
        return 9;
    }
    return 8;
}

public int b0()
{
    return !this_object();
}

public int b1()
{
    return !!this_object();
}


