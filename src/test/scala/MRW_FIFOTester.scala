package MRW_FIFO

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class MRW_FIFOTester extends AnyFlatSpec with ChiselScalatestTester {
    "MRW_FIFO" should "get filled and emptied with data with 1 read and 1 write port" in {
        test(new MRW_FIFO(UInt(8.W), 1, 1, 8)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
            // Test writing to the FIFO
            for (i <- 0 until 8) {
                dut.io.enq(0).valid.poke(true.B)
                dut.io.enq(0).bits.poke((i+1).U)
                dut.clock.step()
            }
            dut.io.enq(0).valid.poke(false.B)

            // Test reading from the FIFO
            for (i <- 0 until 8) {
                dut.io.deq(0).ready.poke(true.B)
                dut.io.deq(0).valid.expect(true.B)
                dut.io.deq(0).bits.expect((i+1).U)
                dut.clock.step()
            }
        }
    }

    "MRW_FIFO" should "get filled and emptied with 1 read and 1 write port and uneven size" in {
        test(new MRW_FIFO(UInt(8.W), 1, 1, 7)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
            // Test writing to the FIFO
            for (i <- 0 until 7) {
                dut.io.enq(0).valid.poke(true.B)
                dut.io.enq(0).bits.poke((i+1).U)
                dut.clock.step()
            }
            dut.io.enq(0).valid.poke(false.B)

            // Test reading from the FIFO
            for (i <- 0 until 7) {
                dut.io.deq(0).ready.poke(true.B)
                dut.io.deq(0).valid.expect(true.B)
                dut.io.deq(0).bits.expect((i+1).U)
                dut.clock.step()
            }
        }
    }

    "MRW_FIFO" should "allow simultaneous writes and reads, starting write 1 cycle earlier and ending read 1 cycle later" in {
        test(new MRW_FIFO(UInt(8.W), 1, 1, 8)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
            // Cycle 0: Start writing data into the FIFO
            dut.io.enq(0).valid.poke(true.B)
            dut.io.enq(0).bits.poke(1.U)
            dut.io.deq(0).ready.poke(false.B)
            dut.clock.step()

            // Cycle 2 to 9: Write and Read simultaneously
            for (i <- 1 until 9) {
                // Writing data into the FIFO
                dut.io.enq(0).valid.poke(true.B)
                dut.io.enq(0).bits.poke((i + 1).U)  // Writing i + 1 data

                // Start reading 1 cycle later, data should be valid
                dut.io.deq(0).ready.poke(true.B)
                dut.io.deq(0).valid.expect(true.B)  // Data should be valid
                dut.io.deq(0).bits.expect(i.U)      // Expect to read (i) from FIFO

                dut.clock.step()
            }

            // Final cycle: Read the last data without writing
            dut.io.enq(0).valid.poke(false.B)     // Stop writing
            dut.io.deq(0).ready.poke(true.B)      // Continue reading last element
            dut.io.deq(0).valid.expect(true.B)    // Data should be valid
            dut.io.deq(0).bits.expect(9.U)        // Expect to read last written value (8)
            dut.clock.step()

            // Check that FIFO is empty after the final read
            dut.io.deq(0).valid.expect(false.B)   // FIFO should now be empty
        }
    }

    "MRW_FIFO" should "allow parallel reads in a setup with 1 write and 3 read ports" in {
        test(new MRW_FIFO(UInt(8.W), 3, 1, 8)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
            // Fill the FIFO
            for (i <- 0 until 8) {
                dut.io.enq(0).valid.poke(true.B)
                dut.io.enq(0).bits.poke((i+1).U)
                dut.clock.step()
            }
            dut.io.enq(0).valid.poke(false.B)

            var expected_read = 1
            // Read from all 3 read ports
            for (i <- 0 until 8) {
                for (j <- 0 until 3) {
                    // only poke read true if valid is true
                    if (dut.io.deq(j).valid.peek().litToBoolean) {
                        dut.io.deq(j).ready.poke(true.B)
                        dut.io.deq(j).bits.expect(expected_read.U)
                        expected_read += 1
                    } else {
                        dut.io.deq(j).ready.poke(false.B)
                    }
                }
                dut.clock.step()

                // keep writing aswell
                dut.io.enq(0).valid.poke(true.B)
                dut.io.enq(0).bits.poke((i+1+8).U)
            }
        }
    }

    "MRW_FIFO" should "allow parallel reads in a setup with 1 write and 3 read ports, only using 2 read ports" in {
        test(new MRW_FIFO(UInt(8.W), 3, 1, 8)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
            // Fill the FIFO
            for (i <- 0 until 8) {
                dut.io.enq(0).valid.poke(true.B)
                dut.io.enq(0).bits.poke((i+1).U)
                dut.clock.step()
            }
            dut.io.enq(0).valid.poke(false.B)

            var expected_read = 1
            // Read from all 3 read ports
            for (i <- 0 until 8) {
                for (j <- 0 until 2) {
                    // only poke read true if valid is true
                    if (dut.io.deq(j).valid.peek().litToBoolean) {
                        dut.io.deq(j).ready.poke(true.B)
                        dut.io.deq(j).bits.expect(expected_read.U)
                        expected_read += 1
                    } else {
                        dut.io.deq(j).ready.poke(false.B)
                    }
                }
                dut.clock.step()

                // keep writing aswell
                dut.io.enq(0).valid.poke(true.B)
                dut.io.enq(0).bits.poke((i+1+8).U)
            }
        }
    }
}
