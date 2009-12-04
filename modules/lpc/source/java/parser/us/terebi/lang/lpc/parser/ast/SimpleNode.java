package us.terebi.lang.lpc.parser.ast;

import java.util.ArrayList;
import java.util.List;

import us.terebi.lang.lpc.parser.jj.Parser;
import us.terebi.lang.lpc.parser.jj.Token;

public class SimpleNode implements TokenNode
{
    private Node _parent;
    private Node[] _children;
    private int _id;
    private Object _value;
    private Token _firstToken;
    private Token _lastToken;
    private List<List<? extends Token>> _pragmas;

    public SimpleNode(int id)
    {
        _id = id;
        _pragmas = new ArrayList<List<? extends Token>>(); 
    }

    public SimpleNode(@SuppressWarnings("unused")
    Parser parser, int id)
    {
        this(id);
    }

    public void jjtOpen()
    {
        // no-op
    }

    public void jjtClose()
    {
        // no-op
    }

    public void jjtSetParent(Node n)
    {
        _parent = n;
    }

    public Node jjtGetParent()
    {
        return _parent;
    }

    public void jjtAddChild(Node n, int i)
    {
        if (_children == null)
        {
            _children = new Node[i + 1];
        }
        else if (i >= _children.length)
        {
            Node c[] = new Node[i + 1];
            System.arraycopy(_children, 0, c, 0, _children.length);
            _children = c;
        }
        _children[i] = n;
    }

    public Node jjtGetChild(int i)
    {
        return _children[i];
    }

    public int jjtGetNumChildren()
    {
        return (_children == null) ? 0 : _children.length;
    }

    public void jjtSetValue(Object value)
    {
        this._value = value;
    }

    public Object jjtGetValue()
    {
        return _value;
    }

    public Token jjtGetFirstToken()
    {
        return _firstToken;
    }

    public void jjtSetFirstToken(Token token)
    {
        this._firstToken = token;
    }

    public Token jjtGetLastToken()
    {
        return _lastToken;
    }

    public void jjtSetLastToken(Token token)
    {
        this._lastToken = token;
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    /** Accept the visitor. **/
    public Object childrenAccept(ParserVisitor visitor, Object data)
    {
        if (_children != null)
        {
            for (int i = 0; i < _children.length; ++i)
            {
                _children[i].jjtAccept(visitor, data);
            }
        }
        return data;
    }

    /* You can override these two methods in subclasses of SimpleNode to
       customize the way the node appears when the tree is dumped.  If
       your output uses more than one line you should override
       toString(String), otherwise overriding toString() is probably all
       you need to do. */

    public String toString()
    {
        return ParserTreeConstants.jjtNodeName[_id];
    }

    public String toString(String prefix)
    {
        return prefix + toString();
    }

    /* Override this method if you want to customize how the node dumps
       out its children. */

    public void dump(String prefix)
    {
        System.out.println(toString(prefix));
        if (_children != null)
        {
            for (int i = 0; i < _children.length; ++i)
            {
                SimpleNode n = (SimpleNode) _children[i];
                if (n != null)
                {
                    n.dump(prefix + " ");
                }
            }
        }
    }

    public void addPragma(List<? extends Token> tokens)
    {
        _pragmas.add(tokens);
    }

    public Iterable<? extends List<? extends Token>> getPragmas()
    {
        return _pragmas;
    }

    protected void addChild(TokenNode child, int index)
    {
        this.jjtAddChild(child, index);
        child.jjtSetParent(this);
    }
}
