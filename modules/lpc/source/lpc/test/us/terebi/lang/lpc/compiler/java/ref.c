/* vim: set ft=lpc : */
private mixed fuzz( string ref s )
{
    s = "!";
    return s;
}

public mixed hoozit( string s , object o )
{
    fuzz( ref s );
    return s + o ;
}

