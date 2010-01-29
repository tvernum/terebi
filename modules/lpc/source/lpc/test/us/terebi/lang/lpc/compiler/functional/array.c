/* vim: set ft=lpc : */

public int a0()
{
    array a = ({ });
    return sizeof(a);
}

public int a1()
{
    string array a = ({ "a" });
    return sizeof(a);
}

public int a2()
{
    int * a = ({ 1 });
    a = a + ({ 2 }) ;
    return sizeof(a);
}

public int a3()
{
    int array a = ({ 2, 3, 4 });
    return a[1];
}

public int a4()
{
    mixed * a = ({ 2, 3, 4 });
    return a[<1];
}

public int a5()
{
    array a = ({ 2,3,4,5,6 });
    return a[<2 .. <1][0];
}

public int a6()
{
    array a = ({ 2,3,4,5,6 });
    return a[<2 .. ][1];
}

public int a7()
{
    mixed array a = ({ 0, 0, 0, 0 });
    a[2] = 7;
    return a[10/(2+3)];
}

public int a8()
{
    mixed * a = ({ "z","o",2,3,"fr"}) ;
    a += ({ 5,"sx","sv",8,9 });
    return a[8];
}

public int a9()
{
    array a = allocate(9);
    return sizeof(a);
}

public int a10()
{
    string array a = allocate(15);
    for(int i=0; i<15; i++)
    {
        a[i] = "x-" + (i*2);
    }
    return member_array("x-20",a);
}

public int a11()
{
    string array a = allocate(15);
    for(int i=0; i<15; i++)
    {
        a[i] = "x-" + (i%10);
    }
    return member_array("x-1", a, 5);
}

public int a12()
{
    int array a = ({ 1, 2, 3, 4, 5 });
    a = map_array(a, (: $1 + $2 :), 10 );
    return a[1];
}

public int a13()
{
    int array a = ({ 1, 2, 3, 4, 5 });
    a = map(a, (: $1 + $2 :), 10 );
    return a[2];
}

public int str_aaaa()
{
    string array a = ({ "a", "aa", "aaa", "aaaa", "aaaaa", "aaaaaa", "b", "bb", "bbb", "bbbb", "bbbbb", "bbbbbb" });
    a = filter_array(a, (: strlen($1) >= $2 :), 3 );
    return a[1];
}

public int str_bbbb()
{
    string array a = ({ "a", "aa", "aaa", "aaaa", "aaaaa", "aaaaaa", "b", "bb", "bbb", "bbbb", "bbbbb", "bbbbbb" });
    a = filter(a, (: strlen($1) >= $2 :), 3 );
    return a[<3];
}

public int b0()
{
    int array a = ({ 5, 7, 7, 34, 45, 1, 2, 21, 7, 0, 2, 66, 73 });
    a = sort_array(a, 1);
    return a[0];
}

public int b1()
{
    int array a = ({ 5, 7, 7, 34, 45, 1, 2, 21, 7, 0, 2, 66, 73 });
    a = sort_array(a, -1);
    return a[<2];
}

public string str_cccc()
{
    string array a = ({ "aaaa", "zbzz", "cccc", "ayyy", "cxxx", "dddd", "daaa" });
    a = sort_array( a, (: $1[1] - $2[1] :) );
    // ({ aaaa / daaa , zbzz , cccc, dddd, cxxx, ayyy });
    return a[3];
}

public string str_dddd()
{
    string array a = ({ "aaaa", "zbzz", "cccc", "ayyy", "cxxx", "dddd", "daaa" });
    a = sort_array( a, (: $1[$3] - $2[$4] :), 1, 1 );
    // ({ aaaa / daaa , zbzz , cccc, dddd, cxxx, ayyy });
    return a[<3];
}

public int b2()
{
    mixed array a = ({  "1", "22", "333", "4444", ({ 1 }), ({ 1, 2 }), ({ 1, 2, 3}), ({1,2,3,4}) });
    a = unique_array( a, (: sizeof($1) :) ) ;
    return sizeof(a[0]);
}

public int b3()
{
    mixed array a = ({  "1", "22", "333", "4444", ({ 1 }), ({ 1, 2 }), ({ 1, 2, 3}), ({1,2,3,4}) });
    a = unique_array( a, (: sizeof($1) :), 4 ) ;
    return sizeof(a);
}

public int b4()
{
    mixed array a = ({  "1", "22", "333", "4444", ({ 1 }), ({ 1, 2 }), ({ 1, 2, 3}), ({1,2,3,4}) });
    a = unique_array( a, (: sizeof($1) :) ) ;
    return sizeof(a);
}

public b5()
{
    int array a = ({ 1, 2, 3, 4, 5, 6, 7, 8 });
    int array b = ({ -3 , -2, -1, 0, 1, 2, 3, 4, 5 });
    return sizeof( a & b );
}

public b6()
{
    int array a = ({ 1, 2, 3, 4 });
    int array b = ({ 3, 4, 5, 6 });
    return sizeof( a | b );
}


