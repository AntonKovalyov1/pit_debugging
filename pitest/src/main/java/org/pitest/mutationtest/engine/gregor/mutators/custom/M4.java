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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

/**
 * copies all the visited variables (method wise) in a MAP,
 * and load an arbitrary variable of the same time if available 
 * when a given type is requested (_load)
 *
 * @author Holden
 */
public final class M4 implements MethodMutatorFactory {

    private static final int MAX_MUTATORS = 1;
    private final int key;

    private M4(final int key) {
        this.key = key;
    }

    public static Iterable<MethodMutatorFactory> makeMutators() {
        final List<MethodMutatorFactory> variations = new ArrayList<>();
        for (int i = 0; i < MAX_MUTATORS; i++) {
            variations.add(new M4(i));
        }
        return variations;
    }

    @Override
    public MethodVisitor create(final MutationContext context,
            final MethodInfo methodInfo,
            final MethodVisitor methodVisitor) {
        return new VaribleExchangeMethodVisitor(this,
                context,
                methodVisitor,
                methodInfo,
                "variables exchanged, key: " + key);
    }

    @Override
    public String getGloballyUniqueId() {
        return getClass().getName() + "_" + getName();
    }

    @Override
    public String getName() {
        return "M4_VARIABLE_EXCHANGE_MUTATOR_" + (key + 1);
    }
}

class VaribleExchangeMethodVisitor extends MethodVisitor {

    private final MutationContext context;
    private final MethodMutatorFactory factory;
    private final String description;
    private final MethodInfo mi;
    /**
     * to store previously visited variables Holden Map<storeLocation, Type>
     */
    private Map<Integer, String> previousVarMap;
    private int lastVisitInsn = -1;
    private String lastVisitOwner = "";
    //private boolean needMutation = false;

    public VaribleExchangeMethodVisitor(
            final MethodMutatorFactory factory,
            final MutationContext context,
            final MethodVisitor methodVisitor,
            final MethodInfo methodInfo,
            String desc) {
        super(Opcodes.ASM6, methodVisitor);
        this.mv = methodVisitor;
        this.description = desc;
        this.context = context;
        this.factory = factory;
        this.mi = methodInfo;

        previousVarMap = new HashMap<>();

        /**
         * if !(isStatic), store comes with a value
         */
        if (mi.isStatic()) {
            registerParams(mi.getMethodDescriptor(), 0);
        } else {
            registerParams(mi.getMethodDescriptor(), 1);
        }

        System.out.println("\nMETHOD_DESC: " + mi.getMethodDescriptor());
    }

    @Override
    public void visitInsn(int opcode) {
        super.visitInsn(opcode);
        /**
         * stores the last instruction, this is helpful detecting null objects
         */
        lastVisitInsn = opcode;
    }

    /**
     * Used this method to test a coarse mutation
     * (once per method)
     * 
     * @param opcode
     * @param owner
     * @param name
     * @param descriptor
     * @param isInterface 
     */
//    @Override
//    public void visitEnd() {
//        super.visitEnd(); //To change body of generated methods, choose Tools | Templates.
//        if (needMutation) {
//            final MutationIdentifier newId = this.context.registerMutation(
//                    this.factory, description);
//        }
//    }
    @Override
    public void visitMethodInsn(int opcode,
            String owner, String name, String descriptor, boolean isInterface) {

        lastVisitInsn = opcode;
        lastVisitOwner = "L" + owner;

        mv.visitMethodInsn(opcode, owner, name, descriptor, isInterface); //To change body of generated methods, choose Tools | Templates.
    }

