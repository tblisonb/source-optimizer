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
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.PLAIN, 18);
    public static final Font CHECKBOX_NODE_PARENT_FONT = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font CHECKBOX_NODE_LIST_FONT = new Font("Segoe UI", Font.PLAIN, 16);
    public static final Font CHECKBOX_LEAF_LIST_FONT = new Font("Segoe UI", Font.ITALIC, 16);
    public static final Font DEFAULT_CODE_FONT = new Font(System.getProperty("os.name").startsWith("Windows") ? "Courier New" : "Liberation Mono", Font.PLAIN, 14);
    public static final Font CUSTOM_CODE_FONT = new Font("JetBrains Mono", Font.PLAIN, 14);

    public static final String TOOL_TIP_COUNTER =           "Applying this optimization will attempt to utilize the " +
                                                            "built-in timer hardware in order to eliminate the use " +
                                                            "of software delays.";
    public static final String TOOL_TIP_TIME_SENSITIVE =    "Applying this option attempts to preserve any timing " +
                                                            "dependencies between blocks of code separated by a " +
                                                            "software delay.";
    public static final String TOOL_TIP_PWM =               "Applying this optimization will attempt to transform any " +
                                                            "software-driven PWM (i.e. toggling a pin on and off at a " +
                                                            "certain duty cycle and frequency) with a hardware-based " +
                                                            "approach which utilizes the counter/timer hardware.";
    public static final String TOOL_TIP_INTERRUPT =         "Applying this optimization will attempt to replace any " +
                                                            "software-bound checks for external pin changes with an " +
                                                            "interrupt driven approach.";
    public static final String TOOL_TIP_BUILTIN =           "Applying this optimization will replace any assignments " +
                                                            "to SREG turning on or off global interrupts with the more " +
                                                            "efficient AVR builtin functions.";
    public static final String TOOL_TIP_ARITHMETIC =        "Applying this optimization will attempt unroll any " +
                                                            "statements with multiplication and replace it with addition " +
                                                            "which is more space efficient.";
    public static final String TOOL_TIP_ADD_SOURCE_ENB =    "Add any additional C source files which are required for " +
                                                            "compilation.";
    public static final String TOOL_TIP_ADD_SOURCE_DIS =    "Import a C source file first before adding additional files";
    public static final String TOOL_TIP_ADD_HEADER_ENB =    "Add any additional C header files which are required for " +
                                                            "compilation.";
    public static final String TOOL_TIP_ADD_HEADER_DIS =    "Import a C source file first before adding additional files";

    public static final String INFO_COUNTER = "Considerations:\n\nThis optimization changes multiple aspects of your " +
            "software, and as such the resulting code may not function as originally intended. To mitigate some of the " +
            "issues that this optimization may introduce, selecting \"Time-Sensitive Order of Execution\" will ensure" +
            " that any blocks of code separated by a software delay still exhibit the same cadence.\n\nDepending on " +
            "the use case, it may be a benefit to leave this option unchecked, primarily if the code included after " +
            "the software delay is not dependent on that delay to function. This will cause any code after the delay " +
            "to execute far more often and out of sync with the code before the delay, which in some cases may be " +
            "beneficial, but in others may harm the intended functionality.";

    public static final String INFO_TIME_SENSITIVE = "Considerations:\n\nThis option ensures any timing requirements " +
            "imposed by the original program is preserved, and should be used in the case that the Counter/Timer " +
            "optimization negatively impacts the functionality of the program.";

    public static final String INFO_INTERRUPT = "Considerations:\n\nThis optimization exposes conditionals which check " +
            "the state of an external pin and maps it to an interrupt service routine which is configured to execute on " +
            "a state change. This optimization will alter the flow of the target program, and as such may interfere with " +
            "variables defined locally within the control loop.";

    public static final String INFO_PWM = "Considerations:\n\nSimilar to the Counter/Timer optimization, this " +
            "optimization alters the existing code and maps it to utilize the counter/timer instance corresponding to " +
            "the pin which is used by the target program to generate a software-driven PWM signal. As such, not all " +
            "programs can have the PWM optimization applied as there are only six total pins which may be used to " +
            "generate a PWM signal (i.e. PB1, PB2, PB3, PD3, PD5, and PD6 in the case of the ATmega168). As such, if a " +
            "software-based PWM signal is being generated on an unsupported pin, the optimization cannot be applied " +
            "without redefining the pin mappings of the program to target one of the supported pins.";

    public static final String INFO_IDC = "In the event that the PWM optimization produces a signal with an unexpected " +
            "duty cycle, it may be due to the optimization not being able to determine the initial state of the pin " +
            "being used for PWM output. The Inverted Duty Cycle option will invert the length of time the PWM channel " +
            "holds the signal high versus low to address this issue.\n\nOnly consider enabling this option if the " +
            "expected duty cycle of the signal is not as expected.";

    public static final String INFO_PF = "If the target program requires a PWM signal of a certain frequency AND duty " +
            "cycle, this option should be selected. It must be noted that this option will limit the PWM output to be " +
            "only on either pin PB1 or PB2, as the single 16 bit counter/timer instance is required for this " +
            "functionality.";

    public static final String INFO_BUILTIN = "Considerations:\n\nThis optimization directly replaces assignments of " +
            "SREG which modify only the global interrupt enable bit. This reduces the number of instructions generated " +
            "by the compiler from four to one instruction any time this case is met. There is no significant " +
            "modification made by this optimizaiton which otherwise changes the functionality of the target program.";

    public static final String INFO_ARITHMETIC = "Considerations:\n\nThis optimization replaces specific cases of " +
            "arithmetic operations with implementations which better target the provided hardware. If floating point " +
            "values are being used in combination with multiplication or divisions by powers of two, this optimization " +
            "may cause the target program to not compile. This is due to not accounting for the original datatype of any " +
            "variable being targeted by the optimization. As such, if such a case within the target program exists, this " +
            "optimization should be disabled.";

}
