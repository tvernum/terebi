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
import java.util.HashMap;
import java.util.Map;

import us.terebi.lang.lpc.runtime.jvm.context.Efuns;
import us.terebi.lang.lpc.runtime.jvm.context.MappedEfuns;
import us.terebi.lang.lpc.runtime.jvm.efun.ArraypEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.BindEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.BufferpEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.CallOtherEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.CallStackEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.ClasspEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.CloneObjectEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.ClonepEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.DebugInfoEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.DebugMessageEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.DestructEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.Efun;
import us.terebi.lang.lpc.runtime.jvm.efun.ErrorEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.EvaluateEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.FetchVariableEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.FileNameEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.FindLivingEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.FindObjectEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.FindPlayerEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.FlushMessagesEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.FunctionExistsEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.FunctionOwnerEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.FunctionpEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.FunctionsEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.InheritListEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.InheritsEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.IntpEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.LivingEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.LivingsEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.LoadObjectEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.MappEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.MasterEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.MatchPathEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.NoOpEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.NullpEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.ObjectpEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.ObjectsEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.OriginEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.PreviousObjectEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.QueryIdleEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.QueryIpNameEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.QueryIpNumberEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.QuerySnoopEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.QuerySnoopingEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.RandomEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.ShutdownEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.SnoopEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.StoreVariableEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.StringpEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.TerminalColourEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.ThisObjectEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.ToFloatEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.ToIntEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.TypeofEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.UserpEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.VariablesEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.VirtualpEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.WriteEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.callout.CallOutEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.callout.CallOutInfoEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.callout.FindCallOutEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.callout.RemoveCallOutEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.collection.AllocateEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.collection.AllocateMappingEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.collection.FilterEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.collection.FilterMappingEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.collection.KeysEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.collection.MapArrayEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.collection.MapDeleteEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.collection.MapEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.collection.MapMappingEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.collection.MemberArrayEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.collection.SizeofEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.collection.SortArrayEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.collection.UniqueArrayEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.collection.UniqueMappingEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.collection.ValuesEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.environment.EnvironmentEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.environment.InventoryEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.environment.MoveObjectEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.environment.PresentEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.file.CopyFileEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.file.CreateDirectoryEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.file.FileSizeEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.file.GetDirectoryInfoEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.file.ReadBytesEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.file.ReadFileEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.file.RemoveFileEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.file.RenameEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.file.StatEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.file.WriteFileEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.heartbeat.HeartBeatsEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.heartbeat.QueryHeartBeatEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.heartbeat.SetHeartBeatEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.string.CapitalizeEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.string.ExplodeEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.string.ImplodeEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.string.LowerCaseEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.string.PluralizeEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.string.RegexpEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.string.RepeatStringEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.string.ReplaceStringEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.string.SprintfEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.string.SscanfEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.string.StrcmpEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.string.StrlenEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.string.StrsrchEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.string.TrimEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.string.UpperCaseEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.time.CtimeEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.time.GetElapsedExecTimeEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.time.GetMaxExecTimeEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.time.LocaltimeEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.time.SetMaxExecTimeEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.time.TimeEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.time.UptimeEfun;

/**
 * 
 */
public class StandardEfuns
{
    public static class MISC
    {
        public static final Efun nullp = new NullpEfun();
        public static final Efun undefinedp = nullp;
        public static final Efun typeof = new TypeofEfun();
    }

    public static class STRING
    {
        public static final Efun stringp = new StringpEfun();
        public static final Efun implode = new ImplodeEfun();
        public static final Efun explode = new ExplodeEfun();
        public static final Efun strlen = new StrlenEfun();
        public static final Efun sscanf = new SscanfEfun();
        public static final Efun sprintf = new SprintfEfun();
        public static final Efun strcmp  = new StrcmpEfun();
        public static final Efun strsrch = new StrsrchEfun();
        public static final Efun replace_string = new ReplaceStringEfun();
        public static final Efun repeat_string = new RepeatStringEfun();
        public static final Efun regexp = new RegexpEfun();
        public static final Efun lower_case = new LowerCaseEfun();
        public static final Efun upper_case = new UpperCaseEfun();
        public static final Efun capitalize = new CapitalizeEfun();
        public static final Efun pluralize = new PluralizeEfun();
        public static final Efun trim = new TrimEfun();
    }

