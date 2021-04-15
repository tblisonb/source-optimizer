package optimizationprototype.config;

import optimizationprototype.util.Logger;
import optimizationprototype.util.Message;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public final class GuiOptions {

    public static boolean fontFlag;

    private GuiOptions() {}

    // Ref: https://stackoverflow.com/questions/5652344/how-can-i-use-a-custom-font-in-java
    static {
        try {
            GraphicsEnvironment ge =
                    GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("src/resources/JetBrainsMono-Regular.ttf")));
            fontFlag = true;
        } catch (IOException |FontFormatException e) {
            fontFlag = false;
            System.out.println(e.getLocalizedMessage());
            Logger.getInstance().log(new Message("Could not apply custom font.", Message.Type.ERROR));
        }
    }

    public static final String HELP_LINK = "https://github.com/tblisonb/source-optimizer";

    public static final Font PANEL_HEADER_FONT = new Font("Segoe UI", Font.PLAIN, 20);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.PLAIN, 20);
    public static final Font CHECKBOX_NODE_PARENT_FONT = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font CHECKBOX_NODE_LIST_FONT = new Font("Segoe UI", Font.PLAIN, 16);
    public static final Font CHECKBOX_LEAF_LIST_FONT = new Font("Segoe UI", Font.ITALIC, 16);
    public static final Font DEFAULT_CODE_FONT = new Font("Courier New", Font.PLAIN, 14);
    public static final Font CUSTOM_CODE_FONT = new Font("JetBrains Mono", Font.PLAIN, 14);

    public static final String TOOL_TIP_COUNTER =           "Applying this optimization will attempt to utilize the " +
                                                            "built-in timer hardware in order to eliminate the use " +
                                                            "of software delays.";
    public static final String TOOL_TIP_TIME_SENSITIVE =    "Applying this option attempts to preserve any timing " +
                                                            "dependencies between blocks of code separated by a " +
                                                            "software delay.";
    public static final String TOOL_TIP_INTERRUPT =         "Applying this optimization will attempt to replace any " +
                                                            "software-bound checks for external pin changes with an " +
                                                            "interrupt driven approach.";
    public static final String TOOL_TIP_BUILTIN =           "Applying this optimization will replace any assignments " +
                                                            "to SREG turning on or off global interrupts with the more " +
                                                            "efficient AVR builtin functions.";
    public static final String TOOL_TIP_ARITHMETIC =        "Applying this optimization will attempt unroll any " +
                                                            "statements with multiplication and replace it with addition " +
                                                            "which is more space efficient.";

    public static final String INFO_COUNTER = "Considerations:\n\nThis optimization changes multiple aspects of your " +
            "software, and as such the resulting code may not function as originally intended. To mitigate some of the " +
            "issues that this optimization may introduce, selecting \"Time-Sensitive Order of Execution\" will ensure" +
            " that any blocks of code separated by a software delay still exhibit the same cadence.\n\nDepending on " +
            "the use case, it may be a benefit to leave this option unchecked, primarily if the code included after " +
            "the software delay is not dependent on that delay to function. This will cause any code after the delay " +
            "to execute far more often and out of sync with the code before the delay, which in some cases may be " +
            "beneficial, but in others may harm the intended functionality.";

}
