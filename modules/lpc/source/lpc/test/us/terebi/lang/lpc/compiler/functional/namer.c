/* vim: set ft=lpc : */

inherit "namer_base";

public int a42() { return hashCode(); }
public int b42() { return this_object()->hashCode(); }
public int c42() { return init()["hashCode"]; }
public int d42() { return this_object()->init()["hashCode"]; }

public string str1_XYZZY() { return toString() ; }
public string str2_XYZZY() { return this_object()->toString() ; }
public string str3_XYZZY() { return init()["toString"] ; }
public string str4_XYZZY() { return this_object()->init()["toString"]; }

