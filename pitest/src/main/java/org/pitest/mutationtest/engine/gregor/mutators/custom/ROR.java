package org.pitest.mutationtest.engine.gregor.mutators.custom;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.gregor.AbstractJumpMutator;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

/**
 *
 * @author koval
 */
public enum ROR implements MethodMutatorFactory {

    EQUALS_MUTATOR {
        @Override
        public MethodVisitor create(final MutationContext context,
                final MethodInfo methodInfo,
                final MethodVisitor methodVisitor) {
            return new EqualsMethodVisitor(this, context, methodVisitor);
        }
    },
    NOT_EQUALS_MUTATOR {
        @Override
        public MethodVisitor create(final MutationContext context,
                final MethodInfo methodInfo,
                final MethodVisitor methodVisitor) {
            return new NotEqualsMethodVisitor(this, context, methodVisitor);
        }
    },
    LESS_THAN_MUTATOR {
        @Override
        public MethodVisitor create(final MutationContext context,
                final MethodInfo methodInfo,
                final MethodVisitor methodVisitor) {
            return new LessThanMethodVisitor(this, context, methodVisitor);
        }
    },
    LESS_THAN_OR_EQUALS_MUTATOR {
        @Override
        public MethodVisitor create(final MutationContext context,
                final MethodInfo methodInfo,
                final MethodVisitor methodVisitor) {
            return new LessThanOREqualMethodVisitor(this, context,
                    methodVisitor);
        }
    },
    GREATER_THAN_MUTATOR {
        @Override
        public MethodVisitor create(final MutationContext context,
                final MethodInfo methodInfo,
                final MethodVisitor methodVisitor) {
            return new GreaterThanMethodVisitor(this, context, methodVisitor);
        }
    },
    GREATER_THAN_OR_EQUALS_MUTATOR {
        @Override
        public MethodVisitor create(final MutationContext context,
                final MethodInfo methodInfo,
                final MethodVisitor methodVisitor) {
            return new GreaterThanOrEqualsMethodVisitor(this, context,
                    methodVisitor);
        }
    };

    @Override
    public String getGloballyUniqueId() {
        return this.getClass().getName() + "_" + getName();
    }

    @Override
    public String getName() {
        return "ROR_" + name();
    }
}

class EqualsMethodVisitor extends AbstractJumpMutator {

    private static final Map<Integer, Substitution> MUTATIONS = new HashMap<>();

    static {
        //replace != with ==
        MUTATIONS.put(Opcodes.IFNE, new Substitution(Opcodes.IFEQ,
                "ROR: != replaced with =="));
        MUTATIONS.put(Opcodes.IF_ICMPNE, new Substitution(Opcodes.IF_ICMPEQ,
                "ROR: != replaced with =="));

        //replace <= with ==
        MUTATIONS.put(Opcodes.IFLE, new Substitution(Opcodes.IFEQ,
                "ROR: <= replaced with =="));
        MUTATIONS.put(Opcodes.IF_ICMPLE, new Substitution(Opcodes.IF_ICMPEQ,
                "ROR: <= replaced with =="));

        //replace >= with ==
        MUTATIONS.put(Opcodes.IFGE, new Substitution(Opcodes.IFEQ,
                "ROR: >= replaced with =="));
        MUTATIONS.put(Opcodes.IF_ICMPGE, new Substitution(Opcodes.IF_ICMPEQ, 
                "ROR: >= replaced with =="));

        //replace > with ==
        MUTATIONS.put(Opcodes.IFGT, new Substitution(Opcodes.IFEQ, 
                "ROR: > replaced with =="));
        MUTATIONS.put(Opcodes.IF_ICMPGT, new Substitution(Opcodes.IF_ICMPEQ, 
                "ROR: > replaced with =="));

        //replace < with ==
        MUTATIONS.put(Opcodes.IFLT, new Substitution(Opcodes.IFEQ, 
                "ROR: < replaced with =="));
        MUTATIONS.put(Opcodes.IF_ICMPLT, new Substitution(Opcodes.IF_ICMPEQ, 
                "ROR: < replaced with =="));
        
        //replace != with == (reference)
        MUTATIONS.put(Opcodes.IF_ACMPNE, new Substitution(Opcodes.IF_ACMPEQ, 
                "ROR: != replaced with == (reference)"));
        
        //replace != null with == null
        MUTATIONS.put(Opcodes.IFNONNULL, new Substitution(Opcodes.IFNULL,
                "ROR: != NULL replaced with == NULL"));
    }

