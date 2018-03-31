package org.pitest.mutationtest.engine.gregor.mutators.custom;

import java.util.HashMap;
import java.util.Map;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.gregor.AbstractInsnMutator;
import org.pitest.mutationtest.engine.gregor.InsnSubstitution;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;
import org.pitest.mutationtest.engine.gregor.ZeroOperandMutation;

/**
 *
 * @author koval
 */
public enum AOR implements MethodMutatorFactory {
    
    SUBTRACTION_MUTATOR {
        @Override
        public MethodVisitor create(final MutationContext context, 
                                    final MethodInfo methodInfo, 
                                    final MethodVisitor methodVisitor) {
            return new SubtractionMethodVisitor(this, methodInfo, context, 
                    methodVisitor);
        }       
    },
    ADDITION_MUTATOR {
        @Override
        public MethodVisitor create(final MutationContext context, 
                                    final MethodInfo methodInfo, 
                                    final MethodVisitor methodVisitor) {
            return new AdditionMethodVisitor(this, methodInfo, context, 
                    methodVisitor);
        }       
    },
    MULTIPLICATION_MUTATOR {
        @Override
        public MethodVisitor create(final MutationContext context, 
                                    final MethodInfo methodInfo, 
                                    final MethodVisitor methodVisitor) {
            return new MultiplicationMethodVisitor(this, methodInfo, context, 
                    methodVisitor);
        }       
    },
    DIVISION_MUTATOR {
        @Override
        public MethodVisitor create(final MutationContext context, 
                                    final MethodInfo methodInfo, 
                                    final MethodVisitor methodVisitor) {
            return new DivisionMethodVisitor(this, methodInfo, context, 
                    methodVisitor);
        }       
    },
    MODULUS_MUTATOR {
        @Override
        public MethodVisitor create(final MutationContext context, 
                                    final MethodInfo methodInfo, 
                                    final MethodVisitor methodVisitor) {
            return new ModulusMethodVisitor(this, methodInfo, context, 
                    methodVisitor);
        }       
    };
    
    @Override
    public String getGloballyUniqueId() {
        return this.getClass().getName() + "_" + getName();
    }
    
    @Override
    public String getName() {
        return "AOR_" + name();
    }
}

class SubtractionMethodVisitor extends AbstractInsnMutator {
    
    public SubtractionMethodVisitor(final MethodMutatorFactory factory,
                                    final MethodInfo methodInfo,
                                    final MutationContext context,
                                    final MethodVisitor writer) {
        super(factory, methodInfo, context, writer);
    }

    private static final Map<Integer, ZeroOperandMutation> MUTATIONS = 
            new HashMap<>();
    
    static {
        // ints
        MUTATIONS.put(Opcodes.IADD, new InsnSubstitution(Opcodes.ISUB,
            "AOR: Replaced integer addition with subtraction"));
        MUTATIONS.put(Opcodes.IMUL, new InsnSubstitution(Opcodes.ISUB,
            "AOR: Replaced integer multiplication with subtraction"));
        MUTATIONS.put(Opcodes.IDIV, new InsnSubstitution(Opcodes.ISUB,
            "AOR: Replaced integer division with subtraction"));
        MUTATIONS.put(Opcodes.IREM, new InsnSubstitution(Opcodes.ISUB,
            "AOR: Replaced integer modulus with subtraction"));

        // longs
        MUTATIONS.put(Opcodes.LADD, new InsnSubstitution(Opcodes.LSUB,
            "AOR: Replaced long addition with subtraction"));
        MUTATIONS.put(Opcodes.LMUL, new InsnSubstitution(Opcodes.LSUB,
            "AOR: Replaced long multiplication with subtraction"));
        MUTATIONS.put(Opcodes.LDIV, new InsnSubstitution(Opcodes.LSUB,
            "AOR: Replaced long division with subtraction"));
        MUTATIONS.put(Opcodes.LREM, new InsnSubstitution(Opcodes.LSUB,
            "AOR: Replaced long modulus with subtraction"));

        // floats
        MUTATIONS.put(Opcodes.FADD, new InsnSubstitution(Opcodes.FSUB,
            "AOR: Replaced float addition with subtraction"));
        MUTATIONS.put(Opcodes.FMUL, new InsnSubstitution(Opcodes.FSUB,
            "AOR: Replaced float multiplication with subtraction"));
        MUTATIONS.put(Opcodes.FDIV, new InsnSubstitution(Opcodes.FSUB,
            "AOR: Replaced float division with subtraction"));
        MUTATIONS.put(Opcodes.FREM, new InsnSubstitution(Opcodes.FSUB,
            "AOR: Replaced float modulus with subtraction"));

        // doubles
        MUTATIONS.put(Opcodes.DADD, new InsnSubstitution(Opcodes.DSUB,
            "AOR: Replaced double addition with subtraction"));
        MUTATIONS.put(Opcodes.DMUL, new InsnSubstitution(Opcodes.DSUB,
            "AOR: Replaced double multiplication with subtraction"));
        MUTATIONS.put(Opcodes.DDIV, new InsnSubstitution(Opcodes.DSUB,
            "AOR: Replaced double division with subtraction"));
        MUTATIONS.put(Opcodes.DREM, new InsnSubstitution(Opcodes.DSUB,
            "AOR: Replaced double modulus with subtraction"));
    }
    
