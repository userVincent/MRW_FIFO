package MRW_FIFO

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class DRAMTester extends AnyFlatSpec with ChiselScalatestTester {
  "DRAM" should "read and write correctly with multiple ports" in {
    test(new DRAM(1024, UInt(8.W), 2, 2, 2)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      // Test writes through write port 1
      dut.io.writePorts(0).enable.poke(true.B)
      dut.io.writePorts(0).addr.poke(1.U)
      dut.io.writePorts(0).data.poke(42.U)
      dut.clock.step()

      // Test writes through write port 2
      dut.io.writePorts(1).enable.poke(true.B)
      dut.io.writePorts(1).addr.poke(2.U)
      dut.io.writePorts(1).data.poke(84.U)
      dut.clock.step()

      // Disable writing after write
      dut.io.writePorts(0).enable.poke(false.B)
      dut.io.writePorts(1).enable.poke(false.B)

      // Test reads from read port 1
      dut.io.readPorts(0).enable.poke(true.B)
      dut.io.readPorts(0).addr.poke(1.U)
      dut.clock.step()
      dut.io.readPorts(0).data.expect(42.U)

      // Test reads from read port 2
      dut.io.readPorts(1).enable.poke(true.B)
      dut.io.readPorts(1).addr.poke(2.U)
      dut.clock.step()
      dut.io.readPorts(1).data.expect(84.U)

      // Test read-write port: Write operation
      dut.io.readwritePorts(0).enable.poke(true.B)
      dut.io.readwritePorts(0).isWrite.poke(true.B)
      dut.io.readwritePorts(0).addr.poke(3.U)
      dut.io.readwritePorts(0).writeData.poke(128.U)
      dut.clock.step()

      // Test read-write port: Read operation
      dut.io.readwritePorts(0).isWrite.poke(false.B)
      dut.io.readwritePorts(0).addr.poke(3.U)
      dut.clock.step()
      dut.io.readwritePorts(0).readData.expect(128.U)

      // Disable ports after testing
      dut.io.readPorts(0).enable.poke(false.B)
      dut.io.readPorts(1).enable.poke(false.B)
      dut.io.readwritePorts(0).enable.poke(false.B)
    }
  }

  "DRAM" should "perform continuous reads and writes with 1 read and 1 write port" in {
    test(new DRAM(1024, UInt(8.W), 1, 1, 0)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.step(5)  // Wait for the memory to initialize
      // Initialize some variables to hold data to be written and read addresses
      var writeDataPort = 1
      var writeDataPortPrev = 0
      var addrWritePort = 1

      var addrReadPort = 0

      for (cycle <- 0 until 10) {
        // Write to both write ports in every clock cycle
        dut.io.writePorts(0).enable.poke(true.B)
        dut.io.writePorts(0).addr.poke(addrWritePort.U)
        dut.io.writePorts(0).data.poke(writeDataPort.U)  // Write incrementing data

        // Read from both read ports in every clock cycle
        dut.io.readPorts(0).enable.poke(true.B)
        dut.io.readPorts(0).addr.poke(addrReadPort.U)
        

        dut.clock.step()

        // Check if the read ports return the expected data from previous writes
        if (cycle > 0) {
          dut.io.readPorts(0).data.expect(writeDataPortPrev.U)
        }

        // Update addresses for next cycle
        addrWritePort = addrWritePort + 1

        addrReadPort = addrReadPort + 1

        // Update data for next cycle
        writeDataPortPrev = writeDataPort
        writeDataPort = writeDataPort + 1
      }

      // Disable write and read ports at the end of the test
      dut.io.writePorts(0).enable.poke(false.B)
      dut.io.readPorts(0).enable.poke(false.B)
    }
  }

  "DRAM" should "perform continuous reads and writes with 1 read and 1 write port to the same address" in {
    test(new DRAM(1024, UInt(8.W), 1, 1, 0)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.step(5)  // Wait for the memory to initialize
      // Initialize some variables to hold data to be written and read addresses
      var writeDataPort = 1
      var writeDataPortPrev = 0

      for (cycle <- 0 until 10) {
        // Write to both write ports in every clock cycle
        dut.io.writePorts(0).enable.poke(true.B)
        dut.io.writePorts(0).addr.poke(0.U)
        dut.io.writePorts(0).data.poke(writeDataPort.U)  // Write incrementing data

        // Read from both read ports in every clock cycle
        dut.io.readPorts(0).enable.poke(true.B)
        dut.io.readPorts(0).addr.poke(0.U)
        
        if (cycle > 0) {
          dut.io.readPorts(0).data.expect(writeDataPortPrev.U)
        }

        dut.clock.step()

        // Check if the read ports return the expected data from previous writes
        if (cycle > 0) {
          dut.io.readPorts(0).data.expect(writeDataPort.U)
        }


        // Update data for next cycle
        writeDataPortPrev = writeDataPort
        writeDataPort = writeDataPort + 1
      }

      // Disable write and read ports at the end of the test
      dut.io.writePorts(0).enable.poke(false.B)
      dut.io.readPorts(0).enable.poke(false.B)
    }
  }

  "DRAM" should "perform async reads with 1 read and 1 write port to the same address" in {
    test(new DRAM(1024, UInt(8.W), 1, 1, 0)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.step(5)  // Wait for the memory to initialize

      // Initialize some variables to hold data to be written
      var writeDataPort = 1

      for (cycle <- 0 until 10) {
        // Step 1: Write to the write port
        dut.io.writePorts(0).enable.poke(true.B)
        dut.io.writePorts(0).addr.poke(0.U) // Always writing to address 0
        dut.io.writePorts(0).data.poke(writeDataPort.U)  // Write incrementing data

        dut.io.readPorts(0).data.expect(0.U)

        dut.clock.step()  // Step clock to commit write

        // Step 2: Read from the same address without waiting for a clock edge (async read)
        dut.io.readPorts(0).enable.poke(true.B)
        dut.io.readPorts(0).addr.poke(0.U) // Reading from the same address

        // Expect the data to be immediately available due to async read behavior
        dut.io.readPorts(0).data.expect(writeDataPort.U)

        // Update data for next cycle
        writeDataPort = writeDataPort + 1

        dut.clock.step()  // Step the clock to commit read

        // Disable write and read ports at the end of the test
        dut.io.writePorts(0).enable.poke(false.B)
        dut.io.readPorts(0).enable.poke(false.B)
      }

      // Disable write and read ports at the end of the test
      dut.io.writePorts(0).enable.poke(false.B)
      dut.io.readPorts(0).enable.poke(false.B)
    }
  }

  "DRAM" should "perform dual read single writes correctly" in {
    test(new DRAM(1024, UInt(8.W), 2, 1, 0)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.step(5)  // Wait for the memory to initialize

      // Initialize some variables to hold data to be written
      var writeDataPort = 1

      for (cycle <- 0 until 10) {
        // Step 1: Write to the write port
        dut.io.writePorts(0).enable.poke(true.B)
        dut.io.writePorts(0).addr.poke(0.U) // Always writing to address 0
        dut.io.writePorts(0).data.poke(writeDataPort.U)  // Write incrementing data

        dut.clock.step()  // Step clock to commit write

        // Step 2: Read from the same address without waiting for a clock edge (async read)
        dut.io.readPorts(0).enable.poke(true.B)
        dut.io.readPorts(0).addr.poke(0.U) // Reading from the same address

        dut.io.readPorts(1).enable.poke(true.B)
        dut.io.readPorts(1).addr.poke(0.U) // Reading from the same address

        // Expect the data to be immediately available due to async read behavior
        dut.io.readPorts(0).data.expect(writeDataPort.U)
        dut.io.readPorts(1).data.expect(writeDataPort.U)

        // Update data for next cycle
        writeDataPort = writeDataPort + 1
      }

      // Disable write and read ports at the end of the test
      dut.io.writePorts(0).enable.poke(false.B)
      dut.io.readPorts(0).enable.poke(false.B)
      dut.io.readPorts(1).enable.poke(false.B)
    }
  }
}
