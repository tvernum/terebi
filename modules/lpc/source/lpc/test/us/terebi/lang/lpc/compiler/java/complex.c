/* vim: set ft=lpc : */

inherit "/std/lib/weapons/sword.c";

private int dwarf_kill;

public void create()
{
    ::create();
    set_name("shortsword");
    add_alias("dwarven shortsword");
    add_alias("dwarven");
    set_short("dwarven shortsword");
    set_long("This is a small iron sword. There are strange dwarven markings on the blade, but they are too faint to read");

    set_weight(7);

    set_wc(10);
    add_wc_bonus( (: ( $1->get_race() == "dwarf" ) ? 4 : -dwarf_kill :) );
}

protected target_killed(object target)
{
    if( target->get_race() == "dwarf" )
    {
        dwarf_kill++;
    }
}

