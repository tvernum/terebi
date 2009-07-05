/* vim: set ft=lpc : */

public int a9()
{
    int a = 0;
    for(int i=0; i<9; i++)
    {
        a++;
    }
    return a;
}

public int a512()
{
    int a = 1;
    for(int i=0; i<9; i++)
    {
        a = a * 2;
    }
    // 2,4,8
    // 16,32,64
    // 128,256,512
    return a;
}

public int a0()
{
    int a = 0;
    for(;0;) 
    {
        a = a * 2;
    }
    return a;
}

public int b512()
{
    int a = 1;
    while( a < 500 )
    {
        a = a * 2;
    }
    return a;
}

public int c512()
{
    int a = 1;
    do
    {
        a = a * 2;
    } while( a < 500 );

    return a;
}


public int a1()
{
    int a = 1;
    while( a > 500 )
    {
        a = a * 2;
    }
    return a;
}

public int a2()
{
    int a = 1;
    do
    {
        a = a * 2;
    } while( a > 500 );

    return a;
}

public int a39()
{
    int a = 0;
    int * arr = ({ 5, 7, 13, 1, 4, 9 });
    // 5,12,25,26,30,39
    foreach ( int e in arr )
    {
        a += e;
    }
    return a;
}

public int b1()
{
    int i = 1;
    while( i == 1 )
    {
        if( i == 1) break;
        i++;
    }
    return i;
}

public int b2()
{
    int i = 1;
    while( i == 1 )
    {
        i++;
        break;
    }
    return i;
}

public int c2()
{
    int i = 1;
    while( i == 1 )
    {
        i++;
        continue;
    }
    return i;
}

public int d2()
{
    int i = 1;
    while( i < 10 )
    {
        i++;
        break;
    }
    return i;
}

public int a10()
{
    int i = 1;
    while( i < 10 )
    {
        i++;
        continue;
    }
    return i;
}

public int b0()
{
    int a = 0;
    for(int i=0; i<9; i++)
    {
        if( 1 ) break;
        a++;
    }
    return a;
}

public string str_xyzzy()
{
    string str = "";
    string * arr = ({ "x", "yz", "z" , "y" });
    foreach ( string e in arr )
    {
        str += e;
    }
    return str;
}


