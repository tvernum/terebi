int mm()
{
while( args[0] == 1 )
    {
        switch(args[1]) 
        {
            case 'd': scan |= OPT_D; break;
            case 'e': scan |= OPT_E; break;
            case 'i': scan |= OPT_I; break;
            case 'f': scan |= OPT_F; break;
        }
    }
}

int method()
{
    int x = 7;
    switch(x)
    {
        case 1: return 10;
        case 2: return 20;
        case 3..5: return 30;
        case 6: return 40;
        default: return 50;
    }
}

