/* vim: set ft=lpc : */

private int global_int;
nosave string global_string;

public void func(int param_int, object param_object, string param_string)
{
    mixed local_mixed;
    int * local_int_array;
    function local_function;
    mapping local_mapping;
    float local_float_1 = 0.5 , local_float_2 = 2.5 , local_float_3 = 1.0;

    local_int_array = ({ param_int , global_int });
    local_mixed = param_string;
    local_mapping = ([ param_string : param_object , global_string : local_int_array ]);
    local_function = (: $1*local_float_1 + $2*local_float_2 + local_float_3 :);

    return;
}
