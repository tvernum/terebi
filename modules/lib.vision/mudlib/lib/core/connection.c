/* vim: set ft=lpc ts=4 sts=4 expandtab shiftwidth=4  : */

#include <input.h>

#define USER_SAVE SAVE "users/"
private string _user;
private string _password;

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
    }
    else
    {
        write("Those password didn't match. Please try again.\nPlease select a password: ");
        efun::input_to( "new_password", INPUT_NO_ESCAPE | INPUT_NO_ECHO, 0);
    }
}

void read_password(string input)
{
    _password = input;
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
        efun::input_to( "read_password", INPUT_NO_ESCAPE | INPUT_NO_ECHO );
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

public void process_input(string input)
{
    write("[" + _user + "/" + _password + "] Input: " + input + "\n");
}

