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

@RequiredArgsConstructor
public class GamePatcher {

    private final ZomboidClassPath cp;

    /**
     * Injects code into the Core class.
     */
    private boolean injectCore() throws IOException {
        ClassNode cl = cp.readClass("zombie/core/Core");
        cl.visibleAnnotations = new LinkedList<>();
        cl.visibleAnnotations.add(new AnnotationNode("Ldev/zomboid/Injected;"));

        MethodNode mt = Inject.findMethod(cl, "EndFrameUI", "()V");
        if (mt == null) {
            System.out.println("Failed to find zombie/core/Core#EndFrameUI()V");
            return false;
        }

        Inject.injectVirtualCallsBegin(mt, "dev/zomboid/interp/RenderingStub", "endFrameUi", "(Lzombie/core/Core;)V");
        cp.replaceClass("zombie/core/Core", cl);
        return true;
    }

    /**
     * Injects code into the GameServer class.
     */
    private boolean injectGameServer() throws IOException {
        ClassNode cl = cp.readClass("zombie/network/GameServer");
        cl.visibleAnnotations = new LinkedList<>();
        cl.visibleAnnotations.add(new AnnotationNode("Ldev/zomboid/Injected;"));

        MethodNode addIncoming = Inject.findMethod(cl, "addIncoming", "(SLjava/nio/ByteBuffer;Lzombie/core/raknet/UdpConnection;)V");
        if (addIncoming == null) {
            System.out.println("Failed to find zombie/network/GameServer#addIncoming()V");
            return false;
        }

        MethodNode main = Inject.findMethod(cl, "main", "([Ljava/lang/String;)V");
        if (main == null) {
            System.out.println("Failed to find zombie/network/GameServer#main()V");
            return false;
        }

        addIncoming.instructions.insert(new MethodInsnNode(Opcodes.INVOKESTATIC, "dev/zomboid/interp/NetworkingStub", "addIncoming", "(SLjava/nio/ByteBuffer;Lzombie/core/raknet/UdpConnection;)V"));
        addIncoming.instructions.insert(new VarInsnNode(Opcodes.ALOAD, 2));
        addIncoming.instructions.insert(new VarInsnNode(Opcodes.ALOAD, 1));
        addIncoming.instructions.insert(new VarInsnNode(Opcodes.ILOAD, 0));

        Inject.injectStaticCallsBegin(main, "dev/zomboid/interp/CoreStub", "serverMain", "()V");

        cp.replaceClass("zombie/network/GameServer", cl);
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
    public void install() throws IOException {
        System.out.println("Injecting into Core");
        injectCore();

        System.out.println("Injecting into GameServer");
        injectGameServer();

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
        System.out.println("Replacing Core with backup");
        removeBackup("zombie/core/Core");

        System.out.println("Replacing GameServer with backup");
        removeBackup("zombie/network/GameServer");
    }
}
