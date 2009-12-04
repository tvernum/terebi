/* ------------------------------------------------------------------------
 * Copyright 2009 Tim Vernum
 * ------------------------------------------------------------------------
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ------------------------------------------------------------------------
 */

package us.terebi.lang.lpc.compiler.java.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.InternalName;
import us.terebi.lang.lpc.runtime.MemberDefinition;
import us.terebi.lang.lpc.runtime.MethodDefinition;
import us.terebi.lang.lpc.runtime.ObjectDefinition;
import us.terebi.lang.lpc.runtime.Callable.Kind;
import us.terebi.lang.lpc.runtime.MemberDefinition.Modifier;
import us.terebi.lang.lpc.runtime.jvm.context.Efuns;

public class FunctionLookup
{
    private final Logger LOG = Logger.getLogger(FunctionLookup.class);
    private final FunctionMap _efuns;
    private final FunctionMap _simul;
    private final FunctionMap _localMethods;
    private final Map<String, ObjectDefinition> _inherited;
    private final FunctionLookup _enclosing;
    private final String _enclosingPath;
    private final IdentityHashMap<FunctionSignature, String> _internalNames;

    public static class FunctionReference
    {
        public final Callable.Kind kind;
        public final String name;
        public final String internalName;
        public final FunctionSignature signature;
        public final VariableLookup.ObjectPath[] objectPath;

        private FunctionReference(Kind k, String n, String internal, FunctionSignature s, VariableLookup.ObjectPath[] path)
        {
            kind = k;
            name = n;
            internalName = internal;
            signature = s;
            objectPath = path;
        }

        public static FunctionReference efun(String name, FunctionSignature signature)
        {
            return reference(Callable.Kind.EFUN, name, name, signature);
        }

        private static FunctionReference reference(Kind kind, String name, String internalName, FunctionSignature signature)
        {
            if (signature == null)
            {
                return null;
            }
            return new FunctionReference(kind, name, internalName, signature, null);
        }

        public static FunctionReference simul(String name, FunctionSignature signature)
        {
            return reference(Callable.Kind.SIMUL_EFUN, name, name, signature);
        }

        public static FunctionReference local(String name, String internalName, FunctionSignature signature)
        {
            return reference(Callable.Kind.METHOD, name, internalName, signature);
        }

        public static FunctionReference inherited(MethodDefinition method, List<VariableLookup.ObjectPath> objectPath)
        {
            String internalName = method.getName();
            if (method instanceof InternalName)
            {
                internalName = ((InternalName) method).getInternalName();
            }
            VariableLookup.ObjectPath[] pathArray = objectPath.toArray(new VariableLookup.ObjectPath[objectPath.size()]);
            return new FunctionReference(Callable.Kind.METHOD, method.getName(), internalName, method.getSignature(), pathArray);
        }

        public static FunctionReference function(String expr)
        {
            return new FunctionReference(Callable.Kind.FUNCTION, expr, expr, GenericSignature.INSTANCE, null);
        }

        public static FunctionReference enclose(FunctionReference ref, ObjectDefinition enclosingObject, String enclosingPath)
        {
            VariableLookup.ObjectPath[] path = new VariableLookup.ObjectPath[1 + (ref.objectPath == null ? 0 : ref.objectPath.length)];
            path[0] = VariableLookup.ObjectPath.enclosing(enclosingPath, enclosingObject);
            if (path.length > 1)
            {
                System.arraycopy(ref.objectPath, 0, path, 1, ref.objectPath.length);
            }

            return new FunctionReference(ref.kind, ref.name, ref.internalName, ref.signature, path);
        }

        public String toString()
        {
            if (objectPath == null)
            {
                return name + "[" + signature + "]";
            }
            else
            {
                return Arrays.toString(objectPath) + "::" + name + "[" + signature + "]";
            }
        }

        public String getTypeName()
        {
            return kind.description();
        }

        public boolean isLocalMethod()
        {
            return (objectPath == null || objectPath.length == 0) && kind == Kind.METHOD;
        }
    }

    public FunctionLookup()
    {
        _efuns = new FunctionMap();
        _simul = new FunctionMap();
        _localMethods = new FunctionMap();
        _enclosing = null;
        _enclosingPath = null;
        _internalNames = new IdentityHashMap<FunctionSignature, String>();
        _inherited = new HashMap<String, ObjectDefinition>();
    }

    private FunctionLookup(FunctionLookup parent, String path)
    {
        _efuns = parent._efuns;
        _simul = parent._simul;
        _localMethods = new FunctionMap();
        _enclosing = parent;
        _enclosingPath = path;
        _internalNames = new IdentityHashMap<FunctionSignature, String>();
        _inherited = new HashMap<String, ObjectDefinition>();
    }

    public List<FunctionReference> findFunctions(String scope, String name, boolean includeSimulatedEfuns)
    {
        if (!isEfunLookup(scope))
        {
            List<FunctionReference> list = findMethod(scope, name);
            if (list != null)
            {
                return list;
            }
        }
        if (includeSimulatedEfuns)
        {
            FunctionSignature simul = _simul.get(name);
            if (simul != null)
            {
                return Collections.singletonList(FunctionReference.simul(name, simul));
            }
        }
        FunctionSignature efun = _efuns.get(name);
        if (efun != null)
        {
            return Collections.singletonList(FunctionReference.efun(name, efun));
        }
        if (isEfunLookup(scope) && LOG.isDebugEnabled())
        {
            LOG.debug("No such efun " + name + ". Valid efuns are:");
            for (String key : _efuns.keySet())
            {
                LOG.debug(" * " + key);
            }
        }
        return Collections.emptyList();
    }

