// See README.md for license details.

package gcd

import chisel3._
import chisel3.tester._
import chisel3.tester.experimental.TestOptionBuilder._
import org.scalatest.FreeSpec
import chisel3.experimental.BundleLiterals._
import chisel3.tester.internal.VerilatorBackendAnnotation

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly gcd.GcdDecoupledTester
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly gcd.GcdDecoupledTester'
  * }}}
  */
//scalastyle:off magic.number
class GcdTesters2 extends FreeSpec with ChiselScalatestTester {

  def runTest(dut: DecoupledGcd, testName: String): Unit = {
    dut.input.initSource()
    dut.input.setSourceClock(dut.clock)
    dut.output.initSink()
    dut.output.setSinkClock(dut.clock)

    val testValues = for {x <- 1 to 100; y <- 1 to 100} yield (x, y)
    val inputSeq = testValues.map { case (x, y) => (new GcdInputBundle(16)).Lit(_.value1 -> x.U, _.value2 -> y.U) }
    val resultSeq = testValues.map { case (x, y) =>
      (new GcdOutputBundle(16)).Lit(_.value1 -> x.U, _.value2 -> y.U, _.gcd -> BigInt(x).gcd(BigInt(y)).U)
    }

    SimpleTimer(testName) {
      fork {
        // push inputs into the calculator, stall for 11 cycles one third of the way
        val (seq1, seq2) = inputSeq.splitAt(resultSeq.length / 3)
        dut.input.enqueueSeq(seq1)
        dut.clock.step(11)
        dut.input.enqueueSeq(seq2)
      }.fork {
        // retrieve computations from the calculator, stall for 10 cycles one half of the way
        val (seq1, seq2) = resultSeq.splitAt(resultSeq.length / 2)
        dut.output.expectDequeueSeq(seq1)
        dut.clock.step(10)
        dut.output.expectDequeueSeq(seq2)
      }.join()
    }
  }


  "Run testers2 regression with decpoupled helpers and treadle" in {
    test(new DecoupledGcd(30)) { dut =>
      runTest(dut, testName = "testers2 gcd decoupled helpers treadle")
    }
  }

  "Run testers2 regression with decpoupled helpers and verilator" in {
    val annos = Seq(VerilatorBackendAnnotation)
    test(new DecoupledGcd(30)).withAnnotations(annos) { dut =>
      runTest(dut, testName = "testers2 gcd decoupled helpers verilator")
    }
  }
}
