package dev.zomboid;

import dev.zomboid.util.Inject;
import lombok.experimental.UtilityClass;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@UtilityClass
public class ToolEp {

    private static boolean injectCore(ZomboidClassPath cp) throws IOException {
        ClassNode cl = cp.readClass("zombie/core/Core");
        MethodNode mt = Inject.findMethod(cl, "EndFrameUI", "()V");
        if (mt == null) {
            System.out.println("Failed to find zombie/core/Core#EndFrameUI()V");
            return false;
        }

        Inject.injectVirtualCallsBegin(mt, "dev/zomboid/interp/RenderingStub", "endFrameUi", "(Lzombie/core/Core;)V");
        cp.replaceClass("zombie/core/Core", cl);
        return true;
    }

    private static boolean injectGameServer(ZomboidClassPath cp) throws IOException {
        ClassNode cl = cp.readClass("zombie/network/GameServer");
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

    private static void extractSelf(String path) throws IOException {
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
            System.out.println(p);
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

    public static void main(String[] args) throws IOException {
        ZomboidClassPath cp = new ZomboidClassPath(".");
        injectCore(cp);
        injectGameServer(cp);

        extractSelf(ToolEp.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getPath());
    }

}
