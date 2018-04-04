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

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

/**
 *
 * @author koval
 */
public enum M1 implements MethodMutatorFactory {
    
    NULL_POINTER_DEREFERENCE_MUTATOR;
    
    @Override 
    public MethodVisitor create(final MutationContext context,
                                final MethodInfo methodInfo,
                                final MethodVisitor methodVisitor) {
        return new NullPointerFieldDereferenceVisitor(this, context, 
                methodVisitor);
    }
    
    @Override
    public String getGloballyUniqueId() {
        return this.getClass().getName() + "_" + getName();
    }
    
    @Override
    public String getName() {
        return name();
    }
}
    
    class NullPointerFieldDereferenceVisitor extends MethodVisitor {
        
    final MutationContext context;
    final MethodMutatorFactory factory;

    public NullPointerFieldDereferenceVisitor(
            final MethodMutatorFactory factory,
            final MutationContext context,
            final MethodVisitor writer) {
        super(Opcodes.ASM6, writer);
        this.factory = factory;
        this.context = context;
    }
        
    @Override
    public void visitFieldInsn(int opcode, 
                               String owner, 
                               String name, 
                               String desc) {
        if (opcode == Opcodes.GETFIELD && shouldMutate(owner, name)) {
            mutateGetFieldInsn(opcode, owner, name, desc);
        } else if (opcode == Opcodes.PUTFIELD && shouldMutate(owner, name)) {
            mutatePutFieldInsn(opcode, owner, name, desc);
        } else {
            super.visitFieldInsn(opcode, owner, name, desc);
        }
    }
    
    private boolean shouldMutate(final String ownerName, 
                                 final String fieldName) {
        final MutationIdentifier newId = this.context.registerMutation(
            this.factory, "M1: if there is a null pointer dereference on " 
                    + ownerName 
                    + " and field of type " 
                    + fieldName 
                    + " replace by field's default value");
        return this.context.shouldMutate(newId);
    }

    private void mutateGetFieldInsn(final int opcode, 
                                   final String owner, 
                                   final String name, 
                                   final String desc) {
        Label l0 = new Label();
        super.visitInsn(Opcodes.DUP);
        super.visitJumpInsn(Opcodes.IFNONNULL, l0);
        super.visitInsn(Opcodes.POP);
        loadDefaultValueToStack(desc);
        Label l1 = new Label();
        super.visitJumpInsn(Opcodes.GOTO, l1);
        super.visitLabel(l0);
        super.visitFieldInsn(opcode, owner, name, desc);
        super.visitLabel(l1); 
    }

    private void mutatePutFieldInsn(final int opcode, 
                                    final String owner, 
                                    final String name, 
                                    final String desc) {
        if (Type.getType(desc).getSize() == 1) {
            mutatePutFieldInsnSmall(opcode, owner, name, desc);
        } else {
            mutatePutFieldInsnBig(opcode, owner, name, desc);
        } 
    }
    
    private void mutatePutFieldInsnSmall(final int opcode, 
                                         final String owner, 
                                         final String name, 
                                         final String desc) {
        Label l0 = new Label();
        super.visitInsn(Opcodes.DUP2);
        super.visitInsn(Opcodes.POP);
        super.visitJumpInsn(Opcodes.IFNONNULL, l0);
        super.visitInsn(Opcodes.POP);
        super.visitInsn(Opcodes.POP);
        Label l1 = new Label();
        super.visitJumpInsn(Opcodes.GOTO, l1);
        super.visitLabel(l0);
        super.visitFieldInsn(opcode, owner, name, desc);
        super.visitLabel(l1);         
    }
    
    private void mutatePutFieldInsnBig(final int opcode, 
                                       final String owner, 
                                       final String name, 
                                       final String desc) {
        Label l0 = new Label();
        super.visitInsn(Opcodes.DUP2_X1);
        super.visitInsn(Opcodes.POP2);
        super.visitInsn(Opcodes.DUP_X2);
        super.visitJumpInsn(Opcodes.IFNONNULL, l0);
        super.visitInsn(Opcodes.POP2);
        super.visitInsn(Opcodes.POP);
        Label l1 = new Label();
        super.visitJumpInsn(Opcodes.GOTO, l1);
        super.visitLabel(l0);
        super.visitFieldInsn(opcode, owner, name, desc);
        super.visitLabel(l1);         
    }
    
    private void loadDefaultValueToStack(String desc) {
        switch (desc) {
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
