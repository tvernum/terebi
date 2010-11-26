/* vim: set ft=lpc : */

public int f1_int = 0;
protected string f2_str = "xyz";
private float f3_flt = 1.5;

public nosave object f4_obj = this_object();
protected nosave mixed array f5_arr = ({ 1 , 2.5 , "x" });  
private nosave mapping f6_map = ([ 1:f1_int, 2:f2_str, 3:f3_flt, 4:f4_obj, 5:f5_arr ]);

private mixed f7_mix = explode( file_name(f4_obj), "#" );

public int a0() { return f1_int; }

public string str1_xyz() { return f2_str; }

public float f1_5() { return f3_flt ; }

public int bool_1() { return f4_obj == this_object(); }
public int bool_0() { return f4_obj == 0; }
public string array eq1() { return ({ "/field", file_name(f4_obj)[0..5] }); }

public int a3() { return sizeof(f5_arr); }
public int a1() { return f5_arr[0]; }
public string array  eq2() { return ({ "3.5x" , f5_arr[0] + f5_arr[1] + f5_arr[2] }); }

public int b0()          { return f6_map[1] ; }
public string str3_xyz() { return f6_map[2] ; }
public float b1_5()      { return f6_map[3] ; }

public int c2() { return sizeof(f7_mix); }
public int c6() { return sizeof(f7_mix[0]); }
public int c1() { return to_int(f7_mix[1]) > 0; }

