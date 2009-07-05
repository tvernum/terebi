/* vim: set ft=lpc : */

int ii() {
    return 7;
}

private class huh {
    int i = ii();
    string s;
    function f;
}

class huh foo() {
    class huh huh = new(class huh);
    huh->i = 7;
    huh->s = "seven";
    huh->f = (: 7 :) ;
    return huh;
}

void test(class huh h)
{
    if( (*h->f)() != h->i )
    {
        write(h->s + " failed");
    }
}

