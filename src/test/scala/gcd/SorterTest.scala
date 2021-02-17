//// SPDX-License-Identifier: Apache-2.0
//
//package gcd
//
//import chisel3._
//import chiseltest._
//import chiseltest.ChiselScalatestTester
//import org.scalatest.freespec.AnyFreeSpec
//
//import scala.util.Random
//
//class SorterTest extends AnyFreeSpec with ChiselScalatestTester {
//  val random = Random
//  "test 4" in {
//    val elements = 64
//    val inputs = Seq.fill(elements)(random.nextInt(32))
//    println(s"Input: ${inputs.mkString(",")}")
//    test(new Sorter(elements, 8)) { dut =>
//      inputs.zipWithIndex.foreach { case (value, index) =>
//        dut.input(index).poke(value.U)
//      }
//      inputs.sorted.zipWithIndex.foreach { case (expected, index) =>
//        print(s"${dut.output(index).peek().litValue()}, ")
//        dut.output(index).expect(expected.U)
//      }
//      println("")
//    }
//  }
//}
