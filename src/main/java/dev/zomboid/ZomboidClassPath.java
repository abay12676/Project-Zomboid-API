package dev.zomboid;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Provides access to the Zomboid classpath, which is just a loose
 * set of files on disk.
 */
public final class ZomboidClassPath {

    private final Path root;

    public ZomboidClassPath(String root) {
        this.root = Paths.get(root);
    }

    /**
     * Normalizes a path so that it uses windows conventions.
     */
    private String normalize(String dir) {
        dir = dir.replace('/', '\\');
        dir = dir + ".class";
        return dir;
    }

    /**
     * Determines if a class on disk has been injected to or not. This is useful
     * for determining if a game patch has been applied over top of our mods or not.
     */
    private boolean isInjected(String dir) throws IOException {
        ClassReader cr = new ClassReader(Files.readAllBytes(root.resolve(dir)));
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);

        for (AnnotationNode node : cn.visibleAnnotations) {
            if (node.desc.equals("Ldev/zomboid/Injected;")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Reads a class from disk. This will read the backup if available and the primary
     * class file has not been replaced.
     */
    public ClassNode readClass(String dir) throws IOException {
        dir = normalize(dir);

        Path bkup = root.resolve(dir + ".bkup");
        if (Files.exists(bkup) && isInjected(dir)) {
            dir = dir + ".bkup";
        }

        ClassReader cr = new ClassReader(Files.readAllBytes(root.resolve(dir)));
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);
        return cn;
    }

    /**
     * Replaces a class file on disk. Creates a backup if none exists, or the file
     * on disk has not been injected to yet.
     */
    public void replaceClass(String dir, ClassNode node) throws IOException {
        dir = normalize(dir);

        Path orig = root.resolve(dir);
        Path bkup = root.resolve(dir + ".bkup");
        if (!Files.exists(bkup) || !isInjected(dir)) {
            Files.copy(orig, bkup);
        }

        ClassWriter cw = new ClassWriter(0);
        node.accept(cw);
        Files.write(orig, cw.toByteArray());
    }

}
