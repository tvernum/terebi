/* vim: set ft=lpc : */
int test( )
{
    switch( 1 )
    {
        case 0:
            return 'a';
        case 1:
            return 'b';
        case 2..4:
            return 'c';
        case 5:
        case 6:
        case 7..8:
            return 'd';
        default:
            return 'e';
    }
}