    private void registerParams(String descriptor, int loc) {
        boolean isArray = false;
        int location = loc;
        for (int i = 0; i < descriptor.length(); i++) {
            System.out.println(">> LOCATION: " + location);
            switch (descriptor.charAt(i)) {
                case ')':
                    i = descriptor.length() + 1; //exit
                    break;
                case 'Z':
                case 'C':
                case 'B':
                case 'S':
                case 'I':
                    if (isArray) {
                        isArray = false;
                        break;
                    }
                    System.out.println("\nINTEGER REGIS: " + location);
                    previousVarMap.put(location, "int");
                    location++;
                    break;
                case 'F':
                    if (isArray) {
                        isArray = false;
                        break;
                    }
                    System.out.println("\nFLOAT REGIS: " + location);
                    previousVarMap.put(location, "float");
                    location++;
                    break;
                case 'J':
                    if (isArray) {
                        isArray = false;
                        break;
                    }
                    System.out.println("\nLONG REGIS: " + location);
                    previousVarMap.put(location, "long");
                    location += 2;
                    break;
                case 'D':
                    if (isArray) {
                        isArray = false;
                        break;
                    }
                    System.out.println("\nDOUBLE REGIS: " + location);
                    previousVarMap.put(location, "double");
                    location += 2;
                    break;
                case '[':
                    isArray = true;
                    location++;
                    break;
                case 'L':
                    int endOfObject = descriptor.indexOf(';', i);
                    String objectType = descriptor.substring(i, endOfObject);
                    i = endOfObject;
                    if (isArray) {
                        isArray = false;
                        break;
                    }
                    System.out.println("\n" + objectType + " REGIS: " + location);
                    if (objectType.equals("Ljava/lang/Integer")
                            || objectType.equals("Ljava/lang/Double")) {
                        previousVarMap.put(location, objectType);
                    }
                    location++;
                    break;

                default:
                    break;
            }
        }
    }

    private void printInfo(String type, int i1, int i2) {
        System.out.println(type
                + " found: "
                + i1
                + " -> "
                + i2
                + "\tsignature: "
                + mi.getMethodDescriptor()
                + "\tmapSize: " + previousVarMap.size());
    }

