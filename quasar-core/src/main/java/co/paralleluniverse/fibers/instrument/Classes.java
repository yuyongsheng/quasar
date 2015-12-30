/*
 * Quasar: lightweight threads and actors for the JVM.
 * Copyright (c) 2013-2015, Parallel Universe Software Co. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 3.0
 * as published by the Free Software Foundation.
 */
package co.paralleluniverse.fibers.instrument;

import co.paralleluniverse.fibers.Instrumented;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

/**
 * This class contains hard-coded values with the names of the classes and methods relevant for instrumentation.
 *
 * @author pron
 */
public final class Classes {
    static final String OBJECT_NAME = Object.class.getName().replace('.', '/');
    static final String STRING_NAME = String.class.getName().replace('.', '/');
    static final String CLASS_NAME = Class.class.getName().replace('.', '/');

    static final String THROWABLE_NAME = Throwable.class.getName().replace('.', '/');
    static final String EXCEPTION_NAME = Exception.class.getName().replace('.', '/');
    static final String RUNTIME_EXCEPTION_NAME = RuntimeException.class.getName().replace('.', '/');

    static final String INVOCATION_TARGET_EXCEPTION_NAME = InvocationTargetException.class.getName().replace('.', '/');
    static final String REFLECTIVE_OPERATION_EXCEPTION_NAME = ReflectiveOperationException.class.getName().replace('.', '/');

    static final String SUSPEND_EXECUTION_NAME = "co/paralleluniverse/fibers/SuspendExecution";
    static final String RUNTIME_SUSPEND_EXECUTION_NAME = "co/paralleluniverse/fibers/RuntimeSuspendExecution";
    static final String UNDECLARED_THROWABLE_NAME = "java/lang/reflect/UndeclaredThrowableException";
    static final String ANNOTATION_NAME = "co/paralleluniverse/fibers/Suspendable";
    static final String DONT_INSTRUMENT_ANNOTATION_NAME = "co/paralleluniverse/fibers/instrument/DontInstrument";
    static final String FIBER_CLASS_NAME = "co/paralleluniverse/fibers/Fiber"; //Type.getInternalName(COROUTINE_CLASS);
    private static final String STRAND_NAME = "co/paralleluniverse/strands/Strand"; //Type.getInternalName(COROUTINE_CLASS);
    static final String STACK_NAME = "co/paralleluniverse/fibers/Stack";
    //static final String EXCEPTION_INSTANCE_NAME = "exception_instance_not_for_user_code";
    private static final BlockingMethod BLOCKING_METHODS[] = {
        new BlockingMethod("java/lang/Thread", "sleep", "(J)V", "(JI)V"),
        new BlockingMethod("java/lang/Thread", "join", "()V", "(J)V", "(JI)V"),
        new BlockingMethod("java/lang/Object", "wait", "()V", "(J)V", "(JI)V"),
    };
    // computed
    // static final String EXCEPTION_DESC = "L" + SUSPEND_EXECUTION_NAME + ";";
    static final String ANNOTATION_DESC = "L" + ANNOTATION_NAME + ";";
    static final String DONT_INSTRUMENT_ANNOTATION_DESC = "L" + DONT_INSTRUMENT_ANNOTATION_NAME + ";";
    static final String ALREADY_INSTRUMENTED_DESC = Type.getDescriptor(Instrumented.class);

    private static final Set<String> yieldMethods = new HashSet<>(Arrays.asList(new String[] {
        "park", "yield", "parkAndUnpark", "yieldAndUnpark", "parkAndSerialize"
    }));

    public static boolean isYieldMethod(String className, String methodName) {
        return FIBER_CLASS_NAME.equals(className) && yieldMethods.contains(methodName);
    }

    public static boolean isAllowedToBlock(String className, String methodName) {
        return STRAND_NAME.equals(className);
    }

    public static int blockingCallIdx(MethodInsnNode ins) {
        for (int i = 0, n = BLOCKING_METHODS.length; i < n; i++) {
            if (BLOCKING_METHODS[i].match(ins))
                return i;
        }
        return -1;
    }

    static class BlockingMethod {
        private final String owner;
        private final String name;
        private final String[] descs;

        private BlockingMethod(String owner, String name, String... descs) {
            this.owner = owner;
            this.name = name;
            this.descs = descs;
        }

        public boolean match(MethodInsnNode min) {
            if (owner.equals(min.owner) && name.equals(min.name)) {
                for (String desc : descs) {
                    if (desc.equals(min.desc)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private Classes() {
    }
}
