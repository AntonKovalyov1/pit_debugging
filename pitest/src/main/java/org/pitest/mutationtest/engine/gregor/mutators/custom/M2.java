/*
 * Copyright 2018 org.pitest.
 *
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
 */
package org.pitest.mutationtest.engine.gregor.mutators.custom;

import org.objectweb.asm.Type;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;
/**
 * Mutates method invocations by other method invocations with the same name and 
 * same return type but different arguments. If the new arguments size is 
 * greater than the old one, replace new arguments with default values. If the 
 * new arguments size is less than the old one, use a subset of the old 
 * arguments.
 * 
 * @author koval
 */
public final class M2 implements MethodMutatorFactory {

    private static final int MAX_MUTATORS = 5;
    private final int key;
    
    private M2(final int key) {
        this.key = key;
    }
    
    public static Iterable<MethodMutatorFactory> makeMutators() {
        final List<MethodMutatorFactory> variations = new ArrayList<>();
        for (int i = 0; i < MAX_MUTATORS; i++) {
            variations.add(new M2(i));
        }
        return variations;
    }

    @Override
    public MethodVisitor create(final MutationContext context, 
                                final MethodInfo methodInfo, 
                                final MethodVisitor methodVisitor) {
        return new OverloadedMethodVisitor(this, context, methodVisitor, key);
    }

    @Override
    public String getGloballyUniqueId() {
        return getClass().getName() + "_" + getName();
    }

    @Override
    public String getName() {
        return "M2_METHOD_OVERLOADING_MUTATOR_" + (key + 1);
    }    
}

class ClassMethodsTracker extends ClassVisitor {

    final String methodName;
    final ArrayList<MethodInfo> methods = new ArrayList<>();
    
    public ClassMethodsTracker(final String methodName) {
        super(Opcodes.ASM6);
        this.methodName = methodName;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
            String signature, String[] exceptions) {
            methods.add(new MethodInfo()
                           .withAccess(access)
                           .withMethodName(name)
                           .withMethodDescriptor(desc));
        return null;
    }
}

class OverloadedMethodVisitor extends MethodVisitor {

    //use cache to save visited classes methods for reuse
    static final Map<String, ArrayList<MethodInfo>> CACHED_METHODS = 
            new HashMap<>();
    static final int MAX_CACHE_SIZE = 1000;
    
    final MutationContext context;
    final MethodMutatorFactory factory;
    final int key;

