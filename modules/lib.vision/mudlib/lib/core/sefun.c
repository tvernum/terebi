/* vim: set ft=lpc : */

public string mudlib_name()
{
    return "vision" ;
}

public int file_exists(string filename)
{
    return file_size(filename) >= 0;
}
