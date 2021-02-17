// See README.md for license details.

package gcd

import chisel3._
import chisel3.stage.ChiselStage

class Sorter(elements: Int, bitSize: Int) extends MultiIOModule {
  val input = IO(Input(Vec(elements, UInt(bitSize.W))))
  val output = IO(Output(Vec(elements, UInt(bitSize.W))))

  def flipper(ins: List[UInt]): List[UInt] = {
    ins match {
      case in1 :: in2 :: Nil =>
        val (wire1, wire2) = (Wire(UInt(bitSize.W)), Wire(UInt(bitSize.W)))
        when(in1 < in2) {
          wire1 := in1
          wire2 := in2
        }.otherwise {
          wire1 := in2
          wire2 := in1
        }
        List(wire1, wire2)
      case in :: Nil =>
        List(in)
    }
  }

  val sortedWires = (0 until elements).foldLeft(input.toList) {
    case (row, rowNumber) =>
      if (rowNumber % 2 == 0) {
        row.sliding(2, 2).map(_.toList).flatMap(flipper).toList
      } else {
        flipper(List(row.head)) ++ row.tail.sliding(2, 2).map(_.toList).flatMap(flipper).toSeq
      }
  }

  output <> sortedWires
}

object Sorter {
  def main(args: Array[String]): Unit = {
    println(ChiselStage.emitFirrtl(new Sorter(4, 8)))
  }
}