    public OverloadedMethodVisitor(
            final MethodMutatorFactory factory,
            final MutationContext context,
            final MethodVisitor writer,
            final int key) {
        super(Opcodes.ASM6, writer);
        this.factory = factory;
        this.context = context;
        this.key = key;
    }
    
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, 
            String desc, boolean itf) {
        ArrayList<MethodInfo> overloaded = findOverloaded(owner, name, desc);
        if (overloaded.size() <= key) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        } else {
            String newDesc = overloaded.get(key).getMethodDescriptor();
            if (shouldMutate(name, desc, newDesc)) {
                mutateOverloadedMethodInsn(opcode, owner, name, desc, itf, 
                        newDesc);
            } else {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
            }
        }
    }

    private ArrayList<MethodInfo> findOverloaded(final String owner, 
                                                 final String name,
                                                 final String desc) {
        //check in cache
        ArrayList<MethodInfo> overloaded = CACHED_METHODS.get(owner);
        if (overloaded != null) {
            return filterOverloaded(name, desc, overloaded);
        }
        ClassReader cr;
        try {
            cr = new ClassReader(owner);
            return findOverloaded(owner, name, desc, cr);
        } catch (IOException ex) { }
        try {
            FileInputStream fis = new FileInputStream(new File(
                    "./target/classes/" + owner + ".class"));
            cr = new ClassReader(fis);
            return findOverloaded(owner, name, desc, cr);
        } catch (IOException ex) {
            System.out.println("M2 mutator: mutating external library method "
                    + "calls is not supported as it is the case with " + owner);
        }
        return new ArrayList<>();
    }
    
    private ArrayList<MethodInfo> findOverloaded(final String owner, 
                                                 final String name,
                                                 final String desc,
                                                 final ClassReader cr) {
        ClassMethodsTracker cmt = new ClassMethodsTracker(name);
        cr.accept(cmt, 0);
        cache(owner, cmt.methods);
        return filterOverloaded(name, desc, cmt.methods);        
    }
    
    private ArrayList<MethodInfo> filterOverloaded(final String name, 
            final String desc, final ArrayList<MethodInfo> overloaded) {
        ArrayList<MethodInfo> filtered = new ArrayList<>();
        MethodInfo invoked = findCurrentlyInvokedMethod(name, desc, overloaded);
        if (invoked == null) {
            return new ArrayList<>();
        }
        Type[] oldArgTypes = Type.getArgumentTypes(desc);
        for (MethodInfo curr : overloaded) {
            Type[] newArgTypes = Type.getArgumentTypes(
                    curr.getMethodDescriptor());
            if (invoked.getName().equals(curr.getName())
                    && invoked.getReturnType().equals(curr.getReturnType()) 
                    && invoked.getAccess() == curr.getAccess()
                    && newArgTypes.length != oldArgTypes.length
                    && similarArgTypes(oldArgTypes, newArgTypes)) {
                filtered.add(curr);
            }
        }
        return filtered;
    }
        
    private MethodInfo findCurrentlyInvokedMethod(final String name, 
                final String desc, final ArrayList<MethodInfo> overloaded) {
        for (MethodInfo curr : overloaded) {
            if (name.equals(curr.getName()) 
                    && desc.equals(curr.getMethodDescriptor())) {
                return curr;
            }
        }
        return null;
    }
    
    private boolean shouldMutate(final String name,
                                 final String originalDesc,
                                 final String newDesc) {
        final MutationIdentifier newId = this.context.registerMutation(
            this.factory, factory.getName() + " : overloaded method call " 
                    + name + " " + originalDesc + " replaced with " + name 
                    + " " + newDesc);
        return this.context.shouldMutate(newId);
    }

    private void mutateOverloadedMethodInsn(final int opcode, 
                                            final String owner, 
                                            final String name, 
                                            final String desc, 
                                            final boolean itf, 
                                            final String newDesc) {
        Type[] oldArgTypes = Type.getArgumentTypes(desc);
        Type[] newArgTypes = Type.getArgumentTypes(newDesc);
        if (oldArgTypes.length < newArgTypes.length) {
            loadDefaultValuesToStack(Arrays.copyOfRange(newArgTypes, 
                    oldArgTypes.length, newArgTypes.length));
        } else {
            popLoadedValues(Arrays.copyOfRange(oldArgTypes, newArgTypes.length, 
                    oldArgTypes.length));
        }
        super.visitMethodInsn(opcode, owner, name, newDesc, itf);
    }  

    private boolean similarArgTypes(Type[] list1, Type[] list2) {
        for (int i = 0; i < list1.length; i++) {
            if (list2.length <= i) {
                return true;
            }
            if (!list1[i].equals(list2[i])) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * New arguments size is greater than the old one, load default values to 
     * stack
     * @param newArgs 
     */
    private void loadDefaultValuesToStack(Type[] newArgs) {
        for (Type newArg : newArgs) {
            switch (newArg.getDescriptor()) {
                case "Z":
                case "C":
                case "B":
                case "S":
                case "I":
                    super.visitInsn(Opcodes.ICONST_0);
                    break;
                case "F":
                    super.visitInsn(Opcodes.FCONST_0);
                    break;
                case "J":
                    super.visitInsn(Opcodes.LCONST_0);
                    break;
                case "D":
                    super.visitInsn(Opcodes.DCONST_0);
                    break;
                default:
                    super.visitInsn(Opcodes.ACONST_NULL);
                    break;                   
            }
        }
    }

    /**
     * New arguments size is less than the old one, pop loaded values from stack 
     * (top to bottom)
     * @param valuesToPop 
     */
    private void popLoadedValues(Type[] valuesToPop) {
        for (int i = valuesToPop.length - 1; i >= 0; i--) {
            if (valuesToPop[i].getSize() == 1) {
                super.visitInsn(Opcodes.POP);
            } else {
                super.visitInsn(Opcodes.POP2);
            }
        }       
    }
    
    private void cache(final String owner, 
                       final ArrayList<MethodInfo> classMethods) {
        if (CACHED_METHODS.containsKey(owner) || classMethods == null) {
            return;
        }
        if (CACHED_METHODS.size() + 1 > MAX_CACHE_SIZE) {
            CACHED_METHODS.clear();
            CACHED_METHODS.put(owner, classMethods);
        }
        CACHED_METHODS.put(owner, classMethods);
    }
}
