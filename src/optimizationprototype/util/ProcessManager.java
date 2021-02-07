package optimizationprototype.util;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ProcessManager {

    public static String[] executeCommands(JFrame parent) {
        try {
            String avrPath = System.getenv("AVR_GCC_PATH");
            if (avrPath == null) {
                JOptionPane.showMessageDialog(parent, "Cannot find AVR-GCC compiler.\n\n" +
                        "Make sure the compiler is installed on your system,\n" +
                        "and the environment variable \"AVR_GCC_PATH\" is set.", "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            Path tempDir = Files.createTempDirectory("source-optimizer");
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
            String[] commands = ProcessManager.getCompileCommands(avrPath + "\\avr-gcc.exe", tempDir.toString(), tempOp.getFileName().toString().replaceFirst("[.][^.]+$", ""), "atmega168");
            Runtime.getRuntime().exec(commands[0]).waitFor();
            Runtime.getRuntime().exec(commands[1]).waitFor();
            // run avr-gcc compiler on unoptimized files
            commands = ProcessManager.getCompileCommands(avrPath + "\\avr-gcc.exe", tempDir.toString(), tempUn.getFileName().toString().replaceFirst("[.][^.]+$", ""), "atmega168");
            Runtime.getRuntime().exec(commands[0]).waitFor();
            Runtime.getRuntime().exec(commands[1]).waitFor();
            // run avr-size on both files and return the result as strings
            return executeSizeCommands(avrPath, tempUn.toString().replaceFirst("[.][^.]+$", "") + ".elf", tempOp.toString().replaceFirst("[.][^.]+$", "") + ".elf");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String[] executeSizeCommands(String avrPath, String unoptimizedBin, String optimizedBin) throws IOException {
        String[] results = new String[2];
        Process p = Runtime.getRuntime().exec(avrPath + "\\" + "avr-size.exe " + unoptimizedBin);
        results[0] = getProcessOutput(p);
        p = Runtime.getRuntime().exec(avrPath + "\\" + "avr-size.exe " + optimizedBin);
        results[1] = getProcessOutput(p);
        return results;
    }

    private static String[] getCompileCommands(String exePath, String dir, String fileName, String targetDev) {
        String[] result = new String[2];
        result[0] = exePath + " -mmcu=" + targetDev + " -c " + dir + "\\" + fileName + ".c -o " + dir + "\\" + fileName + ".o";
        result[1] = exePath + " -mmcu=" + targetDev + " -o " + dir + "\\" + fileName + ".elf " + dir + "\\" + fileName + ".o";
        return result;
    }

    private static String getProcessOutput(Process process) throws IOException {
        BufferedReader readerResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String result = "", line;
        while ((line = readerResult.readLine()) != null) {
            result += line + "\n";
        }
        return result;
    }

}
