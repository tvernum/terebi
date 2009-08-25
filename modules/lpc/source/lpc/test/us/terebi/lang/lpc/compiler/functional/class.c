/* vim: set ft=lpc : */

class Foo
{
    int i;
    int j;
}

public int a1()
{
    class Foo f = new(class Foo);
    f->i = 1;
    return f->i;
}

