/* vim: set ft=lpc : */

inherit "base1";

public int func2() 
{
    return default1() + public1() ;
}

public int var1()
{
    return strlen(public_var) > 0;
}

public int expand5()
{
    int array args = ({ 2, 4, -1 });
    return multiple_arguments( args... );
}

public int state9()
{
    int a, b;
    set_int(12);
    a = get_int();
    set_int(-3);
    b = get_int();
    return a+b;
}

protected string callback() { return "inherit"; }

public string str_inherit() { return call_callback() ; }
