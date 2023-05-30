package optimizationprototype.util;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Vector;

public class ProcessManager {

    private static final ProcessManager instance = new ProcessManager();
    private String optimized, unoptimized;
    private final String COMPILER_EXE = System.getProperty("os.name").startsWith("Windows") ? "avr-gcc.exe" : "avr-gcc";
    private String compilerDir;

    private ProcessManager() {
        optimized = null;
        unoptimized = null;
        compilerDir = null;
    }

    public static ProcessManager getInstance() {
        return instance;
    }

    public Vector<String> executeCommands(JFrame parent) {
        try {
            String avrPath;
            if (compilerDir == null) {
                avrPath = System.getenv("AVR_GCC_PATH");
                if (avrPath == null && this.getCompilerDir() == null) {
                    JOptionPane.showMessageDialog(parent, "Cannot find AVR-GCC compiler.\n\n" +
                            "Make sure the compiler is installed on your system,\n" +
                            "and the environment variable \"AVR_GCC_PATH\" is set.", "Error", JOptionPane.ERROR_MESSAGE);
                    return null;
                }
            } else {
                avrPath = compilerDir;
            }
            Path tempDir = Files.createTempDirectory("source-optimizer");
            addShutdownHook(tempDir);
            Path tempOp = Files.createTempFile(tempDir, "optimized_", ".c");
            Path tempUn = Files.createTempFile(tempDir, "unoptimized_", ".c");
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempOp.toString()));
            // write optimized file
            writer.write(SourceHandler.getInstance().getOptimizedCode());
            writer.close();
            writer = new BufferedWriter(new FileWriter(tempUn.toString()));
            // write unoptimized file
            for (String s : SourceHandler.getInstance().getOriginalCode()) {
                writer.write(s + "\n");
            }
            writer.close();
            Vector<String> includeFiles = SourceHandler.getInstance().getIncludeFiles();
            // copy over all files included in the imported file
            for (String s : includeFiles) {
                File file = new File(SourceHandler.getInstance().getCWD() + System.getProperty("file.separator") + s);
                if (!file.isFile()) {
                    JOptionPane.showMessageDialog(parent, "File \"" + s + "\" included in imported file cannot be found or is a directory.", "Error", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                File f1 = new File(tempDir.toFile().getAbsolutePath() + System.getProperty("file.separator") + s);
                Files.copy(file.toPath(), f1.toPath());
            }
            // copy over all header files which were manually included
            Vector<File> headerFiles = SourceHandler.getInstance().getHeaderFiles();
            for (File f : headerFiles) {
                File f1 = new File(tempDir.toFile().getAbsolutePath() + System.getProperty("file.separator") + f.getName());
                if (!f1.exists())
                    Files.copy(f.toPath(), f1.toPath());
            }
            // copy over all source files which were manually included
            Vector<File> sourceFiles = SourceHandler.getInstance().getSourceFiles();
            for (File f : sourceFiles) {
                File f1 = new File(tempDir.toFile().getAbsolutePath() + System.getProperty("file.separator") + f.getName());
                if (!f1.exists())
                    Files.copy(f.toPath(), f1.toPath());
            }
            // run avr-gcc compiler on optimized files
            Vector<String> results = new Vector<>();
            Process p = Runtime.getRuntime().exec(this.getCompileCommand(avrPath + System.getProperty("file.separator") + COMPILER_EXE, tempDir.toString(), tempOp.getFileName().toString().replaceFirst("[.][^.]+$", ""), "atmega168"));
            results.add(tempOp.getFileName() + ":\n" + getErrorOutput(p));
            p.waitFor();
            optimized = tempOp.toString().replaceFirst("[.][^.]+$", "") + ".elf";
            // run avr-gcc compiler on unoptimized files
            p = Runtime.getRuntime().exec(this.getCompileCommand(avrPath + System.getProperty("file.separator") + COMPILER_EXE, tempDir.toString(), tempUn.getFileName().toString().replaceFirst("[.][^.]+$", ""), "atmega168"));
            results.add(tempUn.getFileName() + ":\n" + getErrorOutput(p));
            p.waitFor();
            unoptimized = tempUn.toString().replaceFirst("[.][^.]+$", "") + ".elf";
            for (File f : SourceHandler.getInstance().getSourceFiles()) {
                p = Runtime.getRuntime().exec(this.getCompileCommand(avrPath + System.getProperty("file.separator") + COMPILER_EXE, tempDir.toString(), f.getName().substring(0, f.getName().indexOf(".")), "atmega168"));
                results.add(f.getName() + ":\n" + getErrorOutput(p));
                p.waitFor();
            }
            p = Runtime.getRuntime().exec(this.getLinkCommand(avrPath + System.getProperty("file.separator") + COMPILER_EXE, tempDir.toString(), tempUn.getFileName().toString().replaceFirst("[.][^.]+$", ""), "atmega168"));
            results.add("Linking " + tempUn.getFileName() + ":\n" + getErrorOutput(p));
            p.waitFor();
            p = Runtime.getRuntime().exec(this.getLinkCommand(avrPath + System.getProperty("file.separator") + COMPILER_EXE, tempDir.toString(), tempOp.getFileName().toString().replaceFirst("[.][^.]+$", ""), "atmega168"));
            results.add("Linking " + tempOp.getFileName() + ":\n" + getErrorOutput(p));
            p.waitFor();
            // run avr-size on both files and return the result as strings
            String[] sizeOutput = executeSizeCommands(avrPath, tempUn.toString().replaceFirst("[.][^.]+$", "") + ".elf", tempOp.toString().replaceFirst("[.][^.]+$", "") + ".elf");
            results.add(0, sizeOutput[0]);
            results.add(1, sizeOutput[1]);
            return results;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void writeBin(JFrame parent, boolean isOptimized) {
        if (unoptimized == null || optimized == null)
            return;
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Executable Linkable Format (*.elf), ", "elf");
        fileChooser.addChoosableFileFilter(filter);
        fileChooser.setFileFilter(filter);
        int successValue = fileChooser.showDialog(parent, "Save");
        if (successValue == JFileChooser.APPROVE_OPTION) {
            try {
                if (isOptimized)
                    Files.copy(Paths.get(optimized), fileChooser.getSelectedFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
                else
                    Files.copy(Paths.get(unoptimized), fileChooser.getSelectedFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(parent, "Could not write file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String[] executeSizeCommands(String avrPath, String unoptimizedBin, String optimizedBin) throws IOException {
        String[] results = new String[4];
        Process p = Runtime.getRuntime().exec(avrPath + System.getProperty("file.separator") + "avr-size " + unoptimizedBin);
        results[0] = getProcessOutput(p);
        p = Runtime.getRuntime().exec(avrPath + System.getProperty("file.separator") + "avr-size " + optimizedBin);
        results[1] = getProcessOutput(p);
        return results;
    }

    private String getCompileCommand(String exePath, String dir, String fileName, String targetDev) {
        return exePath + " -mmcu=" + targetDev + " -c " + dir + System.getProperty("file.separator") + fileName + ".c -o " + dir + System.getProperty("file.separator") + fileName + ".o";
    }
    
    private String getLinkCommand(String exePath, String dir, String fileName, String targetDev) {
        String command = exePath + " -mmcu=" + targetDev + " -o " + dir + System.getProperty("file.separator") + fileName + ".elf " + dir + System.getProperty("file.separator") + fileName + ".o ";
        for (File file : SourceHandler.getInstance().getSourceFiles()) {
            command += dir + System.getProperty("file.separator") + file.getName().substring(0, file.getName().indexOf(".")) + ".o ";
        }
        return command;
    }

    private String getProcessOutput(Process process) throws IOException {
        BufferedReader readerResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String result = "", line;
        while ((line = readerResult.readLine()) != null) {
            result += line + "\n";
        }
        return result;
    }

    private String getErrorOutput(Process process) throws IOException {
        BufferedReader readerResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String result = "", line;
        while ((line = readerResult.readLine()) != null) {
            result += line + "\n";
        }
        if (result.isEmpty())
            result = "Success\n";
        return result;
    }

    /**
     * Adds a shutdown hook for recursively deleting all temporary files generated by compilation, assembling, and linking.
     *
     * Reference: https://stackoverflow.com/questions/15022219/does-files-createtempdirectory-remove-the-directory-after-jvm-exits-normally/20280989
     *
     * @param dir temporary directory to be deleted on shutdown
     */
    private void addShutdownHook(Path dir) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, @SuppressWarnings("unused") BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }
                    @Override
                    public FileVisitResult postVisitDirectory(Path dir1, IOException e) throws IOException {
                        if (e == null) {
                            Files.delete(dir1);
                            return FileVisitResult.CONTINUE;
                        }
                        // directory iteration failed
                        throw e;
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete " + dir, e);
            }
        }));
    }
    
    public void setCompilerDir(String dir) {
        this.compilerDir = dir;
    }
    
    public String getCompilerDir() {
        return this.compilerDir;
    }

}
