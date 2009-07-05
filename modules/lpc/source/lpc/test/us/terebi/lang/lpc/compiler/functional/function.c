/* vim: set ft=lpc : */

public int a1()
{
    function f = (: 1 :) ;
    return (*f)();
}

public int a2()
{
    function f = (: 1+1 :) ;
    return (*f)();
}

public int a3()
{
    int i = 4;
    function f = (: i :) ;
    i = 3;
    return (*f)();
}

public int a4()
{
    int i = 4;
    function f = (: $(i) :) ;
    i = 3;
    return (*f)();
}

public int a5()
{
    function f = (: $1+$2 :) ;
    return (*f)(2,3);
}

public int a6()
{
    function f = (: $2+$2 :) ;
    return (*f)(2,3);
}

public int a7()
{
    function f = (: $1 > 0 ? $2 : $3 :) ;
    return (*f)(1,7,8);
}

public int a8()
{
    function f = (: $1 > 0 ? $2 : $3 :) ;
    return (*f)(-1,7,8);
}

public int a9()
{
    function f = function(int base, int exp) 
    {
        int pow = base;
        for(int i=1; i<exp; i++)
        {
            pow *=base ;
        }
        return pow;
    };
    return (*f)(3,2);
}

private function f1()
{
    int i = 7;
    function f = (: i :);
    i = 10;
    return f;
}

public int a10()
{
    function f = f1();
    return (*f)();
}

private function array f2()
{
    int i = 0;
    function f1 = (: i :);
    function f2 = (: i = $1 :);
    return ({ f1, f2 });
}

public int a11()
{
    function array f = f2();
    function f_get = f[0];
    function f_set = f[1];
    (*f_set)( 11 );
    return (*f_get)();
}

public int a12()
{
    function f = (: $1 * $2 :) ;
    return evaluate(f, 3, 4);
}

public int a13()
{
    function f = (: 13 :);
    return evaluate(f);
}

