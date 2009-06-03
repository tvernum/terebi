/* ------------------------------------------------------------------------
 * $Id$
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

package us.terebi.lang.lpc.runtime.jvm;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import us.terebi.lang.lpc.compiler.java.context.FunctionMap;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.jvm.efun.AllocateEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.AllocateMappingEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.ArraypEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.BindEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.BufferpEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.CallOtherEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.CallOutEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.CallOutInfoEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.CallStackEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.CapitalizeEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.ClasspEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.CloneObjectEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.ClonepEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.CopyFileEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.CreateDirectoryEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.CryptEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.CtimeEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.DebugInfoEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.DebugMessageEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.DestructEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.EnvironmentEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.ErrorEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.EvaluateEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.ExecEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.ExplodeEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.FileNameEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.FileSizeEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.FilterEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.FindLivingEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.FindObjectEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.FindPlayerEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.FlushMessagesEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.FunctionExistsEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.FunctionpEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.FunctionsEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.GetDirectoryInfoEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.ImplodeEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.InheritListEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.InheritsEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.InteractiveEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.IntpEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.InventoryEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.KeysEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.LivingEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.LivingsEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.LoadObjectEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.LocaltimeEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.LowerCaseEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.MapEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.MappEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.MasterEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.MemberArrayEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.MessageEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.NoOpEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.NullpEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.ObjectpEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.ObjectsEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.PluralizeEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.PresentEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.PreviousObjectEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.QueryIpNameEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.QueryIpNumberEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.QueryPrivsEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.QuerySnoopEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.QuerySnoopingEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.RandomEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.ReadBytesEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.ReadFileEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.RegexpEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.RemoveFileEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.ReplaceStringEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.ResetEvalCostEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.SetEvalLimitEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.ShutdownEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.SizeofEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.SnoopEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.SortArrayEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.SprinfEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.SscanfEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.StringpEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.StrlenEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.StrsrchEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.TerminalColourEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.ThisObjectEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.ThisPlayerEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.TimeEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.ToFloatEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.ToIntEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.TypeofEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.UpperCaseEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.UserpEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.UsersEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.WriteEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.WriteFileEfun;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;

/**
 * 
 */
public class StandardEfuns
{
    public static class MISC
    {
        public static final FunctionSignature nullp = new NullpEfun();
        public static final FunctionSignature undefinedp = nullp;
        public static final FunctionSignature typeof = new TypeofEfun();
    }

    public static class STRING
    {
        public static final FunctionSignature stringp = new StringpEfun();
        public static final FunctionSignature implode = new ImplodeEfun();
        public static final FunctionSignature explode = new ExplodeEfun();
        public static final FunctionSignature strlen = new StrlenEfun();
        public static final FunctionSignature sscanf = new SscanfEfun();
        public static final FunctionSignature sprintf = new SprinfEfun();
        public static final FunctionSignature strsrch = new StrsrchEfun();
        public static final FunctionSignature replace_string = new ReplaceStringEfun();
        public static final FunctionSignature regexp = new RegexpEfun();
        public static final FunctionSignature lower_case = new LowerCaseEfun();
        public static final FunctionSignature upper_case = new UpperCaseEfun();
        public static final FunctionSignature capitalize = new CapitalizeEfun();
        public static final FunctionSignature pluralize = new PluralizeEfun();
    }

    public static class COLLECTION
    {
        public static final FunctionSignature sizeof = new SizeofEfun();
        public static final FunctionSignature allocate = new AllocateEfun();
        public static final FunctionSignature allocate_mapping = new AllocateMappingEfun();
        public static final FunctionSignature keys = new KeysEfun();
        public static final FunctionSignature filter = new FilterEfun();
        public static final FunctionSignature map = new MapEfun();
        public static final FunctionSignature sort_array = new SortArrayEfun();
        public static final FunctionSignature member_array = new MemberArrayEfun();
        public static final FunctionSignature arrayp = new ArraypEfun();
        public static final FunctionSignature pointerp = arrayp;
        public static final FunctionSignature mapp = new MappEfun();
    }

    public static class OBJECT
    {
        public static final FunctionSignature this_object = new ThisObjectEfun();
        public static final FunctionSignature master = new MasterEfun();
        public static final FunctionSignature inherits = new InheritsEfun();
        public static final FunctionSignature deep_inherit_list = new InheritListEfun(true);
        public static final FunctionSignature shallow_inherit_list = new InheritListEfun(false);
        public static final FunctionSignature inherit_list = shallow_inherit_list;
        public static final FunctionSignature file_name = new FileNameEfun();
        public static final FunctionSignature find_object = new FindObjectEfun();
        public static final FunctionSignature objects = new ObjectsEfun();
        public static final FunctionSignature objectp = new ObjectpEfun();
        public static final FunctionSignature load_object = new LoadObjectEfun();
        public static final FunctionSignature clone_object = new CloneObjectEfun();
        public static final FunctionSignature _new = clone_object;
        public static final FunctionSignature destruct = new DestructEfun();
        public static final FunctionSignature clonep = new ClonepEfun();
    }

    public static class CLASS
    {
        public static final FunctionSignature classp = new ClasspEfun();
    }

    public static class BUFFER
    {
        public static final FunctionSignature bufferp = new BufferpEfun();
    }

