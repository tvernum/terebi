/* vim: set ft=lpc ts=4 sts=4 expandtab shiftwidth=4 showmatch : */

#include <message.h>

public void main(object connection, string cmd, string args)
{
    object o;
    string err = catch { o = load_object(args); } ;

    if( err )
    {
        connection->receive_message(MSG_ERROR, "Could not load '" + args + "' : " + err + "\n");
    }
    else if( o )
    {
        connection->receive_message(MSG_CREATE, "Loaded object '" + o + "'\n");
    }
    else
    {
        connection->receive_message(MSG_ERROR, "Could not load '" + args + "'\n");
    }
}

