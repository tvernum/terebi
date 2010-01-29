/* vim: set ft=lpc : */

#include <roles.h>

public string mudlib_name()
{
    return "vision" ;
}

public int file_exists(string filename)
{
    return file_size(filename) >= 0;
}

public object locate_object(string name, object relative_to)
{
    int creator = interactive(relative_to) && relative_to->has_role(ROLE_CREATE);
    object result;
    
    if(creator)
    {
        result = find_object(name);
        if(result)
        {
            return result;
        }
    }

    // TODO When environments exist ...

    return result;
}
