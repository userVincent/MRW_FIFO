package MRW_FIFO

import chisel3._
import chisel3.util._

class DRAMInterface[T <: Data](depth: Int, gen: T, r: Int, w: Int, rw: Int) extends Bundle {
  val readPorts = Vec(r, new Bundle {
    val addr = Input(UInt(log2Ceil(depth).W))
    val enable = Input(Bool())
    val data = Output(gen)
  })
  val writePorts = Vec(w, new Bundle {
    val addr = Input(UInt(log2Ceil(depth).W))
    val enable = Input(Bool())
    val data = Input(gen)
  })
  val readwritePorts = Vec(rw, new Bundle {
    val addr = Input(UInt(log2Ceil(depth).W))
    val enable = Input(Bool())
    val isWrite = Input(Bool())
    val writeData = Input(gen)
    val readData = Output(gen)
  })
}

class DRAM[T <: Data](depth: Int, gen: T, r: Int, w: Int, rw: Int) extends Module {
  val io = IO(new DRAMInterface(depth, gen, r, w, rw))

  // internal memory (synchronous write and asynchronous read)
  val mem = Mem(depth, gen)

  // read ports
  for (i <- 0 until r) {
    val readAddr = io.readPorts(i).addr
    when(io.readPorts(i).enable) {
      io.readPorts(i).data := mem.read(readAddr)
    }.otherwise {
      io.readPorts(i).data := 0.U.asTypeOf(gen) // Default value when disabled
    }
  }

  // write ports
  for (i <- 0 until w) {
    when(io.writePorts(i).enable) {
      mem.write(io.writePorts(i).addr, io.writePorts(i).data)
    }
  }

  // read-write ports
  for (i <- 0 until rw) {
    val addr = io.readwritePorts(i).addr
    io.readwritePorts(i).readData := 0.U.asTypeOf(gen) // Default value when disabled
    when(io.readwritePorts(i).enable) {
      when(io.readwritePorts(i).isWrite) {
        mem.write(addr, io.readwritePorts(i).writeData)
      }.otherwise {
        io.readwritePorts(i).readData := mem.read(addr)
      }
    }
  }
}
