/* vim: set ft=lpc : */

public string str_abc()
{
    string s,t;
    sscanf( "abc def", "%s %s", s, t);
    return s;
}

public string str_def()
{
    string s,t;
    sscanf( "abc def", "%s %s", s, t);
    return t;
}

public int a70()
{
    mixed s,t;
    sscanf( "result=score(70)", "result=%s(%d)", s, t);
    return t;
}

public string str_xyzzy()
{
    string a,b,c,d;
    sscanf("result='foo';result='bar';result='xyzzy';result='terebi'" ,
           "result='%s';result='%s';result='%s';result='%s'" ,
           a,b,c,d);
    return c;
}