    @Override
    protected Map<Integer, ZeroOperandMutation> getMutations() {
        return MUTATIONS;
    }
}

class AdditionMethodVisitor extends AbstractInsnMutator {

    public AdditionMethodVisitor(final MethodMutatorFactory factory,
                                    final MethodInfo methodInfo,
                                    final MutationContext context,
                                    final MethodVisitor writer) {
        super(factory, methodInfo, context, writer);
    }
    
    private static final Map<Integer, ZeroOperandMutation> MUTATIONS = 
            new HashMap<>();
    
    static {
        // ints
        MUTATIONS.put(Opcodes.ISUB, new InsnSubstitution(Opcodes.IADD,
            "AOR: Replaced integer subtraction with addition"));
        MUTATIONS.put(Opcodes.IMUL, new InsnSubstitution(Opcodes.IADD,
            "AOR: Replaced integer multiplication with addition"));
        MUTATIONS.put(Opcodes.IDIV, new InsnSubstitution(Opcodes.IADD,
            "AOR: Replaced integer division with addition"));
        MUTATIONS.put(Opcodes.IREM, new InsnSubstitution(Opcodes.IADD,
            "AOR: Replaced integer modulus with addition"));

        // longs
        MUTATIONS.put(Opcodes.LSUB, new InsnSubstitution(Opcodes.LADD,
            "AOR: Replaced long subtraction with addition"));
        MUTATIONS.put(Opcodes.LMUL, new InsnSubstitution(Opcodes.LADD,
            "AOR: Replaced long multiplication with addition"));
        MUTATIONS.put(Opcodes.LDIV, new InsnSubstitution(Opcodes.LADD,
            "AOR: Replaced long division with addition"));
        MUTATIONS.put(Opcodes.LREM, new InsnSubstitution(Opcodes.LADD,
            "AOR: Replaced long modulus with addition"));

        // floats
        MUTATIONS.put(Opcodes.FSUB, new InsnSubstitution(Opcodes.FADD,
            "AOR: Replaced float subtraction with addition"));
        MUTATIONS.put(Opcodes.FMUL, new InsnSubstitution(Opcodes.FADD,
            "AOR: Replaced float multiplication with addition"));
        MUTATIONS.put(Opcodes.FDIV, new InsnSubstitution(Opcodes.FADD,
            "AOR: Replaced float division with addition"));
        MUTATIONS.put(Opcodes.FREM, new InsnSubstitution(Opcodes.FADD,
            "AOR: Replaced float modulus with addition"));

        // doubles
        MUTATIONS.put(Opcodes.DSUB, new InsnSubstitution(Opcodes.DADD,
            "AOR: Replaced double subtraction with addition"));
        MUTATIONS.put(Opcodes.DMUL, new InsnSubstitution(Opcodes.DADD,
            "AOR: Replaced double multiplication with addition"));
        MUTATIONS.put(Opcodes.DDIV, new InsnSubstitution(Opcodes.DADD,
            "AOR: Replaced double division with addition"));
        MUTATIONS.put(Opcodes.DREM, new InsnSubstitution(Opcodes.DADD,
            "AOR: Replaced double modulus with addition"));
    }
    
    @Override
    protected Map<Integer, ZeroOperandMutation> getMutations() {
        return MUTATIONS;
    }
}

class MultiplicationMethodVisitor extends AbstractInsnMutator {

    public MultiplicationMethodVisitor(final MethodMutatorFactory factory,
                                    final MethodInfo methodInfo,
                                    final MutationContext context,
                                    final MethodVisitor writer) {
        super(factory, methodInfo, context, writer);
    }
    
    private static final Map<Integer, ZeroOperandMutation> MUTATIONS = 
            new HashMap<>();
    
