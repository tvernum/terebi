/* vim: set ft=lpc : */

inherit "base1";

int func2() 
{
    return default1() + public1() ;
}

int var1()
{
    return strlen(public_var) > 0;
}

int state9()
{
    int a, b;
    set_int(12);
    a = get_int();
    set_int(-3);
    b = get_int();
    return a+b;
}
