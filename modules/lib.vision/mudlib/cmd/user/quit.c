/* vim: set ft=lpc ts=4 sts=4 expandtab shiftwidth=4  : */

#include <message.h>

public void main(object connection, string cmd, string args)
{
    connection->save();
    connection->receive_message(MSG_SYSTEM, "Quiting\n");
    destruct(connection);
}

