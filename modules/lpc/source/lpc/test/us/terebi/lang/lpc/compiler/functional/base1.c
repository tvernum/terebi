/* vim: set ft=lpc : */

private string private_var = "private" ;
public string public_var = "public" ;
string default_var = "default" ;

private int private1() { return 1 ; }
private int private2() { return 2 ; }

public int public1() { return 1 ; }
public int public2() { return 2 ; }

protected int protected1() { return 1 ; }
protected int protected2() { return 2 ; }

int default1() { return 1 ; }
int default2() { return 2 ; }

nomask int nomask1() { return 1 ; }
nomask int nomask2() { return 2 ; }

private int private_int_var;
void set_int(int i) { private_int_var = i ; }
int get_int() { return private_int_var; }

protected /* pure_virtual */ string callback();

public string call_callback() { return callback() ; }