    public EqualsMethodVisitor(final MethodMutatorFactory factory,
            final MutationContext context,
            final MethodVisitor delegateMethodVisitor) {
        super(factory, context, delegateMethodVisitor);
    }

    @Override
    protected Map<Integer, Substitution> getMutations() {
        return MUTATIONS;
    }
}

class NotEqualsMethodVisitor extends AbstractJumpMutator {

    private static final Map<Integer, Substitution> MUTATIONS = new HashMap<>();

    static {
        //replace == with !=
        MUTATIONS.put(Opcodes.IFEQ, new Substitution(Opcodes.IFNE, 
                "ROR: == replaced with !="));
        MUTATIONS.put(Opcodes.IF_ICMPEQ, new Substitution(Opcodes.IF_ICMPNE, 
                "ROR: == replaced with !="));

        //replace <= with !=
        MUTATIONS.put(Opcodes.IFLE, new Substitution(Opcodes.IFNE, 
                "ROR: <= replaced with !="));
        MUTATIONS.put(Opcodes.IF_ICMPLE, new Substitution(Opcodes.IF_ICMPNE, 
                "ROR: <= replaced with !="));

        //replace >= with !=
        MUTATIONS.put(Opcodes.IFGE, new Substitution(Opcodes.IFNE, 
                "ROR: >= replaced with !="));
        MUTATIONS.put(Opcodes.IF_ICMPGE, new Substitution(Opcodes.IF_ICMPNE, 
                "ROR: >= replaced with !="));

        //replace > with !=
        MUTATIONS.put(Opcodes.IFGT, new Substitution(Opcodes.IFNE, 
                "ROR: > replaced with !="));
        MUTATIONS.put(Opcodes.IF_ICMPGT, new Substitution(Opcodes.IF_ICMPNE, 
                "ROR: > replaced with !="));

        //replace < with !=
        MUTATIONS.put(Opcodes.IFLT, new Substitution(Opcodes.IFNE, 
                "ROR: < replaced with !="));
        MUTATIONS.put(Opcodes.IF_ICMPLT, new Substitution(Opcodes.IF_ICMPNE, 
                "ROR: < replaced with !="));
        
        //replace == with != (reference)
        MUTATIONS.put(Opcodes.IF_ACMPEQ, new Substitution(Opcodes.IF_ACMPNE, 
                "ROR: != replaced with == (reference)"));
        
        //replace == null with != null
        MUTATIONS.put(Opcodes.IFNULL, new Substitution(Opcodes.IFNONNULL,
                "ROR: != NULL replaced with == NULL"));
    }

    public NotEqualsMethodVisitor(final MethodMutatorFactory factory,
            final MutationContext context,
            final MethodVisitor delegateMethodVisitor) {
        super(factory, context, delegateMethodVisitor);
    }

    @Override
    protected Map<Integer, Substitution> getMutations() {
        return MUTATIONS;
    }
}

class LessThanMethodVisitor extends AbstractJumpMutator {

    private static final Map<Integer, Substitution> MUTATIONS = new HashMap<>();

