/* vim: set ft=lpc : */

public string eq0()
{
    return ({
            "(  +42  )" ,
            sprintf("(%+|7d)" ,42)
        });
}

public string eq1()
{
    return ({
            "abcde",
            sprintf( "%-:5s", "abcdefgh")
        });
}

public string eq2()
{
    return ({
            "a    bc   def  ghij klmn pqrs ",
            sprintf("%-5.4@s", ({ "a", "bc", "def", "ghij", "klmno", "pqrstu" }) )
        });
}

public string eq3()
{
    return ({
            " . . . 30",
            sprintf("%' .'9x", 48 )
        });
}

public string eq4()
{
    return ({
            "[ 1 ] [abcde] [nopqr] [ 2 ]\n       fghij   stuvw\n        klm \n[ x ] [  10 ] [  y  ] [ z ]\n" ,
            sprintf("[%|3d] [%|=5s] [%|=5s] [%|3d]\n[%|3s] [%|5d] [%|5s] [%|3s]\n", 1, "abcdefghijklm", "nopqrstuvw", 2, "x", 10, "y", "z" )
        });
}

public string eq5()
{
    return ({
            "001.00" ,
            sprintf("%06.2f" , 1.0 )
        });
}

public string eq6()
{
    return ({
            "  12.34560/12.34560/     12.35/12.35" ,
             sprintf( "%10.5f/%5.5f/%10.2f/%5.2f" , 12.3456 , 12.3456 , 12.3456 , 12.3456 )
        });
}

