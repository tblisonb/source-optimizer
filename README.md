## Embedded C Source Code Optimizer
This software is intended to be both a functional and educational tool for demonstrating embedded design concepts. It attempts to apply optimizations to user-provided source code in order to maximize potential performance / minimize overall latency by utilizing supported hardware peripherals and builtin functions found on a variety of AVR MCUs. 

For a novice embedded programmer, configuring hardware peripherals and applying embedded design principles can be daunting. This tool acts as a gateway for such an individual to learn how to setup these peripherals and apply such concepts without the end user having to dig through datasheets.

There are many user-selectable options for what types of optimizations are applied, and how each optimization affects the performance uplift and functionality of the generated code. Not all optimization options are guaranteed to preserve the original functionality of the program, although it is made clear which optimizations pose a risk of altering the final functionality of the program and to what degree.

It is worth mentioning that this software is not intended to fix source code that doesn't compile or run, rather it transforms a working program to be more performant, better utilize the provided hardware, and add to the extensibility of the program for future additions.