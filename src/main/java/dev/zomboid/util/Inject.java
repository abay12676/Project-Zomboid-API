package dev.zomboid.util;

import lombok.experimental.UtilityClass;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

/**
 * Provides utilities to inject into Java bytecode.
 */
@UtilityClass
public class Inject {

    /**
     * Finds a method by its name.
     */
    public static MethodNode findMethod(ClassNode node, String name, String desc) {
        for (MethodNode method : node.methods) {
            if (method.name.equals(name) && method.desc.equals(desc)) {
                return method;
            }
        }
        return null;
    }

    /**
     * Injects a method call before each return in the method. The current object will be passed
     * to the called function.
     *
     * @param node  The method to inject into.
     * @param owner The owner of the method to call.
     * @param name  The name of the method to call.
     * @param desc  The description of the method to call.
     */
    public static void injectVirtualCallsBeforeReturns(MethodNode node, String owner, String name, String desc) {
        for (AbstractInsnNode ain : node.instructions.toArray()) {
            if (ain.getOpcode() >= Opcodes.IRETURN && ain.getOpcode() <= Opcodes.RETURN) {
                node.instructions.insertBefore(ain, new VarInsnNode(Opcodes.ALOAD, 0));
                node.instructions.insertBefore(ain, new MethodInsnNode(Opcodes.INVOKESTATIC, owner, name, desc));
            }
        }
    }

    /**
     * Injects a method call before each return in the method.
     *
     * @param node  The method to inject into.
     * @param owner The owner of the method to call.
     * @param name  The name of the method to call.
     * @param desc  The description of the method to call.
     */
    public static void injectStaticCallsBeforeReturns(MethodNode node, String owner, String name, String desc) {
        for (AbstractInsnNode ain : node.instructions.toArray()) {
            if (ain.getOpcode() >= Opcodes.IRETURN && ain.getOpcode() <= Opcodes.RETURN) {
                node.instructions.insertBefore(ain, new MethodInsnNode(Opcodes.INVOKESTATIC, owner, name, desc));
            }
        }
    }

    /**
     * Injects a method call at the beginning of a method. The current object will be passed
     * to the called function.
     *
     * @param node  The method to inject into.
     * @param owner The owner of the method to call.
     * @param name  The name of the method to call.
     * @param desc  The description of the method to call.
     */
    public static void injectVirtualCallsBegin(MethodNode node, String owner, String name, String desc) {
        node.instructions.insert(new MethodInsnNode(Opcodes.INVOKESTATIC, owner, name, desc));
        node.instructions.insert(new VarInsnNode(Opcodes.ALOAD, 0));
    }

    /**
     * Injects a method call at the beginning of a method.
     *
     * @param node  The method to inject into.
     * @param owner The owner of the method to call.
     * @param name  The name of the method to call.
     * @param desc  The description of the method to call.
     */
    public static void injectStaticCallsBegin(MethodNode node, String owner, String name, String desc) {
        node.instructions.insert(new MethodInsnNode(Opcodes.INVOKESTATIC, owner, name, desc));
    }
}