    private boolean isEfunLookup(String scope)
    {
        return "efun".equals(scope);
    }

    private List<FunctionReference> findMethod(String scope, String name)
    {
        if (scope == null && _localMethods.containsKey(name))
        {
            FunctionSignature sig = _localMethods.get(name);
            String internalName = _internalNames.get(sig);
            return Collections.singletonList(FunctionReference.local(name, internalName, sig));
        }

        List<FunctionReference> references = new ArrayList<FunctionReference>();
        if (scope == null || "".equals(scope))
        {
            findFunctions(name, _inherited, references, new ArrayList<VariableLookup.ObjectPath>());
            if (!references.isEmpty())
            {
                return references;
            }
        }
        else
        {
            List<VariableLookup.ObjectPath> path = getInheritedObject(_inherited, scope);
            if (!path.isEmpty())
            {
                FunctionReference ref = findFunctionReference(name, path.get(path.size() - 1).definition, path);
                if (ref != null)
                {
                    return Collections.singletonList(ref);
                }
            }
            return null;
        }

        if (_enclosing != null)
        {
            List<FunctionReference> enclosing = _enclosing.findMethod(scope, name);
            if (enclosing != null)
            {
                List<FunctionReference> result = new ArrayList<FunctionReference>(enclosing.size());
                for (FunctionReference ref : enclosing)
                {
                    result.add(FunctionReference.enclose(ref, null, _enclosingPath));
                }
                return result;
            }
        }

        return null;
    }

    private List<VariableLookup.ObjectPath> getInheritedObject(Map<String, ? extends ObjectDefinition> inherited, String scope)
    {
        List<VariableLookup.ObjectPath> path = new ArrayList<VariableLookup.ObjectPath>();
        if (getInheritedObject(inherited, scope, path))
        {
            return path;
        }
        else
        {
            return Collections.emptyList();
        }
    }

    private boolean getInheritedObject(Map<String, ? extends ObjectDefinition> inherited, String scope, List<VariableLookup.ObjectPath> path)
    {
        ObjectDefinition object = inherited.get(scope);
        if (object != null)
        {
            path.add(VariableLookup.ObjectPath.inherit(scope, object));
            return true;
        }

        for (String key : inherited.keySet())
        {
            ObjectDefinition parent = inherited.get(key);
            path.add(VariableLookup.ObjectPath.inherit(key, parent));
            if (getInheritedObject(parent.getInheritedObjects(), scope, path))
            {
                return true;
            }
            else
            {
                path.remove(path.size() - 1);
            }
        }
        return false;
    }

    private void findFunctions(String functionName, Map<String, ? extends ObjectDefinition> inherited, List<FunctionReference> matches,
            List<VariableLookup.ObjectPath> path)
    {
        for (String parentName : inherited.keySet())
        {
            ObjectDefinition parent = inherited.get(parentName);
            path.add(VariableLookup.ObjectPath.inherit(parentName, parent));
            FunctionReference func = findFunctionReference(functionName, parent, path);
            if (func != null)
            {
                matches.add(func);
            }
            findFunctions(functionName, parent.getInheritedObjects(), matches, path);
            path.remove(path.size() - 1);
        }
    }

    private FunctionReference findFunctionReference(String name, ObjectDefinition object, List<VariableLookup.ObjectPath> objectPath)
    {
        MethodDefinition method = object.getMethods().get(name);
        if (isCallableMethod(method))
        {
            return FunctionReference.inherited(method, objectPath);
        }
        return null;
    }

    private boolean isCallableMethod(MethodDefinition method)
    {
        if (method == null)
        {
            return false;
        }
        Set< ? extends Modifier> modifiers = method.getModifiers();
        if (modifiers == null)
        {
            throw new NullPointerException("Method " + method + " has 'null' modifiers");
        }
        if (modifiers.contains(MemberDefinition.Modifier.PRIVATE))
        {
            return false;
        }
        return true;
    }

    public void addEfuns(FunctionMap efuns)
    {
        _efuns.putAll(efuns);
    }

    public void addEfuns(Efuns efuns)
    {
        _efuns.putAll(efuns.getSignatures());
    }

    public void addSimulEfuns(Iterable< ? extends MethodDefinition> methods)
    {
        for (MethodDefinition method : methods)
        {
            _simul.put(method.getName(), method.getSignature());
        }
    }

    public void addInherit(String name, ObjectDefinition parent)
    {
        _inherited.put(name, parent);
    }

    public void defineLocalMethod(String publicName, String internalName, FunctionSignature signature)
    {
        _localMethods.put(publicName, signature);
        _internalNames.put(signature, internalName);
    }

    public static FunctionLookup enclosing(FunctionLookup parent, String name)
    {
        FunctionLookup lookup = new FunctionLookup(parent, name);
        return lookup;
    }

}
