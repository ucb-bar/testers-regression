// See README.md for license details.

package gcd

import chisel3.iotesters
import chisel3.iotesters.{ChiselFlatSpec, PeekPokeTester}

class GcdTestersRegressionTester(gcd: DecoupledGcd, testName: String) extends PeekPokeTester(gcd) {
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


  poke(gcd.output.ready,1)

  SimpleTimer(testName) {
    for ((i, j) <- testValues) {
      poke(gcd.input.bits.value1, i)
      poke(gcd.input.bits.value2, j)
      poke(gcd.input.valid, 1)
      step(1)
      // poke(gcd.input.valid, 0)

      val (expected_gcd, steps) = computeGcd(i, j)

      while (peek(gcd.output.valid) == 0) {
        step(1)
      }
      expect(gcd.output.bits.gcd, expected_gcd)
      expect(gcd.output.valid, 1)
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
class GcdTestersRegression extends ChiselFlatSpec {

  "Basic test using Driver.execute" should "be used as an alternative way to run specification" in {
    iotesters.Driver.execute(Array(), () => new DecoupledGcd(30)) {
      c => new GcdTestersRegressionTester(c, testName = "testers gcd treadle")
    } should be (true)
  }

  "using --backend-name verilator" should "be an alternative way to run using verilator" in {
    iotesters.Driver.execute(Array("--backend-name", "verilator"), () => new DecoupledGcd(30)) {
      c => new GcdTestersRegressionTester(c, testName = "testers gcd verilator")
    } should be(true)
  }
}
