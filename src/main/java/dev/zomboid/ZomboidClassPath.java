package dev.zomboid;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ZomboidClassPath {

    private final Path root;

    public ZomboidClassPath(String root) {
        this.root = Paths.get(root);
    }

    private String normalize(String dir) {
        dir = dir.replace('/', '\\');
        dir = dir + ".class";
        return dir;
    }

    public ClassNode readClass(String dir) throws IOException {
        dir = normalize(dir);

        Path bkup = root.resolve(dir + ".bkup");
        if (Files.exists(bkup)) {
            dir = dir + ".bkup";
        }

        ClassReader cr = new ClassReader(Files.readAllBytes(root.resolve(dir)));
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);
        return cn;
    }

    public void replaceClass(String dir, ClassNode node) throws IOException {
        dir = normalize(dir);

        Path orig = root.resolve(dir);
        Path bkup = root.resolve(dir + ".bkup");
        if (!Files.exists(bkup)) {
            Files.copy(orig, bkup);
        }

        ClassWriter cw = new ClassWriter(0);
        node.accept(cw);
        Files.write(orig, cw.toByteArray());
    }

}