    /**
     * Simplify code ! register similar sections in a method
     *
     * @param opcode
     * @param var //location in the store
     */
    @Override
    public void visitVarInsn(int opcode, int var) {
        boolean foundAnother = false;
        switch (opcode) {
            /**
             * 54: istore 4 bytes: [boolean, char, byte, short, int] !!!!clean
             * index
             */
            case Opcodes.ISTORE:
                System.out.println("\n!! INTEGER REGIS: " + var);
                previousVarMap.put(var, "int");
                mv.visitVarInsn(opcode, var);
                break;
            case Opcodes.LSTORE:
                System.out.println("\n!! LONG REGIS: " + var);
                previousVarMap.put(var, "long");
                previousVarMap.remove(var + 1);
                mv.visitVarInsn(opcode, var);
                break;
            case Opcodes.FSTORE:
                System.out.println("\n!! FLOAT REGIS: " + var);
                previousVarMap.put(var, "float");
                mv.visitVarInsn(opcode, var);
                break;
            case Opcodes.DSTORE:
                System.out.println("\n!! DOUBLE REGIS: " + var);
                previousVarMap.put(var, "double");
                previousVarMap.remove(var + 1);
                mv.visitVarInsn(opcode, var);
                break;
            case Opcodes.ASTORE:
                /**
                 * register if not null (object type) lastVisitOwner =
                 * lastExecutedInstruction of type: invokespecial invokestatic
                 * aload
                 *
                 * for now, only save Integers and Doubles
                 */
                if ((lastVisitInsn != Opcodes.ACONST_NULL) && lastVisitOwner != null) {
                    lastVisitInsn = Opcodes.ACONST_NULL;
                    System.out.println("\n" + lastVisitOwner + " REGIS: " + var);
                    if (lastVisitOwner.equals("Ljava/lang/Integer")
                            || lastVisitOwner.equals("Ljava/lang/Double")) {
                        previousVarMap.put(var, lastVisitOwner);
                    }

                } else {
                    System.out.println("couldn't detect object type");
                }
                mv.visitVarInsn(opcode, var);
                break;
            /**
             * 21: iload 4 bytes: [boolean, char, byte, short, int] (also
             * accounts for other loads e.g. ILOAD_1)
             */
            /**
             * case 0x1a: case 0x1b: case 0x1c: case 0x1d:
             */
            case Opcodes.ILOAD:
                foundAnother = false;
                for (int key : previousVarMap.keySet()) {
                    //use a different value of type int (or Integer)
                    if (key != var) {
                        if (previousVarMap.get(key).equals("int")) {
                            //
                            printInfo("integer", var, key);
                            final MutationIdentifier newId = this.context.registerMutation(
                                    this.factory, description);
                            mv.visitVarInsn(opcode, key);
                            foundAnother = true;
                            break;

                        } else if (previousVarMap.get(key).equals("Ljava/lang/Integer")) {
                            printInfo("Ljava/lang/Integer", var, key);
                            final MutationIdentifier newId = this.context.registerMutation(
                                    this.factory, description);
                            mv.visitVarInsn(Opcodes.ALOAD, key);
                            foundAnother = true;
                            break;
                        }
                    }
                }
                if (!foundAnother) {
                    mv.visitVarInsn(opcode, var);
                }
                break;
            /**
             * case 0x1e: case 0x1f: case 0x20: case 0x21:
             */
            case Opcodes.LLOAD:
                foundAnother = false;
                for (int key : previousVarMap.keySet()) {
                    //use a different value of type int (or Integer)
                    if (key != var) {
                        if (previousVarMap.get(key).equals("long")) {
                            printInfo("long", var, key);
                            final MutationIdentifier newId = this.context.registerMutation(
                                    this.factory, description);
                            mv.visitVarInsn(opcode, key);
                            foundAnother = true;
                            break;

                        } else if (previousVarMap.get(key).equals("Ljava/lang/Long")) {
                            printInfo("Ljava/lang/Long", var, key);
                            final MutationIdentifier newId = this.context.registerMutation(
                                    this.factory, description);
                            mv.visitVarInsn(Opcodes.ALOAD, key);
                            foundAnother = true;
                            break;
                        }
                    }
                }
                if (!foundAnother) {
                    mv.visitVarInsn(opcode, var);
                }
                break;
            /**
             * case 0x22: case 0x23: case 0x24: case 0x25:
             */
            case Opcodes.FLOAD:
                foundAnother = false;
                for (int key : previousVarMap.keySet()) {
                    //use a different value of type int (or Integer)
                    if (key != var) {
                        if (previousVarMap.get(key).equals("float")) {
                            printInfo("float", var, key);
                            final MutationIdentifier newId = this.context.registerMutation(
                                    this.factory, description);
                            mv.visitVarInsn(opcode, key);
                            foundAnother = true;
                            break;

                        } else if (previousVarMap.get(key).equals("Ljava/lang/Float")) {
                            printInfo("Ljava/lang/Float", var, key);
                            final MutationIdentifier newId = this.context.registerMutation(
                                    this.factory, description);
                            mv.visitVarInsn(Opcodes.ALOAD, key);
                            foundAnother = true;
                            break;
                        }
                    }
                }
                if (!foundAnother) {
                    mv.visitVarInsn(opcode, var);
                }
                break;
            /**
             * case 0x26: case 0x27: case 0x28: case 0x29:
             */
            case Opcodes.DLOAD:
                foundAnother = false;
                for (int key : previousVarMap.keySet()) {
                    //use a different value of type int (or Integer)
                    if (key != var) {
                        if (previousVarMap.get(key).equals("double")) {
                            printInfo("double", var, key);
                            final MutationIdentifier newId = this.context.registerMutation(
                                    this.factory, description);
                            mv.visitVarInsn(opcode, key);
                            foundAnother = true;
                            break;

                        } else if (previousVarMap.get(key).equals("Ljava/lang/Double")) {
                            printInfo("Ljava/lang/Double", var, key);
                            final MutationIdentifier newId = this.context.registerMutation(
                                    this.factory, description);
                            mv.visitVarInsn(Opcodes.ALOAD, key);
                            foundAnother = true;
                            break;
                        }
                    }
                }
                if (!foundAnother) {
                    mv.visitVarInsn(opcode, var);
                }
                break;
            case Opcodes.ALOAD:
                foundAnother = false;
                String type = previousVarMap.get(var);
                lastVisitOwner = type;
                if (type == null) {
                    mv.visitVarInsn(opcode, var);
                    break;
                }
                for (int key : previousVarMap.keySet()) {
                    //use a different value of type int (or Integer)
                    if (key != var && previousVarMap.get(key).equals(type)) {
                        printInfo(type, var, key);
                        final MutationIdentifier newId = this.context.registerMutation(
                                this.factory, description);
                        mv.visitVarInsn(opcode, key);
                        foundAnother = true;
                        break;
                    }
                }
                if (!foundAnother) {
                    mv.visitVarInsn(opcode, var);
                }
                break;
            default:
                mv.visitVarInsn(opcode, var);
        }
    }

    /**
     * how is shouldMutate in this case ? returning true in the meanwhile
     */
    private boolean shouldMutate() {
        final MutationIdentifier newId = this.context.registerMutation(
                this.factory, description);
        return context.shouldMutate(newId);
    }

}
