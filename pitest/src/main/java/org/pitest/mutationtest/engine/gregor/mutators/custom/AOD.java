package org.pitest.mutationtest.engine.gregor.mutators.custom;

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
public enum AOD implements MethodMutatorFactory {

    FIRST_OPERAND_MUTATOR {
        @Override
        public MethodVisitor create(final MutationContext context, 
                                    final MethodInfo methodInfo, 
                                    final MethodVisitor methodVisitor) {
            return new FirstOperandMethodVisitor(this, context, methodVisitor);
        }        
    },
    SECOND_OPERAND_MUTATOR {
        @Override
        public MethodVisitor create(final MutationContext context, 
                                    final MethodInfo methodInfo, 
                                    final MethodVisitor methodVisitor) {
            return new SecondOperandMethodVisitor(this, context, methodVisitor);
        }
    };

    @Override
    public String getGloballyUniqueId() {
        return this.getClass().getName() + "_" + getName();
    }

    @Override
    public String getName() {
        return "AOD_" + name();
    }
}

enum ArithmeticExprType {
    SMALL,
    BIG,
    NOT_APPLICABLE;

    static ArithmeticExprType getType(int opcode) {
        switch (opcode) {
            case Opcodes.IADD:
            case Opcodes.ISUB:
            case Opcodes.IMUL:
            case Opcodes.IDIV:
            case Opcodes.IREM:
            case Opcodes.FADD:
            case Opcodes.FSUB:
            case Opcodes.FMUL:
            case Opcodes.FDIV:
            case Opcodes.FREM:
                return ArithmeticExprType.SMALL;
            case Opcodes.LADD:
            case Opcodes.LSUB:
            case Opcodes.LMUL:
            case Opcodes.LDIV:
            case Opcodes.LREM:
            case Opcodes.DADD:
            case Opcodes.DSUB:
            case Opcodes.DMUL:
            case Opcodes.DDIV:
            case Opcodes.DREM:
                return ArithmeticExprType.BIG;
            default:
                return ArithmeticExprType.NOT_APPLICABLE;
        }
    }
}

abstract class OperandMethodVisitor extends MethodVisitor {
    final MutationContext context;
    final MethodMutatorFactory factory;
    
    public OperandMethodVisitor(final MethodMutatorFactory factory,
                                      final MutationContext context,
                                      final MethodVisitor writer) {
        super(Opcodes.ASM6, writer);
        this.context = context;
        this.factory = factory;
    }   
        
    @Override
    public void visitInsn(int opcode) {
        ArithmeticExprType type = ArithmeticExprType.getType(opcode);
        if (type == ArithmeticExprType.NOT_APPLICABLE) {
            super.visitInsn(opcode); 
        } else {
            final MutationIdentifier newId = this.context.registerMutation(
                this.factory, getDescription());
            if (this.context.shouldMutate(newId)) {
                if (type == ArithmeticExprType.SMALL) {
                    smallReplacement();
                } else {
                    bigReplacement();
                }
            } else {
                super.visitInsn(opcode);
            }
        }
    }
    
    abstract String getDescription();
    abstract void smallReplacement();
    abstract void bigReplacement();
}

class FirstOperandMethodVisitor extends OperandMethodVisitor {
    
    public FirstOperandMethodVisitor(final MethodMutatorFactory factory,
                                     final MutationContext context,
                                     final MethodVisitor writer) {
        super(factory, context, writer);
    }
    
    @Override
    public String getDescription() {
        return "AOD: Replaced arithmetic expression with first operand.";
    }
    
    @Override
    void smallReplacement() {
        //remove top value from stack
        super.visitInsn(Opcodes.POP);
    }
    
    @Override
    void bigReplacement() {
        //remove top double value from stack
        super.visitInsn(Opcodes.POP2);
    }
}

class SecondOperandMethodVisitor extends OperandMethodVisitor {
    
    public SecondOperandMethodVisitor(final MethodMutatorFactory factory,
                                      final MutationContext context,
                                      final MethodVisitor writer) {
        super(factory, context, writer);
    }
    
    @Override
    String getDescription() {
        return "AOD: Replaced arithmetic expression with second operand.";
    }
    
    @Override
    void smallReplacement() {
        //swap both integers in the stack
        super.visitInsn(Opcodes.SWAP);
        //remove top integer from stack
        super.visitInsn(Opcodes.POP);
    }
    
    @Override
    void bigReplacement() {
        //duplicate double value and insert beneath second
        super.visitInsn(Opcodes.DUP2_X2);
        //pop twice
        super.visitInsn(Opcodes.POP2);
        super.visitInsn(Opcodes.POP2);
    }
}
