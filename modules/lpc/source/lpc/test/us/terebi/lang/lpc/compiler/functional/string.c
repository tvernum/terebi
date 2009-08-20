/* vim: set ft=lpc : */

public int a0()
{
    string s = "";
    return sizeof(s);
}

public int b0()
{
    string s = "";
    return strlen(s);
}

public int a1()
{
    string s = "x";
    return sizeof(s);
}

public int b1()
{
    string s = "x";
    return strlen(s);
}

public int a5()
{
    string s = "xyzzy";
    return sizeof(s);
}

public int b5()
{
    string s = "xyzzy";
    return strlen(s);
}

public string str0_XyzZy()
{
    return capitalize("xyzZy");
}

public int x3()
{
    return sizeof( explode("Terebi LPC Driver", " ") );
}

public string str_LPC()
{
    return explode("Terebi LPC Driver", " ")[1];
}

//  explode( "///AAA//BB/CCC///DDD/EE/", "/" ) => ({ "", "", "AAA", "", "BB", "CCC", "", "", "DDD", "EE" })
public int x10() { return sizeof(explode("///AAA//BB/CCC///DDD/EE/", "/")); }
public string str1_() { return explode("///AAA//BB/CCC///DDD/EE/", "/")[0]; }
public string str1_EE() { return explode("///AAA//BB/CCC///DDD/EE/", "/")[<1]; }

// explode("/A/B/C/////", "/") => ({ "A", "B", "C", "", "", "", "" })
public int x7() { return sizeof(explode("/A/B/C/////", "/")); }
public string str2_A() { return explode("/A/B/C/////", "/")[0]; }
public string str2_() { return explode("/A/B/C/////", "/")[<1]; }

// explode(":::::A:BB::CC:::DDDDD::::EEEE", "::") => ({ "", ":A:BB", "CC", ":DDDDD", "", "EEEE" })
public int x6() { return sizeof(explode(":::::A:BB::CC:::DDDDD::::EEEE", "::")); }
public string str3_() { return explode(":::::A:BB::CC:::DDDDD::::EEEE", "::")[0]; }
public array cmp3_0_1() { return ({ ":A:BB", explode(":::::A:BB::CC:::DDDDD::::EEEE", "::")[1] }) ;}
public string str3_EEEE() { return explode(":::::A:BB::CC:::DDDDD::::EEEE", "::")[<1]; }

// implode( ({ "" ,"", "","", "A", "B", "C", "", "D", "" }), "9" ) => "9999A9B9C99D9"
public string str_9999A9B9C99D9()
{
    return implode( ({ "" ,"", "","", "A", "B", "C", "", "D", "" }), "9" ) ;
}

// implode( ({ "A", "B", "C", "D" }), "9" ) => "A9B9C9D"
public string str_A9B9C9D()
{
    return implode( ({ "A", "B", "C", "D" }), "9" ) ;
}

// implode( ({ "A", "B", "C", "D" }), (: $1 + lower_case($2) :) ) => "Abcd"
public string str_Abcd()
{
    return implode( ({ "A", "B", "C", "D" }), (: $1 + lower_case($2) :) ) ;
}

public int i23()
{
    return implode( ({ 2, 4, 6, 8 }), (: $1 + $2 :) , 3 );
}

public string str_foo_bar0baz()
{
    return lower_case( "FOO_Bar0baz" );
}

public string str_frogs() { return pluralize("frog"); }
public string str_foxes() { return pluralize("fox"); }
public string str_data() { return pluralize("datum"); }
public string str_chefs() { return pluralize("chef"); }
public string str_zeros() { return pluralize("zero"); }
public string array eq01() { return ({ "swords of fate" , pluralize("sword of fate") }) ; }
public string array eq02() { return ({ "Green Lanterns" , pluralize("Green Lantern") }) ; }
public string array eq03() { return ({ "Dead Geese" , pluralize("Dead Goose") }) ; }

public string str_whywhywhy() { return repeat_string("why", 3); }

public string str_d339_sl339() { return replace_string("deep_sleep", "eep", "339") ; }
public string str_d33p_sl3ep() { return replace_string("deep_sleep", "e", "3", 3) ; }
public string str_piNG_poNG_siNG_soNG() { return replace_string("ping_pong_sing_song", "ng", "NG", 7 ); }
public string str_piNG_poNG_sing_song() { return replace_string("ping_pong_sing_song", "ng", "NG", 2 ); }

public string str_ping_poNG_siNG_soNG() { return replace_string("ping_pong_sing_song", "ng", "NG", 2,7 ); }
public string str_ping_poNG_siNG_song() { return replace_string("ping_pong_sing_song", "ng", "NG", 2,3 ); }
public string str_piNG_pong_sing_song() { return replace_string("ping_pong_sing_song", "ng", "NG", 0,1 ); }



