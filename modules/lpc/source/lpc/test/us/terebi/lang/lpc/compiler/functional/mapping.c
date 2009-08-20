/* vim: set ft=lpc : */

public int a0()
{
    mapping m = ([ ]);
    return sizeof(m);
}

public int a1()
{
    mapping m = ([ "a" : 1 ]);
    return sizeof(m);
}

public int a2()
{
    mapping m = ([ "a" : 1 ]);
    m["b"] = 2;
    return sizeof(m);
}

public int a3()
{
    mapping m = ([ "k" : 3 ]);
    return m["k"];
}

public int a4()
{
    mapping m = ([ "k" : 4 ]);
    return values(m)[0];
}

public int a5()
{
    mapping m = ([ 5 : "v" ]);
    return keys(m)[0];
}

public int a6()
{
    mapping m = ([ "x" : 3 ]);
    m["k"] = 6;
    return m["k"];
}

public int a7()
{
    mapping m = ([ "x" : 3 ]);
    m += ([ "k" : 7 ]);
    return m["k"];
}

public int b3()
{
    mapping m = ([ "x" : 3 , "y": 2, "z":1 ]);
    mixed x;
    m["x"] = x;
    return sizeof(m);
}

public int b2()
{
    mapping m = ([ "x" : 3 , "y": 2, "z":1 ]);
    map_delete(m, "x");
    return sizeof(m);
}

public int b0()
{
    mapping m = allocate_mapping(42);
    return sizeof(m);
}

public string str_abc123()
{
    function f = (: $1 + $2 :);
    mapping m = ([ "abc" : 123 , "xyz" : 789 ]);
    m = map_mapping(m, f);
    return m["abc"];
}

public string str_xyz789()
{
    function f = (: $1 + $2 :);
    mapping m = ([ "abc" : 123 , "xyz" : 789 ]);
    m = map(m, f);
    return m["xyz"];
}

public int b4()
{
    mapping m = ([ "aa":1 , "b":2 , "cc":3 , "d":4 , "e":5 , "f":6 , "g":7 ]);
    m = filter(m, (: strlen($1) == 2 || $2 >= 6 :) ) ;
    return sizeof(m);
}

public int b5()
{
    mapping m = ([ "aa":1 , "b":2 , "cc":3 , "d":4 , "e":5 , "f":6 , "g":7 ]);
    m = filter(m, (: strlen($1) == 2 || ($2 % 2) == 0 :) ) ;
    return sizeof(m);
}



