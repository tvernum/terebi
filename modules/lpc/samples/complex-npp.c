/*
 *    Block Comment 
 */

inherit "some/file/name";
inherit "another/file";

private int int_var;
private string str_var1 = str_var2 = "", str_var3 = "!", str_var4;
private static object obj_var;
private nosave mapping map_var = ([]) ;

object prototype();

int method1()
{
        some_method();
        some_method_with_args(1, "2");
        some_method_with_args( some_method(), obj_var );
}

int method2()
{
    int var = "some/object"->some_method(this_object()->some_value());
    if(var) return var;
    return -1;
}

int set_int_value(int i) {
    int_var = 1;
    return int_var ;
}

int set_obj_value(object o)
{
    return obj_var = o;
}

varargs int method2(object arg1, object arg2)
{
    object env = environment(this_object());

    if(arg1()->is_something())    return 1;
    else if(arg2->is_something()) return 2;
    else                          return 3;    
}

varargs int bit_twiddle(int a, int b, int c)
{
    if( (a & b) == c ) c = ~c;
    else if( (a | b) == c ) c = 0;
    else if( (a ^ b) == c ) c = 0xFFFFFFFF;

    return c;
}

private void private_method()
{
    int_var = 0;
    str_var1 = str_var2 = str_var3 = "";
    map_var = ([ "k1" : 1 , "k2" : 22,
                 "k3" : 333 ]);
}
    
protected void setup()
{
    name::setup();
    file::setup();
    bit_twiddle(1,2,3);
}

public mixed get_mixed_var()
{
    if( int_var == 0 )
    {
        return 0;
    }
    else if( str_var1 )
    {
        return str_var1;
    }
    return map_var;
}

public mixed get_map(mixed k)
{
    return map_var[1];
}

public function get_fun()
{
    return (: this_object(), get_mixed_var() :) ;
}

private int * get_array()
{
    int array ii;
    return ii;
}

// vim:set ft=lpc:
