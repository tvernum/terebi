package us.terebi.lang.lpc.runtime.jvm.type;

import us.terebi.lang.lpc.runtime.ClassDefinition;

/**
 * 
 */
 final class ClassType extends AbstractType
{
    private final int _depth;
    private final ClassDefinition _definition;

     ClassType(int depth, ClassDefinition definition)
    {
        _depth = depth;
        _definition = definition;
    }

    public Kind getKind()
    {
        return Kind.CLASS;
    }

    public ClassDefinition getClassDefinition()
    {
        return _definition;
    }

    public int getArrayDepth()
    {
        return _depth;
    }
}