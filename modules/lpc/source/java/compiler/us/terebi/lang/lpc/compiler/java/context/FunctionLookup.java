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
    private final IdentityHashMap<FunctionSignature, String> _internalNames;

    public static class FunctionReference
    {
        public final Callable.Kind kind;
        public final String name;
        public final String internalName;
        public final FunctionSignature signature;
        public final ObjectDefinition object;
        public final String[] objectPath;

        private FunctionReference(Kind k, String n, String internal, FunctionSignature s, ObjectDefinition o, String[] path)
        {
            kind = k;
            name = n;
            internalName = internal;
            signature = s;
            object = o;
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
            return new FunctionReference(kind, name, internalName, signature, null, null);
        }

        public static FunctionReference simul(String name, FunctionSignature signature)
        {
            return reference(Callable.Kind.SIMUL_EFUN, name, name, signature);
        }

        public static FunctionReference local(String name, String internalName, FunctionSignature signature)
        {
            return reference(Callable.Kind.METHOD, name, internalName, signature);
        }

        public static FunctionReference inherited(MethodDefinition method, List<String> objectPath)
        {
            String internalName = method.getName();
            if (method instanceof InternalName)
            {
                internalName = ((InternalName) method).getInternalName();
            }
            return new FunctionReference(Callable.Kind.METHOD, method.getName(), internalName, method.getSignature(),
                    method.getDeclaringType(), objectPath.toArray(new String[objectPath.size()]));
        }

        public static FunctionReference function(String expr)
        {
            return new FunctionReference(Callable.Kind.FUNCTION, expr, expr, GenericSignature.INSTANCE, null, null);
        }

        public String toString()
        {
            if (object == null)
            {
                return name;
            }
            else
            {
                return object.getName() + "::" + name;
            }
        }

        public String getTypeName()
        {
            return kind.description();
        }

    }

    public FunctionLookup()
    {
        _efuns = new FunctionMap();
        _simul = new FunctionMap();
        _localMethods = new FunctionMap();
        _internalNames = new IdentityHashMap<FunctionSignature, String>();
        _inherited = new HashMap<String, ObjectDefinition>();
    }

    public List<FunctionReference> findFunctions(String scope, String name, boolean includeSimulatedEfuns)
    {
        if (!"efun".equals(scope))
        {
            if (scope == null)
            {
                if (_localMethods.containsKey(name))
                {
                    FunctionSignature sig = _localMethods.get(name);
                    String internalName = _internalNames.get(sig);
                    return Collections.singletonList(FunctionReference.local(name, internalName, sig));
                }
            }
            List<FunctionReference> references = new ArrayList<FunctionReference>();
            Map<String, ObjectDefinition> inherited = _inherited;
            if (scope == null || "".equals(scope))
            {
                findFunctions(name, inherited, references, new ArrayList<String>());
                if (!references.isEmpty())
                {
                    return references;
                }
            }
            else
            {
                ObjectDefinition object = getInheritedObject(inherited, scope);
                FunctionReference ref = findFunctionReference(name, object, Collections.singletonList(scope));
                if (ref != null)
                {
                    return Collections.singletonList(ref);
                }
                else
                {
                    return null;
                }
            }
        }
        if (includeSimulatedEfuns)
        {
            FunctionReference simul = FunctionReference.simul(name, _simul.get(name));
            if (simul != null)
            {
                return Collections.singletonList(simul);
            }
        }
        if (_efuns.containsKey(name))
        {
            return Collections.singletonList(FunctionReference.efun(name, _efuns.get(name)));
        }
        else
        {
            if ("efun".equals(scope) && LOG.isDebugEnabled())
            {
                LOG.debug("No such efun " + name + ". Valid efuns are:");
                for (String key : _efuns.keySet())
                {
                    LOG.debug(" * " + key);
                }
            }
            return Collections.emptyList();
        }
    }

    private ObjectDefinition getInheritedObject(Map<String, ? extends ObjectDefinition> inherited, String scope)
    {
        ObjectDefinition object = inherited.get(scope);
        if (object == null)
        {
            for (ObjectDefinition parent : inherited.values())
            {
                object = getInheritedObject(parent.getInheritedObjects(), scope);
                if (object != null)
                {
                    return object;
                }
            }
            return null;
        }
        else
        {
            return object;
        }
    }

    private void findFunctions(String name, Map<String, ? extends ObjectDefinition> map, List<FunctionReference> match,
            List<String> path)
    {
        for (String parentName : map.keySet())
        {
            ObjectDefinition parent = map.get(parentName);
            path.add(parentName);
            FunctionReference func = findFunctionReference(name, parent, path);
            if (func != null)
            {
                match.add(func);
            }
            findFunctions(name, parent.getInheritedObjects(), match, path);
            path.remove(path.size() - 1);
        }
    }

    private FunctionReference findFunctionReference(String name, ObjectDefinition object, List<String> objectPath)
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

}