    public static class COLLECTION
    {
        public static final Efun sizeof = new SizeofEfun();
        public static final Efun allocate = new AllocateEfun();
        public static final Efun allocate_mapping = new AllocateMappingEfun();
        public static final Efun keys = new KeysEfun();
        public static final Efun values = new ValuesEfun();
        public static final Efun map_delete = new MapDeleteEfun();
        public static final Efun filter = new FilterEfun();
        public static final Efun filter_array = new FilterEfun();
        public static final Efun filter_mapping = new FilterMappingEfun();
        public static final Efun map = new MapEfun();
        public static final Efun map_array = new MapArrayEfun();
        public static final Efun map_mapping = new MapMappingEfun();
        public static final Efun sort_array = new SortArrayEfun();
        public static final Efun member_array = new MemberArrayEfun();
        public static final Efun unique_array = new UniqueArrayEfun();
        public static final Efun unique_mapping = new UniqueMappingEfun();
        public static final Efun arrayp = new ArraypEfun();
        public static final Efun pointerp = arrayp;
        public static final Efun mapp = new MappEfun();
    }

    public static class OBJECT
    {
        public static final Efun this_object = new ThisObjectEfun();
        public static final Efun master = new MasterEfun();
        public static final Efun inherits = new InheritsEfun();
        public static final Efun deep_inherit_list = new InheritListEfun(true);
        public static final Efun shallow_inherit_list = new InheritListEfun(false);
        public static final Efun inherit_list = shallow_inherit_list;
        public static final Efun file_name = new FileNameEfun();
        public static final Efun find_object = new FindObjectEfun();
        public static final Efun objects = new ObjectsEfun();
        public static final Efun objectp = new ObjectpEfun();
        public static final Efun load_object = new LoadObjectEfun();
        public static final Efun clone_object = new CloneObjectEfun();
        public static final Efun _new = clone_object;
        public static final Efun destruct = new DestructEfun();
        public static final Efun clonep = new ClonepEfun();
        public static final Efun virtualp = new VirtualpEfun();
        public static final Efun variables = new VariablesEfun();
        public static final Efun store_variable = new StoreVariableEfun();
        public static final Efun fetch_variable = new FetchVariableEfun();
    }

    public static class CLASS
    {
        public static final Efun classp = new ClasspEfun();
    }

    public static class BUFFER
    {
        public static final Efun bufferp = new BufferpEfun();
    }

    public static class ENVIRONMENT
    {
        public static final Efun environment = new EnvironmentEfun();
        public static final Efun present = new PresentEfun();
        public static final Efun all_inventory = new InventoryEfun(false);
        public static final Efun deep_inventory = new InventoryEfun(true);
        public static final Efun move_object = new MoveObjectEfun();
    }

    public static class INTERACTIVE
    {
        public static final Efun userp = new UserpEfun();
        public static final Efun find_player = new FindPlayerEfun();
        public static final Efun find_living = new FindLivingEfun();
        public static final Efun living = new LivingEfun();
        public static final Efun livings = new LivingsEfun();
        public static final Efun write = new WriteEfun();
        public static final Efun flush_messages = new FlushMessagesEfun();
        public static final Efun terminal_colour = new TerminalColourEfun();
        public static final Efun snoop = new SnoopEfun();
        public static final Efun query_snoop = new QuerySnoopEfun();
        public static final Efun query_snooping = new QuerySnoopingEfun();
        public static final Efun query_idle= new QueryIdleEfun();
    }

    public static class MATH
    {
        public static final Efun to_int = new ToIntEfun();
        public static final Efun to_float = new ToFloatEfun();
        public static final Efun intp = new IntpEfun();
        public static final Efun floatp = new IntpEfun();
        public static final Efun random = new RandomEfun();
    }

