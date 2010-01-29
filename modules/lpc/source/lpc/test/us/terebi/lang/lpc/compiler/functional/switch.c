/* vim: set ft=lpc : */

public int a1()
{
    int i = 1;
    switch(i)
    {
        case 0: return 0;
        case 1: return 1;
        case 2: return 2;
    }
    return 3;
}

public int a2()
{
    int i = 5;
    switch(i)
    {
        case 0..1: return 0;
        case 2..4: return 1;
        case 5..6: return 2;
        default: return 3;                
    }
}

public int a3()
{
    int i = 15;
    switch( i % 10 )
    {
        case 0: return 0;
        case 1: return 1;
        case 2: return 2;
        default: return 3;                
    }
}

