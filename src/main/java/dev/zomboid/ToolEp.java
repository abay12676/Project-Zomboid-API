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

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Please specify either -install or -uninstall.");
            return;
        }

        ZomboidClassPath cp = new ZomboidClassPath(".");
        GamePatcher patcher = new GamePatcher(cp);
        switch (args[0]) {
            case "-install":
                System.out.println("Installing...");
                patcher.install();
                break;
            case "-uninstall":
                System.out.println("Uninstalling...");
                patcher.uninstall();
                break;
            default:
                System.out.println("Unknown command line argument '" + args[0] + "'");
        }
    }

}