    public static class CALLS
    {
        public static final Efun call_out = new CallOutEfun();
        public static final Efun call_out_info = new CallOutInfoEfun();
        public static final Efun find_call_out = new FindCallOutEfun();
        public static final Efun remove_call_out = new RemoveCallOutEfun();
        public static final Efun call_other = new CallOtherEfun();
        public static final Efun functions = new FunctionsEfun();
        public static final Efun function_exists = new FunctionExistsEfun();
        public static final Efun previous_object = new PreviousObjectEfun();
        public static final Efun functionp = new FunctionpEfun();
        public static final Efun evaluate = new EvaluateEfun();
        public static final Efun bind = new BindEfun();
        public static final Efun function_owner = new FunctionOwnerEfun();
        public static final Efun call_stack = new CallStackEfun();
        public static final Efun origin = new OriginEfun();
        public static final Efun set_heart_beat = new SetHeartBeatEfun();
        public static final Efun query_heart_beat = new QueryHeartBeatEfun();
        public static final Efun heart_beats = new HeartBeatsEfun();
    }

    public static class FILE
    {
        public static final Efun file_size = new FileSizeEfun();
        public static final Efun read_file = new ReadFileEfun();
        public static final Efun read_bytes = new ReadBytesEfun();
        public static final Efun write_file = new WriteFileEfun();
        public static final Efun rename = new RenameEfun();
        public static final Efun cp = new CopyFileEfun();
        public static final Efun rm = new RemoveFileEfun();
        public static final Efun mkdir = new CreateDirectoryEfun();
        public static final Efun get_dir = new GetDirectoryInfoEfun();
        public static final Efun stat = new StatEfun();
        public static final Efun match_path = new MatchPathEfun();
    }

    public static class SYSTEM
    {
        public static final Efun shutdown = new ShutdownEfun();
        public static final Efun error = new ErrorEfun();
        public static final Efun debug_message = new DebugMessageEfun();
        public static final Efun debug_info = new DebugInfoEfun();
        public static final Efun time = new TimeEfun();
        public static final Efun ctime = new CtimeEfun();
        public static final Efun localtime = new LocaltimeEfun();
        public static final Efun uptime = new UptimeEfun();
        public static final Efun dump_file_descriptors = new NoOpEfun(LpcConstants.STRING.BLANK);
        public static final Efun reclaim_objects = new NoOpEfun(LpcConstants.INT.ZERO);

        public static final Efun get_max_execution_time = new GetMaxExecTimeEfun();
        public static final Efun set_max_execution_time = new SetMaxExecTimeEfun();
        public static final Efun get_elapsed_execution_time = new GetElapsedExecTimeEfun();
    }

    public static class NET
    {
        public static final Efun query_ip_number = new QueryIpNumberEfun();
        public static final Efun query_ip_name = new QueryIpNameEfun();
    }

    private static final Map<String, Efun> _efuns = new HashMap<String, Efun>();

    public static Efuns getImplementation()
    {
        checkPopulated();
        MappedEfuns efuns = new MappedEfuns();
        for (String name : _efuns.keySet())
        {
            Efun efun = _efuns.get(name);
            efuns.define(name, efun, efun);
        }
        return efuns;
    }

    private static void checkPopulated()
    {
        synchronized (_efuns)
        {
            if (_efuns.isEmpty())
            {
                populate(_efuns, MISC.class);
                populate(_efuns, STRING.class);
                populate(_efuns, OBJECT.class); 
                populate(_efuns, CLASS.class);
                populate(_efuns, BUFFER.class);
                populate(_efuns, ENVIRONMENT.class);
                populate(_efuns, COLLECTION.class);
                populate(_efuns, INTERACTIVE.class);
                populate(_efuns, MATH.class);
                populate(_efuns, CALLS.class);
                populate(_efuns, FILE.class);
                populate(_efuns, SYSTEM.class);
                populate(_efuns, NET.class);
            }
        }
    }

    private static void populate(Map<String, Efun> efuns, Class< ? > container)
    {
        Field[] fields = container.getFields();
        for (Field field : fields)
        {
            boolean isStatic = Modifier.isStatic(field.getModifiers());
            boolean isSignature = Efun.class.isAssignableFrom(field.getType());
            if (isSignature && isStatic)
            {
                try
                {
                    Efun efun = (Efun) field.get(null);
                    String name = field.getName();
                    if (name.charAt(0) == '_')
                    {
                        name = name.substring(1);
                    }
                    efuns.put(name, efun);
                }
                catch (IllegalAccessException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
