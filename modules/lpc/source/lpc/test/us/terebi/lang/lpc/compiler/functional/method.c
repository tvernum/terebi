/* vim: set ft=lpc : */

private void implemented_later(int a, int b);
private void not_implemented(string s);

public int a1()
{
    implemented_later(1,1);
    return 1;
}

private void implemented_later(int a, int b)
{
    return ;
}

public int a2()
{
    not_implemented("foo");
    return 2;
}
