/* vim: set ft=lpc ts=4 sts=4 expandtab shiftwidth=4  : */

#include <input.h>

#define USER_SAVE SAVE "users/"
private string _user;
private string _password;
private string array _roles = ({ "user" });

private string save_file()
{
    return USER_SAVE + _user + __SAVE_EXTENSION__ ;
}

public void save()
{
    write("Saving ...");
    save_object( save_file() ) ;
    write("\n");
}

string prompt()
{
    write( "> " );
}

void new_password(string input, int count)
{
    if( count == 0 )
    {
        _password = input;
        write("Please enter that same password again to confirm: ");
        efun::input_to( "new_password", INPUT_NO_ESCAPE | INPUT_NO_ECHO, 1);
    }
    else if(input == _password)
    {
       save();
       prompt();
    }
    else
    {
        write("Those password didn't match. Please try again.\nPlease select a password: ");
        efun::input_to( "new_password", INPUT_NO_ESCAPE | INPUT_NO_ECHO, 0);
    }
}

void read_password(string input, int count)
{
    if( _password == input) 
    {
        prompt();
    }
    else
    {
        write("Incorrect password for " + _user + "\n");
        if(count >= 3)
        {
            destruct(this_object());
            return;
        }
        write("Password: ");
        efun::input_to( "read_password", INPUT_NO_ESCAPE | INPUT_NO_ECHO , count+1 );
    }
}

public void read_user(string input)
{
    if( strlen(input) == 0 )
    {
        write("Seriously, what is your name? ");
        efun::input_to( "read_user", INPUT_NO_ESCAPE );
    } 
    _user = input;
    write("Welcome " + _user + "\n");

    string save_file = save_file();
    if( file_exists(save_file) )
    {
        restore_object(save_file, 1);
        write("Password: ");
        efun::input_to( "read_password", INPUT_NO_ESCAPE | INPUT_NO_ECHO, 0 );
    }
    else
    {
        write("You seem to be new here.\nPlease select a password: ");
        efun::input_to( "new_password", INPUT_NO_ESCAPE | INPUT_NO_ECHO, 0);
    }
}

public void logon()
{
    write("What is your name? ");
    efun::input_to( "read_user", INPUT_NO_ESCAPE );
}

private object find_command(string cmd)
{
    foreach (string role in _roles)
    {
        string file = CMDS + role + "/" + cmd + ".c";
        if( file_exists( file ))
        {
            object o;
            string err = catch { o = load_object(file); } ;
            if(!err && o) 
            {
                return o;
            }
        }
    }
    return 0;
}

public void process_input(string input)
{
    string cmd = input;
    string args = "";
    sscanf(input, "%s %s", cmd, args);
    object cmd_object = find_command(cmd);
    if(cmd_object)
    {
        cmd_object->main(this_object(), cmd, args);
    }
    else
    {
        write("No such command '" + cmd + "'\n");
        prompt();
    }
}

public void receive_message(object type, object message)
{
    receive(  message );
}

public void catch_tell(string message)
{
    receive(  message );
}

public void grant_role(string role)
{
    _roles += ({ role }) ;
}

public void revoke_role(string role)
{
    _roles -= ({ role }) ;
}

