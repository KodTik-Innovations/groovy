package groovy.runner;

import java.io.File;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;

public class App {
    public static void main(String[] args) {
        File groovyFile = new File("src/main/resources/Test.groovy");
        File outputDir = new File(groovyFile.getParentFile(), "out");

        if (!outputDir.exists()) outputDir.mkdirs();

        try {
            System.out.println("Initializing compiler configuration...");

            CompilerConfiguration configuration = new CompilerConfiguration();
            configuration.setTargetBytecode(CompilerConfiguration.JDK8);
            configuration.setTargetDirectory(outputDir);

            System.out.println("Creating compilation unit...");

            CompilationUnit unit = new CompilationUnit(configuration);
            unit.addSource(groovyFile);

            System.out.println("Starting compilation...");
            unit.compile();

            System.out.println("Compilation successful! Output directory: " + outputDir.getAbsolutePath());

            if (deleteDirectory(outputDir)) {
                System.out.println("Deleted outputDir: " + outputDir.getAbsolutePath());
            } else {
                System.err.println("Failed to delete outputDir: " + outputDir.getAbsolutePath());
            }

        } catch (Throwable t) {
            deleteDirectory(outputDir);
            System.err.println("Compilation failed:");
            t.printStackTrace();
        }
    }

    private static boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                deleteDirectory(file);
            }
        }
        return dir.delete();
    }
}