    static {
        // ints
        MUTATIONS.put(Opcodes.IADD, new InsnSubstitution(Opcodes.IMUL,
            "AOR: Replaced integer addition with multiplication"));
        MUTATIONS.put(Opcodes.ISUB, new InsnSubstitution(Opcodes.IMUL,
            "AOR: Replaced integer subtraction with multiplication"));
        MUTATIONS.put(Opcodes.IDIV, new InsnSubstitution(Opcodes.IMUL,
            "AOR: Replaced integer division with multiplication"));
        MUTATIONS.put(Opcodes.IREM, new InsnSubstitution(Opcodes.IMUL,
            "AOR: Replaced integer modulus with multiplication"));

        // longs
        MUTATIONS.put(Opcodes.LADD, new InsnSubstitution(Opcodes.LMUL,
            "AOR: Replaced long addition with multiplication"));
        MUTATIONS.put(Opcodes.LSUB, new InsnSubstitution(Opcodes.LMUL,
            "AOR: Replaced long subtraction with multiplication"));
        MUTATIONS.put(Opcodes.LDIV, new InsnSubstitution(Opcodes.LMUL,
            "AOR: Replaced long division with multiplication"));
        MUTATIONS.put(Opcodes.LREM, new InsnSubstitution(Opcodes.LMUL,
            "AOR: Replaced long modulus with multiplication"));

        // floats
        MUTATIONS.put(Opcodes.FADD, new InsnSubstitution(Opcodes.FMUL,
            "AOR: Replaced float addition with multiplication"));
        MUTATIONS.put(Opcodes.FSUB, new InsnSubstitution(Opcodes.FMUL,
            "AOR: Replaced float subtraction with multiplication"));
        MUTATIONS.put(Opcodes.FDIV, new InsnSubstitution(Opcodes.FMUL,
            "AOR: Replaced float division with multiplication"));
        MUTATIONS.put(Opcodes.FREM, new InsnSubstitution(Opcodes.FMUL,
            "AOR: Replaced float modulus with multiplication"));

        // doubles
        MUTATIONS.put(Opcodes.DADD, new InsnSubstitution(Opcodes.DMUL,
            "AOR: Replaced double addition with multiplication"));
        MUTATIONS.put(Opcodes.DSUB, new InsnSubstitution(Opcodes.DMUL,
            "AOR: Replaced double subtraction with multiplication"));
        MUTATIONS.put(Opcodes.DDIV, new InsnSubstitution(Opcodes.DMUL,
            "AOR: Replaced double division with multiplication"));
        MUTATIONS.put(Opcodes.DREM, new InsnSubstitution(Opcodes.DMUL,
            "AOR: Replaced double modulus with multiplication"));
    }
    
    @Override
    protected Map<Integer, ZeroOperandMutation> getMutations() {
        return MUTATIONS;
    }
}

class DivisionMethodVisitor extends AbstractInsnMutator {

    public DivisionMethodVisitor(final MethodMutatorFactory factory,
                                    final MethodInfo methodInfo,
                                    final MutationContext context,
                                    final MethodVisitor writer) {
        super(factory, methodInfo, context, writer);
    }
    
    private static final Map<Integer, ZeroOperandMutation> MUTATIONS = 
            new HashMap<>();
    
    static {
        // ints
        MUTATIONS.put(Opcodes.IADD, new InsnSubstitution(Opcodes.IDIV,
            "AOR: Replaced integer addition with division"));
        MUTATIONS.put(Opcodes.IMUL, new InsnSubstitution(Opcodes.IDIV,
            "AOR: Replaced integer multiplication with division"));
        MUTATIONS.put(Opcodes.ISUB, new InsnSubstitution(Opcodes.IDIV,
            "AOR: Replaced integer subtraction with division"));
        MUTATIONS.put(Opcodes.IREM, new InsnSubstitution(Opcodes.IDIV,
            "AOR: Replaced integer modulus with division"));

        // longs
        MUTATIONS.put(Opcodes.LADD, new InsnSubstitution(Opcodes.LDIV,
            "AOR: Replaced long addition with division"));
        MUTATIONS.put(Opcodes.LMUL, new InsnSubstitution(Opcodes.LDIV,
            "AOR: Replaced long multiplication with division"));
        MUTATIONS.put(Opcodes.LSUB, new InsnSubstitution(Opcodes.LDIV,
            "AOR: Replaced long subtraction with division"));
        MUTATIONS.put(Opcodes.LREM, new InsnSubstitution(Opcodes.LDIV,
            "AOR: Replaced long modulus with division"));

        // floats
        MUTATIONS.put(Opcodes.FADD, new InsnSubstitution(Opcodes.FDIV,
            "AOR: Replaced float addition with division"));
        MUTATIONS.put(Opcodes.FMUL, new InsnSubstitution(Opcodes.FDIV,
            "AOR: Replaced float multiplication with division"));
        MUTATIONS.put(Opcodes.FSUB, new InsnSubstitution(Opcodes.FDIV,
            "AOR: Replaced float subtraction with division"));
        MUTATIONS.put(Opcodes.FREM, new InsnSubstitution(Opcodes.FDIV,
            "AOR: Replaced float modulus with division"));

        // doubles
        MUTATIONS.put(Opcodes.DADD, new InsnSubstitution(Opcodes.DDIV,
            "AOR: Replaced double addition with division"));
        MUTATIONS.put(Opcodes.DMUL, new InsnSubstitution(Opcodes.DDIV,
            "AOR: Replaced double multiplication with division"));
        MUTATIONS.put(Opcodes.DSUB, new InsnSubstitution(Opcodes.DDIV,
            "AOR: Replaced double subtraction with division"));
        MUTATIONS.put(Opcodes.DREM, new InsnSubstitution(Opcodes.DDIV,
            "AOR: Replaced double modulus with division"));
    }
    
