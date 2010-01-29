/* vim: set ft=lpc ts=4 sts=4 expandtab shiftwidth=4 showmatch : */

#include <message.h>

public void main(object connection, string cmd, string args)
{
    if( !args || "" == args )
    {
        connection->receive_message(MSG_ERROR, "Usage '" + cmd + " <object-id>'.\n");
        return;
    }

    object o = locate_object(args, connection);
    if( o )
    {
        string msg = "Destructed '" + o + "'.\n";
        destruct(o);
        connection->receive_message(MSG_CREATE, msg);
    }
    else
    {
        connection->receive_message(MSG_ERROR, "No '" + args + "' can be found.\n");
    }
}