    static {
        //replace != with <
        MUTATIONS.put(Opcodes.IFNE, new Substitution(Opcodes.IFLT, 
                "ROR: != replaced with <"));
        MUTATIONS.put(Opcodes.IF_ICMPNE, new Substitution(Opcodes.IF_ICMPLT, 
                "ROR: != replaced with <"));

        //replace <= with <
        MUTATIONS.put(Opcodes.IFEQ, new Substitution(Opcodes.IFLT, 
                "ROR: <= replaced with <"));
        MUTATIONS.put(Opcodes.IF_ICMPEQ, new Substitution(Opcodes.IF_ICMPLT, 
                "ROR: <= replaced with <"));

        //replace >= with <
        MUTATIONS.put(Opcodes.IFGE, new Substitution(Opcodes.IFLT, 
                "ROR: >= replaced with <"));
        MUTATIONS.put(Opcodes.IF_ICMPGE, new Substitution(Opcodes.IF_ICMPLT, 
                "ROR: >= replaced with <"));

        //replace > with <
        MUTATIONS.put(Opcodes.IFGT, new Substitution(Opcodes.IFLT, 
                "ROR: > replaced with <"));
        MUTATIONS.put(Opcodes.IF_ICMPGT, new Substitution(Opcodes.IF_ICMPLT, 
                "ROR: > replaced with <"));

        //replace <= with <
        MUTATIONS.put(Opcodes.IFLE, new Substitution(Opcodes.IFLT, 
                "ROR: <= replaced with <"));
        MUTATIONS.put(Opcodes.IF_ICMPLE, new Substitution(Opcodes.IF_ICMPLT, 
                "ROR: <= replaced with <"));
    }

    public LessThanMethodVisitor(final MethodMutatorFactory factory,
            final MutationContext context,
            final MethodVisitor delegateMethodVisitor) {
        super(factory, context, delegateMethodVisitor);
    }

    @Override
    protected Map<Integer, Substitution> getMutations() {
        return MUTATIONS;
    }
}

class LessThanOREqualMethodVisitor extends AbstractJumpMutator {

    private static final Map<Integer, Substitution> MUTATIONS = new HashMap<>();

    static {
        //replace != with <=
        MUTATIONS.put(Opcodes.IFNE, new Substitution(Opcodes.IFLE, 
                "ROR: != replaced with <="));
        MUTATIONS.put(Opcodes.IF_ICMPNE, new Substitution(Opcodes.IF_ICMPLE, 
                "ROR: != replaced with <="));

        //replace == with <=
        MUTATIONS.put(Opcodes.IFEQ, new Substitution(Opcodes.IFLE, 
                "ROR: == replaced with <="));
        MUTATIONS.put(Opcodes.IF_ICMPEQ, new Substitution(Opcodes.IF_ICMPLE, 
                "ROR: == replaced with <="));

        //replace >= with <=
        MUTATIONS.put(Opcodes.IFGE, new Substitution(Opcodes.IFLE, 
                "ROR: >= replaced with <="));
        MUTATIONS.put(Opcodes.IF_ICMPGE, new Substitution(Opcodes.IF_ICMPLE, 
                "ROR: >= replaced with <="));

        //replace > with <=
        MUTATIONS.put(Opcodes.IFGT, new Substitution(Opcodes.IFLE, 
                "ROR: > replaced with <="));
        MUTATIONS.put(Opcodes.IF_ICMPGT, new Substitution(Opcodes.IF_ICMPLE, 
                "ROR: > replaced with <="));

        //replace < with <=
        MUTATIONS.put(Opcodes.IFLT, new Substitution(Opcodes.IFLE, 
                "ROR: < replaced with <="));
        MUTATIONS.put(Opcodes.IF_ICMPLT, new Substitution(Opcodes.IF_ICMPLE, 
                "ROR: < replaced with <="));
    }

    public LessThanOREqualMethodVisitor(final MethodMutatorFactory factory,
            final MutationContext context,
            final MethodVisitor delegateMethodVisitor) {
        super(factory, context, delegateMethodVisitor);
    }

    @Override
    protected Map<Integer, Substitution> getMutations() {
        return MUTATIONS;
    }
}

class GreaterThanMethodVisitor extends AbstractJumpMutator {
    
    private static final Map<Integer, Substitution> MUTATIONS = new HashMap<>();

