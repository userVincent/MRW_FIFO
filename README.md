
# MRW_FIFO and DRAM Documentation

## Overview

This project contains two key components: the **DRAM** (Distributed RAM) module and the **MRW_FIFO** (Multi-Read, Multi-Write FIFO). These modules are designed for high-throughput applications requiring multiple read/write operations to be executed in parallel. Both modules are implemented in Chisel and are fully synthesizable for use in FPGA environments, particularly optimized for **Xilinx FPGAs**.

### Key Features:
- **DRAM**: A **synchronous write, asynchronous read** multiport RAM designed for **high-efficiency** distributed memory implementations on Xilinx FPGAs.
- **MRW_FIFO**: A **multi-port FIFO** that supports multiple reads and writes simultaneously, optimizing the throughput for high-demand applications.

---

## DRAM (Distributed RAM)

### Description
The **DRAM** module is a **multiport memory** module designed for high-efficiency **single-write, triple-read** configurations, with **synchronous write** and **asynchronous read** operations. The naming as "Distributed RAM" comes from its optimal synthesis onto the **SLICEM** resources in Xilinx FPGAs, where **LUTs** (Look-Up Tables) in SLICEM can be used as storage elements.

This module can be configured with:
- **Multiple read ports**
- **Multiple write ports**
- **Read-write ports** that can either perform a read or a write operation in a single clock cycle.

The DRAM is ideal for situations where multiple data accesses (especially reads) are needed in parallel, such as in **buffering**, **data processing**, ... .

### Key Configurations:
- **Read Ports (r)**: Number of parallel read ports.
- **Write Ports (w)**: Number of parallel write ports.
- **Read-Write Ports (rw)**: Number of ports that can be either used for reading or writing (depending on `isWrite` signal).

### How it Works:
- **Synchronous Write**: The write operations happen synchronously, meaning data is written to memory at the rising edge of the clock when the `enable` signal is asserted.
- **Asynchronous Read**: The read operations are asynchronous, meaning data is immediately available after the address is presented and the `enable` signal is asserted. No clock cycle is required to get the data after providing the address.

### Example Usage:
A **single-write, double-read** configuration can be easily achieved using this module by setting:
- `w = 1` (1 write port)
- `r = 2` (2 read ports)

This configuration will ensure the **single-write** port writes data in a clocked manner, and **two read ports** can access different memory locations asynchronously in parallel.
Below you can see the timing diagram write every clock cycle a single write and dual read is attempted on the same address.
![image](https://github.com/user-attachments/assets/9bf23137-dccd-41dc-ad29-bcf3c95bfda7)


---

## MRW_FIFO (Multi-Read, Multi-Write FIFO)

### Description
The **MRW_FIFO** is a configurable **multi-port FIFO** that supports **multiple reads** and **multiple writes** happening simultaneously, depending on the number of configured read/write ports. The FIFO uses the **DRAM** module internally to allow for efficient memory access and parallel operations, making it ideal for applications that require high throughput and efficient buffer management.

This FIFO is particularly useful in scenarios where:
- Data is **written frequently** (e.g., every clock cycle).
- Data can be **read in parallel** to avoid the FIFO from filling up too quickly (e.g., using multiple read ports to keep up with the write throughput).

### Key Configurations:
- **Read Ports (r)**: Number of parallel read ports. 
- **Write Ports (w)**: Number of parallel write ports.
- **Depth**: The total capacity (in terms of number of elements) of the FIFO.

### How it Works:
- **Parallel Reading and Writing**: Multiple read and write ports allow simultaneous enqueue and dequeue operations. If the write throughput is higher than the read throughput, using multiple read ports can help prevent overflow by reading multiple elements at once.
- **Pointer Management**: The FIFO uses **modulo arithmetic** to wrap the read and write pointers around the memory depth, ensuring efficient address management even for non-power-of-two depths.

### Important Usage Considerations:
- **Order of Usage for Read/Write Ports**: The read and write ports **must** be used in order:
  - For example, if 3 read ports are available and you want to read 2 elements, you **must** use ports 1 and 2, not 2 and 3 nor 1 and 3. Failing to do this will corrupt the internal state and require a reset!
  - Similarly, if fewer write ports are used than are available, they must be used sequentially!
  
- **Best Alignment with Xilinx SLICEM**: For efficient synthesis, it’s recommended to configure the FIFO with a **single write** and **triple read** configuration to align with Xilinx SLICEM resources, which can store data using LUTs in an efficient manner.

### Example Use Case:
This FIFO is designed for scenarios where:
- **Writes happen frequently**, but the consumer can only **read the data occasionally**.
- **Parallel reading** can help optimize throughput, ensuring the input and output throughput are balanced.

---

## Example Timing Diagrams
Below are some example timing diagrams illustrating MRW_FIFO operations. These diagrams demonstrate simultaneous read/write operations. On these timing diagrams you can see all read and write ports, the count (element currently in the buffer, max is alwyas setup as 8), both read and write pointers and also read and write count representing the amount of reads/writes occuring that clock cycle.

### 1 write and 3 read ports
![image](https://github.com/user-attachments/assets/c8505c42-2542-49c8-b409-1c37c02d5194)

### 1 write and 3 read ports while only using 2 of the 3 read ports
![image](https://github.com/user-attachments/assets/ed8d1571-8f3a-442c-8234-f9c2b401628c)

### 5 write and 2 read ports
![image](https://github.com/user-attachments/assets/741c74ee-ae5a-4c84-98a9-2f7a0b70806b)

### 2 write and 5 read ports
![image](https://github.com/user-attachments/assets/5a29c349-05a4-4370-9b14-ace30897ac75)

To see what tests where ran to get these diagrams and in case you want to run the tests yourself you can clone this repo and run sbt test which will run all the test code found in 'src/test/scala'. After running sbt test, you will be able to find the timing diagrams (.vcd) in the test_run_dir folder.

## Conclusion
The **DRAM** and **MRW_FIFO** modules are highly optimized for FPGA environments, particularly for Xilinx FPGAs using SLICEM resources. With configurable read/write ports, asynchronous read capabilities, and the ability to handle high-throughput applications, these modules are well-suited for advanced buffer management and memory access needs.
