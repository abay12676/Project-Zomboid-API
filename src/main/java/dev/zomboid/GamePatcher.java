package dev.zomboid;

import dev.zomboid.util.Inject;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Handles patching the game to install/uninstall mods.
 */
@RequiredArgsConstructor
public class GamePatcher {

    private final ZomboidClassPath cp;

    /**
     * The descriptor of the annotation to inject into modified classes.
     */
    private static final String INJECTED_ANNOTATION = "Ldev/zomboid/Injected;";

    /**
     * Files to restore when uninstalling.
     */
    private static final String[] RESTORATION_FILES = {
           "zombie/core/Core",
            "zombie/network/GameClient",
            "zombie/network/GameServer",
            "zombie/core/raknet/UdpEngine",
    };

    /**
     * Marks a class as injected by putting an injected annotation on it.
     */
    private void markInjected(ClassNode cl) {
        if (cl.visibleAnnotations == null) {
            cl.visibleAnnotations = new LinkedList<>();
        }

        cl.visibleAnnotations.add(new AnnotationNode(INJECTED_ANNOTATION));
    }

    /**
     * Injects code into the Core class.
     */
    private boolean injectCore() throws IOException {
        String name = "zombie/core/Core";
        ClassNode cl = cp.readClass(name);
        markInjected(cl);

        MethodNode mt = Inject.findMethod(cl, "EndFrameUI", "()V");
        Inject.injectVirtualCallsBegin(mt, "dev/zomboid/interp/RenderingStub", "endFrameUi", "(Lzombie/core/Core;)V");
        cp.replaceClass(name, cl);
        return true;
    }

    /**
     * Injects code into the GameClient class.
     */
    private boolean injectGameClient() throws IOException {
        String name = "zombie/network/GameClient";
        ClassNode cl = cp.readClass(name);
        markInjected(cl);

        MethodNode addIncoming = Inject.findMethod(cl, "addIncoming", "(SLjava/nio/ByteBuffer;)V");
        addIncoming.instructions.insert(new MethodInsnNode(Opcodes.INVOKESTATIC, "dev/zomboid/interp/NetworkingStub", "addIncomingClient", "(SLjava/nio/ByteBuffer;)V"));
        addIncoming.instructions.insert(new VarInsnNode(Opcodes.ALOAD, 2));
        addIncoming.instructions.insert(new VarInsnNode(Opcodes.ILOAD, 1));

        cp.replaceClass(name, cl);
        return true;
    }

    /**
     * Injects code into the UdpEngine class.
     */
    private boolean injectUdpEngine() throws IOException {
        String name = "zombie/core/raknet/UdpEngine";
        ClassNode cl = cp.readClass(name);
        markInjected(cl);

        MethodNode decode = Inject.findMethod(cl, "decode", "(Ljava/nio/ByteBuffer;)V");
        decode.instructions.insert(new MethodInsnNode(Opcodes.INVOKESTATIC, "dev/zomboid/interp/NetworkingStub", "decode", "(Lzombie/core/raknet/UdpEngine;Ljava/nio/ByteBuffer;)V"));
        decode.instructions.insert(new VarInsnNode(Opcodes.ALOAD, 1));
        decode.instructions.insert(new VarInsnNode(Opcodes.ALOAD, 0));

        cp.replaceClass(name, cl);
        return true;
    }

    /**
     * Injects code into the GameServer class.
     */
    private boolean injectGameServer() throws IOException {
        String name = "zombie/network/GameServer";
        ClassNode cl = cp.readClass(name);
        markInjected(cl);

        MethodNode addIncoming = Inject.findMethod(cl, "addIncoming", "(SLjava/nio/ByteBuffer;Lzombie/core/raknet/UdpConnection;)V");
        MethodNode main = Inject.findMethod(cl, "main", "([Ljava/lang/String;)V");

        addIncoming.instructions.insert(new MethodInsnNode(Opcodes.INVOKESTATIC, "dev/zomboid/interp/NetworkingStub", "addIncomingServer", "(SLjava/nio/ByteBuffer;Lzombie/core/raknet/UdpConnection;)V"));
        addIncoming.instructions.insert(new VarInsnNode(Opcodes.ALOAD, 2));
        addIncoming.instructions.insert(new VarInsnNode(Opcodes.ALOAD, 1));
        addIncoming.instructions.insert(new VarInsnNode(Opcodes.ILOAD, 0));

        Inject.injectStaticCallsBegin(main, "dev/zomboid/interp/CoreStub", "serverMain", "()V");

        cp.replaceClass(name, cl);
        return true;
    }

    /**
     * Extracts the currently running jar into the currently running directory. This will
     * allow the game to access our classes to talk to us, as the entire folder is loaded
     * as a classpath for the game.
     *
     * @param path the path to the jar file.
     */
    private void extractSelf(String path) throws IOException {
        ZipInputStream zis = new ZipInputStream(new FileInputStream(path));
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] tmp = new byte[4096];
            int read;
            while ((read = zis.read(tmp)) != -1) {
                bos.write(tmp, 0, read);
            }

            Path p = Paths.get(entry.getName());
            if (entry.isDirectory()) {
                Files.createDirectories(p);
            } else {
                if (p.getParent() != null) {
                    Files.createDirectories(p.getParent());
                }

                Files.write(Paths.get(entry.getName()), bos.toByteArray());
            }
        }
    }

    /**
     * Attempts to install the mod on top of the game.
     */
    public void install(GamePatcherCfg cfg) throws IOException {
        System.out.println("Removing old installation");
        uninstall();

        System.out.println("Injecting into Core");
        injectCore();

        System.out.println("Injecting into UdpEngine");
        injectUdpEngine();

        if (cfg.isClient()) {
            System.out.println("Injecting into GameClient");
            injectGameClient();
        }

        if (cfg.isServer()) {
            System.out.println("Injecting into GameServer");
            injectGameServer();
        }

        System.out.println("Extracting dependencies from self");
        extractSelf(getClass().getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getPath());

        System.out.println("Done!");
    }

    private void removeBackup(String name) throws IOException {
        Path modified = Paths.get(name + ".class");
        Path backup = Paths.get(name + ".class.bkup");
        if (Files.exists(modified) && Files.exists(backup)) {
            Files.delete(modified);
            Files.move(backup, modified);
        }
    }

    /**
     * Attempts to uninstall any existing mod files
     */
    public void uninstall() throws IOException {
        for (String s : RESTORATION_FILES) {
            System.out.println("Replacing '" + s + "' with backup");
            removeBackup(s);
        }
    }
}