    static {
        //replace != with >
        MUTATIONS.put(Opcodes.IFNE, new Substitution(Opcodes.IFGT, 
                "ROR: != replaced with >"));
        MUTATIONS.put(Opcodes.IF_ICMPNE, new Substitution(Opcodes.IF_ICMPGT, 
                "ROR: != replaced with >"));

        //replace <= with >
        MUTATIONS.put(Opcodes.IFLE, new Substitution(Opcodes.IFGT, 
                "ROR: <= replaced with >"));
        MUTATIONS.put(Opcodes.IF_ICMPLE, new Substitution(Opcodes.IF_ICMPGT, 
                "ROR: <= replaced with >"));

        //replace >= with >
        MUTATIONS.put(Opcodes.IFGE, new Substitution(Opcodes.IFGT, 
                "ROR: >= replaced with >"));
        MUTATIONS.put(Opcodes.IF_ICMPGE, new Substitution(Opcodes.IF_ICMPGT, 
                "ROR: >= replaced with >"));

        //replace == with >
        MUTATIONS.put(Opcodes.IFEQ, new Substitution(Opcodes.IFGT, 
                "ROR: == replaced with >"));
        MUTATIONS.put(Opcodes.IF_ICMPEQ, new Substitution(Opcodes.IF_ICMPGT, 
                "ROR: == replaced with >"));

        //replace < with >
        MUTATIONS.put(Opcodes.IFLT, new Substitution(Opcodes.IFGT, 
                "ROR: < replaced with >"));
        MUTATIONS.put(Opcodes.IF_ICMPLT, new Substitution(Opcodes.IF_ICMPGT, 
                "ROR: < replaced with >"));
    }

    public GreaterThanMethodVisitor(final MethodMutatorFactory factory,
            final MutationContext context,
            final MethodVisitor delegateMethodVisitor) {
        super(factory, context, delegateMethodVisitor);
    }

    @Override
    protected Map<Integer, Substitution> getMutations() {
        return MUTATIONS;
    }
}

class GreaterThanOrEqualsMethodVisitor extends AbstractJumpMutator {

    private static final Map<Integer, Substitution> MUTATIONS = new HashMap<>();

    static {
        //replace != with >=
        MUTATIONS.put(Opcodes.IFNE, new Substitution(Opcodes.IFGE, 
                "ROR: != replaced with >="));
        MUTATIONS.put(Opcodes.IF_ICMPNE, new Substitution(Opcodes.IF_ICMPGE, 
                "ROR: != replaced with >="));

        //replace <= with >=
        MUTATIONS.put(Opcodes.IFLE, new Substitution(Opcodes.IFGE, 
                "ROR: <= replaced with >="));
        MUTATIONS.put(Opcodes.IF_ICMPLE, new Substitution(Opcodes.IF_ICMPGE, 
                "ROR: <= replaced with >="));

        //replace == with >=
        MUTATIONS.put(Opcodes.IFEQ, new Substitution(Opcodes.IFGE, 
                "ROR: == replaced with >="));
        MUTATIONS.put(Opcodes.IF_ICMPEQ, new Substitution(Opcodes.IF_ICMPGE, 
                "ROR: == replaced with >="));

        //replace > with >=
        MUTATIONS.put(Opcodes.IFGT, new Substitution(Opcodes.IFGE, 
                "ROR: > replaced with >="));
        MUTATIONS.put(Opcodes.IF_ICMPGT, new Substitution(Opcodes.IF_ICMPGE, 
                "ROR: > replaced with >="));

        //replace < with >=
        MUTATIONS.put(Opcodes.IFLT, new Substitution(Opcodes.IFGE, 
                "ROR: < replaced with >="));
        MUTATIONS.put(Opcodes.IF_ICMPLT, new Substitution(Opcodes.IF_ICMPGE, 
                "ROR: < replaced with >="));
    }

    public GreaterThanOrEqualsMethodVisitor(final MethodMutatorFactory factory,
            final MutationContext context,
            final MethodVisitor delegateMethodVisitor) {
        super(factory, context, delegateMethodVisitor);
    }

    @Override
    protected Map<Integer, Substitution> getMutations() {
        return MUTATIONS;
    }
}