    public static class ENVIRONMENT
    {
        public static final FunctionSignature environment = new EnvironmentEfun();
        public static final FunctionSignature present = new PresentEfun();
        public static final FunctionSignature all_inventory = new InventoryEfun(false);
        public static final FunctionSignature deep_inventory = new InventoryEfun(true);
    }

    public static class INTERACTIVE
    {
        public static final FunctionSignature users = new UsersEfun();
        public static final FunctionSignature userp = new UserpEfun();
        public static final FunctionSignature interactive = new InteractiveEfun();
        public static final FunctionSignature exec = new ExecEfun();
        public static final FunctionSignature this_player = new ThisPlayerEfun();
        public static final FunctionSignature find_player = new FindPlayerEfun();
        public static final FunctionSignature find_living = new FindLivingEfun();
        public static final FunctionSignature living = new LivingEfun();
        public static final FunctionSignature livings = new LivingsEfun();
        public static final FunctionSignature write = new WriteEfun();
        public static final FunctionSignature message = new MessageEfun();
        public static final FunctionSignature flush_messages = new FlushMessagesEfun();
        public static final FunctionSignature terminal_colour = new TerminalColourEfun();
        public static final FunctionSignature snoop = new SnoopEfun();
        public static final FunctionSignature query_snoop = new QuerySnoopEfun();
        public static final FunctionSignature query_snooping = new QuerySnoopingEfun();
    }

    public static class MATH
    {
        public static final FunctionSignature to_int = new ToIntEfun();
        public static final FunctionSignature to_float = new ToFloatEfun();
        public static final FunctionSignature intp = new IntpEfun();
        public static final FunctionSignature floatp = new IntpEfun();
        public static final FunctionSignature random = new RandomEfun();
    }

    public static class CALLS
    {
        public static final FunctionSignature call_out = new CallOutEfun();
        public static final FunctionSignature call_out_info = new CallOutInfoEfun();
        public static final FunctionSignature call_other = new CallOtherEfun();
        public static final FunctionSignature functions = new FunctionsEfun();
        public static final FunctionSignature function_exists = new FunctionExistsEfun();
        public static final FunctionSignature previous_object = new PreviousObjectEfun();
        public static final FunctionSignature functionp = new FunctionpEfun();
        public static final FunctionSignature evaluate = new EvaluateEfun();
        public static final FunctionSignature bind = new BindEfun();
        public static final FunctionSignature call_stack = new CallStackEfun();
    }

    public static class FILE
    {
        public static final FunctionSignature file_size = new FileSizeEfun();
        public static final FunctionSignature read_file = new ReadFileEfun();
        public static final FunctionSignature read_bytes = new ReadBytesEfun();
        public static final FunctionSignature write_file = new WriteFileEfun();
        public static final FunctionSignature cp = new CopyFileEfun();
        public static final FunctionSignature rm = new RemoveFileEfun();
        public static final FunctionSignature mkdir = new CreateDirectoryEfun();
        public static final FunctionSignature get_dir = new GetDirectoryInfoEfun();
    }

    public static class SYSTEM
    {
        public static final FunctionSignature shutdown = new ShutdownEfun();
        public static final FunctionSignature error = new ErrorEfun();
        public static final FunctionSignature debug_message = new DebugMessageEfun();
        public static final FunctionSignature debug_info = new DebugInfoEfun();
        public static final FunctionSignature time = new TimeEfun();
        public static final FunctionSignature ctime = new CtimeEfun();
        public static final FunctionSignature localtime = new LocaltimeEfun();
        public static final FunctionSignature crypt = new CryptEfun();
        public static final FunctionSignature query_privs = new QueryPrivsEfun();
        public static final FunctionSignature reset_eval_cost = new ResetEvalCostEfun();
        public static final FunctionSignature set_eval_limit = new SetEvalLimitEfun();
        public static final FunctionSignature dump_file_descriptors = new NoOpEfun(new StringValue(""));
    }

    public static class NET
    {
        public static final FunctionSignature query_ip_number = new QueryIpNumberEfun();
        public static final FunctionSignature query_ip_name = new QueryIpNameEfun();
    }

    public static FunctionMap get()
    {
        FunctionMap efuns = new FunctionMap();
        populate(efuns, MISC.class);
        populate(efuns, STRING.class);
        populate(efuns, OBJECT.class);
        populate(efuns, CLASS.class);
        populate(efuns, BUFFER.class);
        populate(efuns, ENVIRONMENT.class);
        populate(efuns, COLLECTION.class);
        populate(efuns, INTERACTIVE.class);
        populate(efuns, MATH.class);
        populate(efuns, CALLS.class);
        populate(efuns, FILE.class);
        populate(efuns, SYSTEM.class);
        populate(efuns, NET.class);
        return efuns;
    }

    private static void populate(FunctionMap efuns, Class< ? > container)
    {
        Field[] fields = container.getFields();
        for (Field field : fields)
        {
            boolean isStatic = Modifier.isStatic(field.getModifiers());
            boolean isSignature = FunctionSignature.class.isAssignableFrom(field.getType());
            if (isSignature && isStatic)
            {
                try
                {
                    FunctionSignature signature = (FunctionSignature) field.get(null);
                    String name = field.getName();
                    if (name.charAt(0) == '_')
                    {
                        name = name.substring(1);
                    }
                    efuns.put(name, signature);
                }
                catch (IllegalAccessException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