    @Override
    protected Map<Integer, ZeroOperandMutation> getMutations() {
        return MUTATIONS;
    }
}

class ModulusMethodVisitor extends AbstractInsnMutator {

    public ModulusMethodVisitor(final MethodMutatorFactory factory,
                                    final MethodInfo methodInfo,
                                    final MutationContext context,
                                    final MethodVisitor writer) {
        super(factory, methodInfo, context, writer);
    }
    
    private static final Map<Integer, ZeroOperandMutation> MUTATIONS = 
            new HashMap<>();
    
    static {
        // ints
        MUTATIONS.put(Opcodes.IADD, new InsnSubstitution(Opcodes.IREM,
            "AOR: Replaced integer addition with modulus"));
        MUTATIONS.put(Opcodes.IMUL, new InsnSubstitution(Opcodes.IREM,
            "AOR: Replaced integer multiplication with modulus"));
        MUTATIONS.put(Opcodes.IDIV, new InsnSubstitution(Opcodes.IREM,
            "AOR: Replaced integer division with modulus"));
        MUTATIONS.put(Opcodes.ISUB, new InsnSubstitution(Opcodes.IREM,
            "AOR: Replaced integer subtraction with modulus"));

        // longs
        MUTATIONS.put(Opcodes.LADD, new InsnSubstitution(Opcodes.LREM,
            "AOR: Replaced long addition with modulus"));
        MUTATIONS.put(Opcodes.LMUL, new InsnSubstitution(Opcodes.LREM,
            "AOR: Replaced long multiplication with modulus"));
        MUTATIONS.put(Opcodes.LDIV, new InsnSubstitution(Opcodes.LREM,
            "AOR: Replaced long division with modulus"));
        MUTATIONS.put(Opcodes.LSUB, new InsnSubstitution(Opcodes.LREM,
            "AOR: Replaced long subtraction with modulus"));

        // floats
        MUTATIONS.put(Opcodes.FADD, new InsnSubstitution(Opcodes.FREM,
            "AOR: Replaced float addition with modulus"));
        MUTATIONS.put(Opcodes.FMUL, new InsnSubstitution(Opcodes.FREM,
            "AOR: Replaced float multiplication with modulus"));
        MUTATIONS.put(Opcodes.FDIV, new InsnSubstitution(Opcodes.FREM,
            "AOR: Replaced float division with modulus"));
        MUTATIONS.put(Opcodes.FSUB, new InsnSubstitution(Opcodes.FREM,
            "AOR: Replaced float subtraction with modulus"));

        // doubles
        MUTATIONS.put(Opcodes.DADD, new InsnSubstitution(Opcodes.DREM,
            "AOR: Replaced double addition with modulus"));
        MUTATIONS.put(Opcodes.DMUL, new InsnSubstitution(Opcodes.DREM,
            "AOR: Replaced double multiplication with modulus"));
        MUTATIONS.put(Opcodes.DDIV, new InsnSubstitution(Opcodes.DREM,
            "AOR: Replaced double division with modulus"));
        MUTATIONS.put(Opcodes.DSUB, new InsnSubstitution(Opcodes.DREM,
            "AOR: Replaced double subtraction with modulus"));
    }
    
    @Override
    protected Map<Integer, ZeroOperandMutation> getMutations() {
        return MUTATIONS;
    }
}
