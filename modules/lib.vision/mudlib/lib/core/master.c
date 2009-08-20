/* vim: set ft=lpc : */

#define CONNECTION "/lib/core/connection.c"

public string array epilog()
{
    return ({
            CONNECTION
            });
}

public void preload(string file)
{
    load_object(file);
}

public object connect(int port)
{
    return new ( CONNECTION );
}

public int valid_write(string file, object obj, string func)
{
    return 1;
}

