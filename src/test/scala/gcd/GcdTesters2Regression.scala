// See README.md for license details.

package gcd

import chisel3._
import chisel3.tester._
import chisel3.tester.experimental.TestOptionBuilder._
import chisel3.tester.internal.VerilatorBackendAnnotation
import org.scalatest.{FreeSpec, Matchers}

class GcdTesters2RegressionTester(gcd: DecoupledGcd, testName: String) {
  /**
    * compute the gcd and the number of steps it should take to do it
    *
    * @param a positive integer
    * @param b positive integer
    * @return the GCD of a and b and the number of steps required to compute it
    */
  def computeGcd(a: Int, b: Int): (Int, Int) = {
    var x = a
    var y = b
    var depth = 1
    while(y > 0 ) {
      if (x > y) {
        x -= y
      }
      else {
        y -= x
      }
      depth += 1
    }
    (x, depth)
  }

  val testValues = for { x <- 1 to 100; y <- 1 to 100} yield (x, y)

  //scalastyle:off magic.number
  gcd.clock.setTimeout(1000000)

  gcd.output.ready.poke(true.B)

  SimpleTimer(testName) {
    for ((i, j) <- testValues) {
      gcd.input.bits.value1.poke(i.U)
      gcd.input.bits.value2.poke(j.U)
      gcd.input.valid.poke(true.B)
      gcd.clock.step()

      val (expected_gcd, steps) = computeGcd(i, j)

      while (!gcd.output.valid.peek.litToBoolean) {
        gcd.clock.step()
      }

      gcd.output.bits.gcd.expect(expected_gcd.U)
      gcd.output.valid.expect(true.B)
    }
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly gcd.GCDTester
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly gcd.GCDTester'
  * }}}
  */
//scalastyle:off magic.number
class GcdTesters2Regression extends FreeSpec with ChiselScalatestTester with Matchers {


  "Run testers2 regression using treadle" in {
    test(new DecoupledGcd(width = 30)) { c =>
      new GcdTesters2RegressionTester(c, testName = "testers2 gcd treadle")
      }
  }

  "run tester2 regression using verilator" in {
    test(new DecoupledGcd(width = 30)).withAnnotations(Seq(VerilatorBackendAnnotation)) { c =>
      new GcdTesters2RegressionTester(c, testName = "testers2 gcd verilator")
    }
  }
}
