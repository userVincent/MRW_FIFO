package MRW_FIFO

import chisel3._
import chisel3.util._
import chisel3.stage._

class MRW_FIFO[T <: Data](gen: T, r: Int = 1, w: Int = 1, depth: Int = 8) extends Module {
    val io = IO(new Bundle {
        val enq = Vec(w, Flipped(Decoupled(gen)))
        val deq = Vec(r, Decoupled(gen))
    })

    val mem = Module(new DRAM(depth, gen, r, w, 0))
    val ptrSize = log2Ceil(depth)
    val r_ptr = RegInit(0.U(ptrSize.W))
    val w_ptr = RegInit(0.U(ptrSize.W))
    // keep track of the number of elements in the FIFO
    val count = RegInit(0.U((ptrSize+1).W))

    // read side
    for (i <- 0 until r) {
        io.deq(i).valid := count > i.U
        mem.io.readPorts(i).addr := (r_ptr + i.U) % depth.U
        mem.io.readPorts(i).enable := true.B
        io.deq(i).bits := mem.io.readPorts(i).data
    }

    // write side
    for (i <- 0 until w) {
        io.enq(i).ready := (count + i.U) < depth.U
        when (io.enq(i).valid && io.enq(i).ready) {
            mem.io.writePorts(i).addr := (w_ptr + i.U) % depth.U
            mem.io.writePorts(i).data := io.enq(i).bits
            mem.io.writePorts(i).enable := true.B
        }.otherwise {
            mem.io.writePorts(i).enable := false.B
            mem.io.writePorts(i).data := DontCare
            mem.io.writePorts(i).addr := DontCare
        }
    }

    // next state logic
    val read_count = PopCount(io.deq.map(deq => deq.valid && deq.ready))
    val write_count = PopCount(io.enq.map(enq => enq.valid && enq.ready))
    count := count + write_count - read_count
    r_ptr := (r_ptr + read_count) % depth.U
    w_ptr := (w_ptr + write_count) % depth.U
}
