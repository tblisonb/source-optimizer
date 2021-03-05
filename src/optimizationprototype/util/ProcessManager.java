package optimizationprototype.util;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class ProcessManager {

    private static ProcessManager instance = new ProcessManager();
    private String optimized, unoptimized;

    private ProcessManager() {
        optimized = null;
        unoptimized = null;
    }

    public static ProcessManager getInstance() {
        return instance;
    }

    public String[] executeCommands(JFrame parent) {
        try {
            String avrPath = System.getenv("AVR_GCC_PATH");
            if (avrPath == null) {
                JOptionPane.showMessageDialog(parent, "Cannot find AVR-GCC compiler.\n\n" +
                        "Make sure the compiler is installed on your system,\n" +
                        "and the environment variable \"AVR_GCC_PATH\" is set.", "Error", JOptionPane.ERROR_MESSAGE);
                return null;
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
            writer.write(SourceHandler.getInstance().getOriginalFile().toString());
            writer.close();

            // run avr-gcc compiler on optimized files
            String[] commands = this.getCompileCommands(avrPath + "\\avr-gcc.exe", tempDir.toString(), tempOp.getFileName().toString().replaceFirst("[.][^.]+$", ""), "atmega168");
            Runtime.getRuntime().exec(commands[0]).waitFor();
            Runtime.getRuntime().exec(commands[1]).waitFor();
            optimized = tempOp.toString().replaceFirst("[.][^.]+$", "") + ".elf";
            // run avr-gcc compiler on unoptimized files
            commands = this.getCompileCommands(avrPath + "\\avr-gcc.exe", tempDir.toString(), tempUn.getFileName().toString().replaceFirst("[.][^.]+$", ""), "atmega168");
            String[] results = new String[4];
            Process p = Runtime.getRuntime().exec(commands[0]);
            results[2] = getErrorOutput(p);
            p.waitFor();
            p = Runtime.getRuntime().exec(commands[1]);
            results[3] = getErrorOutput(p);
            p.waitFor();
            unoptimized = tempUn.toString().replaceFirst("[.][^.]+$", "") + ".elf";
            // run avr-size on both files and return the result as strings
            String[] sizeOutput = executeSizeCommands(avrPath, tempUn.toString().replaceFirst("[.][^.]+$", "") + ".elf", tempOp.toString().replaceFirst("[.][^.]+$", "") + ".elf");
            results[0] = sizeOutput[0];
            results[1] = sizeOutput[1];
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
        Process p = Runtime.getRuntime().exec(avrPath + "\\" + "avr-size.exe " + unoptimizedBin);
        results[0] = getProcessOutput(p);
        p = Runtime.getRuntime().exec(avrPath + "\\" + "avr-size.exe " + optimizedBin);
        results[1] = getProcessOutput(p);
        return results;
    }

    private String[] getCompileCommands(String exePath, String dir, String fileName, String targetDev) {
        String[] result = new String[2];
        result[0] = exePath + " -mmcu=" + targetDev + " -c " + dir + "\\" + fileName + ".c -o " + dir + "\\" + fileName + ".o";
        result[1] = exePath + " -mmcu=" + targetDev + " -o " + dir + "\\" + fileName + ".elf " + dir + "\\" + fileName + ".o";
        return result;
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

}
