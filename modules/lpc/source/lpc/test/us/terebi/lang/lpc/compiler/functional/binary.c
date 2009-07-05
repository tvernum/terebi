/* vim: set ft=lpc : */

public int a1()
{
    return 1 | 1 ;
}

public int b1()
{
    return 1 & 1 ;
}

public int a0()
{
    return 1 ^ 1 ;
}

public int a3()
{
    return 1 | 2 ;
}

public int b0()
{
    return 1 & 2 ;
}

public int b3()
{
    return 1 ^ 2 ;
}

public int a255()
{
    int i = 0x0F;
    int j = 0xF0;
    return i|j;
}

public int a4095()
{
    int i = 0x00F;
    int j = 0x0F0;
    int k = 0xF00;
    return j|k|i;
}

public int a65535()
{
    int i = 0x00FF;
    int j = 0x0F0F;
    int k = 0xF00F;
    return j|k|i;
}

public int c0()
{
    int i = 0x0F;
    int j = 0xF0;
    return i&j;
}

public int d0()
{
    int i = 0x00F;
    int j = 0x0F0;
    int k = 0xF00;
    return j&k&i;
}

public int a15()
{
    int i = 0x00FF;
    int j = 0x0F0F;
    int k = 0xF00F;
    return j&k&i;
}

public int b255()
{
    int i = 0x0F;
    int j = 0xF0;
    return i^j;
}

public int b4095()
{
    int i = 0x00F;
    int j = 0x0F0;
    int k = 0xF00;
    return j^k^i;
}

public int b65535()
{
    int i = 0x00FF;
    int j = 0x0F0F;
    int k = 0xF00F;
    return j^k^i;
}

public int c1()
{
    return 1 << 0;
}

public int a2()
{
    return 1 << 1;
}

public int a4()
{
    return 1 << 2;
}

public int a12()
{
    return 3 << 2;
}

public int c3()
{
    return 12 >> 2;
}

public int c65535()
{
    return ~(0xffff0000);
}

public int e0()
{
    int i = 0x345fec;
    return ~(i ^ ~i);
}

/*
 * f = 1111 ~ 0000 = 0
 * e = 1110 ~ 0001 = 1
 * d = 1101 ~ 0010 = 2
 * c = 1100 ~ 0011 = 3
 * b = 1011 ~ 0100 = 4
 * a = 1010 ~ 0101 = 5
 * 9 = 1001 ~ 0110 = 6
 * 8 = 1000 ~ 0111 = 7
 */
public int d3()
{
    int i = 0x345edc;
    return ~(i ^ ( (~i) & 0xFFFFFFF0 ) );
    // ~ ( 0xFFFFFc )
    // = 3
}

