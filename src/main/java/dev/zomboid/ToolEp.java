package dev.zomboid;

import lombok.experimental.UtilityClass;

import java.io.IOException;

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
