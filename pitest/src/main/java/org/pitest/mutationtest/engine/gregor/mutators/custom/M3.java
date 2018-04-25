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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;
/**
 * 
 * @author koval
 */
public final class M3 implements MethodMutatorFactory {

    private static final int MAX_MUTATORS = 5;
    private final int key;
    
    private M3(final int key) {
        this.key = key;
    }
    
    public static Iterable<MethodMutatorFactory> makeMutators() {
        final List<MethodMutatorFactory> variations = new ArrayList<>();
        for (int i = 0; i < MAX_MUTATORS; i++) {
            variations.add(new M3(i));
        }
        return variations;
    }

    @Override
    public MethodVisitor create(final MutationContext context, 
                                final MethodInfo methodInfo, 
                                final MethodVisitor methodVisitor) {
        return new M3MethodVisitor(this, context, methodVisitor, key);
    }

    @Override
    public String getGloballyUniqueId() {
        return getClass().getName() + "_" + getName();
    }

    @Override
    public String getName() {
        return "M3_METHOD_MUTATOR_" + (key + 1);
    }    
}

class M3MethodVisitor extends MethodVisitor {

    //use cache to save visited classes methods for reuse
    static final Map<String, ArrayList<MethodInfo>> CACHED_METHODS = 
            new HashMap<>();
    static final int MAX_CACHE_SIZE = 1000;
    
    final MutationContext context;
    final MethodMutatorFactory factory;
    final int key;

    public M3MethodVisitor(final MethodMutatorFactory factory,
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
        ArrayList<MethodInfo> methods = findMethods(owner, name, desc);
        if (methods.size() <= key) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        } else {
            String newName = methods.get(key).getName();
            if (shouldMutate(name, desc, newName)) {
                super.visitMethodInsn(opcode, owner, newName, desc, itf);
            } else {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
            }
        }
    }

    private ArrayList<MethodInfo> findMethods(final String owner, 
                                              final String name,
                                              final String desc) {
        //check in cache
        ArrayList<MethodInfo> methods = CACHED_METHODS.get(owner);
        if (methods != null) {
            return filterMethods(name, desc, methods);
        }
        ClassReader cr;
        try {
            cr = new ClassReader(owner);
            return findMethods(owner, name, desc, cr);
        } catch (IOException ex) { }
        try {
            FileInputStream fis = new FileInputStream(new File(
                    "./target/classes/" + owner + ".class"));
            cr = new ClassReader(fis);
            return findMethods(owner, name, desc, cr);
        } catch (IOException ex) {
            System.out.println("M3 mutator: mutating external library method "
                    + "calls is not supported as it is the case with " + owner);
        }
        return new ArrayList<>();
    }
    
    private ArrayList<MethodInfo> findMethods(final String owner, 
                                                 final String name,
                                                 final String desc,
                                                 final ClassReader cr) {
        ClassMethodsTracker cmt = new ClassMethodsTracker(name);
        cr.accept(cmt, 0);
        cache(owner, cmt.methods);
        return filterMethods(name, desc, cmt.methods);        
    }
    
    private ArrayList<MethodInfo> filterMethods(final String name, 
            final String desc, final ArrayList<MethodInfo> methods) {
        ArrayList<MethodInfo> filtered = new ArrayList<>();
        MethodInfo invoked = findCurrentlyInvokedMethod(name, desc, methods);
        if (invoked == null) {
            return new ArrayList<>();
        }
        for (MethodInfo curr : methods) {
            if (!invoked.getName().equals(curr.getName())
                    && invoked.getReturnType().equals(curr.getReturnType()) 
                    && invoked.getAccess() == curr.getAccess()
                    && invoked.getMethodDescriptor().equals(
                            curr.getMethodDescriptor())) {
                filtered.add(curr);
            }
        }
        return filtered;
    }
        
    private MethodInfo findCurrentlyInvokedMethod(final String name, 
                final String desc, final ArrayList<MethodInfo> methods) {
        for (MethodInfo curr : methods) {
            if (name.equals(curr.getName()) 
                    && desc.equals(curr.getMethodDescriptor())) {
                return curr;
            }
        }
        return null;
    }
    
    private boolean shouldMutate(final String name,
                                 final String originalDesc,
                                 final String newName) {
        final MutationIdentifier newId = this.context.registerMutation(
            this.factory, factory.getName() + " : method call " 
                    + name + " " + originalDesc + " replaced with " + newName 
                    + " " + originalDesc);
        return this.context.shouldMutate(newId);
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
