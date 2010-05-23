/* vim: set ft=lpc : */

public int a2()
{
    catch( 1+1 );
    return 2;
}

public int a3()
{
    catch {  1+1 ; } ;
    return 3;
}

public int a4()
{
    int i;
    catch {  i = 2+2 ; } ;
    return i;
}

public int a5()
{
    string s = catch ( 1+1 );
    return 5;
}

public int a6()
{
    4 + strlen( catch( 2+2 ) + "%" + catch(5+5) ) ;
    return 6;
}

public int a0()
{
    object o;
    string 
    s = catch { o->foo(); } ;
    return !s;
}

public int a1()
{
    object o;
    string
    s = catch { o = this_object() ; } ;
    return !s;
}

public string array eq0()
{
    return ({ "*abcd", 
               catch( error("abcd" ) ) ,
            }) ;
}

public int complex2()
{
    int count = 0;
    object array obj = ({ 0, this_object(), 0 }) ;
    foreach (object o in obj)
    {
        if( catch(o->foo()) )
        {
            count++;
        }
    }
    return count;
}

public int b1()
{
    string err;
    object o;
    function f = (: err = catch( o->foo() ) :) ;
    (*f)();
    return !!err;
}

public int c1()
{
    string err;
    function f = (: err = catch( $1->foo() ) :) ;
    (*f)(0);
    return !!err;
}

