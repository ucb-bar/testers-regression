package test

import chisel3.iotesters.{Driver, PeekPokeTester}
import chiseltest.fork
import chiseltest._
import chisel3._
import chisel3.experimental.BundleLiterals.AddBundleLiteralConstructor
import chiseltest.internal._
import firrtl.options.TargetDirAnnotation
import firrtl.annotations.Annotation
import gcd._
import logger.{LogLevel, LogLevelAnnotation}

object RunRegression extends App {
  assert(args.length >= 2)
  val bench = args(0)
  val sim = args(1)
  val repeat = if (args.length > 2) args(2).toInt else 1

  Runner.run(sim = sim, bench = bench, repeat = repeat)
}

private object Runner {
  def run(sim: String, bench: String, repeat: Int): Unit = {

    val backendAnnotation = sim match {
      case "verilator" => VerilatorBackendAnnotation
      case "treadle" => TreadleBackendAnnotation
    }

    val test = bench match {
      case "io-testers" => r: Int => testIOTesters(sim, r)
      case "chiseltest" => r: Int => testChiseltest(backendAnnotation, r)
      case "chiseltest-threads" => r: Int => testThreadedChiseltest(backendAnnotation, r)
    }

    println(s"Running $bench $repeat times on $sim")
    test(repeat)
  }

  private def testIOTesters(sim: String, repeat: Int): Unit = {
    Driver.execute(Array(
      "--target-dir", "test_run_dir/iotesters_gcd",
      "--top-name", "iotesters_gcd",
      "--backend-name", sim,
      //      "-tiwv"
    ), () => new DecoupledGcd(bitWidth = 60)) { dut =>
      (0 until repeat).map { _ =>
        new IoTestersGcd(dut, testValues)
      }.last
    }
  }

  private def testChiseltest(anno: Annotation, repeat: Int): Unit = {
    RawTester.test(
      new DecoupledGcd(bitWidth = 60),
      Seq(
        anno,
        TargetDirAnnotation("test_run_dir/chiseltest_gcd"),
        LogLevelAnnotation(LogLevel.Error)
      )
    ) { dut =>
      (0 until repeat).foreach { _ =>
        new ChiselTestGcdTest(dut, testValues)
      }
    }
  }

  private def testThreadedChiseltest(anno: Annotation, repeat: Int): Unit = {
    RawTester.test(
      new DecoupledGcd(bitWidth = 60),
      Seq(
        anno,
        TargetDirAnnotation("test_run_dir/chiseltest_gcd_enqueue"),
        LogLevelAnnotation(LogLevel.Error)
      )
    ) { dut =>
      val inputBundles = testValues.map { case (a, b, _) =>
        (new GcdInputBundle(dut.bitWidth)).Lit(_.value1 -> a.U, _.value2 -> b.U)
      }
      val outputBundles = testValues.map { case (a, b, c) =>
        (new GcdOutputBundle(dut.bitWidth)).Lit(_.value1 -> a.U, _.value2 -> b.U, _.gcd -> c.U)
      }
      (0 until repeat).foreach { _ =>
        new ChiselTestGcdEnqueueTest(dut, inputBundles, outputBundles)
      }
    }
  }

  private def computeGcd(a: Int, b: Int): Int = {
    var x = a
    var y = b
    var depth = 1
    while (y > 0) {
      if (x > y) {
        x -= y
      }
      else {
        y -= x
      }
      depth += 1
    }
    x
  }

  private val testValues = (for {x <- 2 to 100; y <- 2 to 100} yield (x, y, computeGcd(x, y)))
}


class IoTestersGcd(gcd: DecoupledGcd, testValues: Seq[(Int, Int, Int)]) extends PeekPokeTester(gcd) {
  reset(2)
  poke(gcd.output.ready, 1)
  for ((i, j, expected) <- testValues) {
    poke(gcd.input.bits.value1, i)
    poke(gcd.input.bits.value2, j)
    poke(gcd.input.valid, 1)
    step(1)

    while (peek(gcd.output.valid) == 0) {
      step(1)
    }
    expect(gcd.output.bits.gcd, expected, s"($i,$j) expected: $expected, got: ${peek(gcd.output.bits.gcd)}")
    expect(gcd.output.valid, 1)
  }
}

class ChiselTestGcdTest(dut: DecoupledGcd, testValues: Seq[(Int, Int, Int)]) {
  dut.reset.poke(true.B)
  dut.clock.step(2)
  dut.reset.poke(false.B)
  dut.clock.setTimeout(100000)

  dut.output.ready.poke(true.B)

  for ((i, j, expected) <- testValues) {
    dut.input.bits.value1.poke(i.U)
    dut.input.bits.value2.poke(j.U)
    dut.input.valid.poke(true.B)
    dut.clock.step()

    while (!dut.output.valid.peek.litToBoolean) {
      dut.clock.step()
    }

    dut.output.bits.gcd.expect(expected.U, s"($i,$j) expected: $expected, got: ${dut.output.bits.gcd}")
    dut.output.valid.expect(true.B)
  }
}

class ChiselTestGcdEnqueueTest(
  dut: DecoupledGcd,
  inputBundles: Seq[GcdInputBundle], outputBundles: Seq[GcdOutputBundle]) {
  dut.reset.poke(true.B)
  dut.clock.step(2)
  dut.reset.poke(false.B)

  dut.input.initSource().setSourceClock(dut.clock)
  dut.output.initSink().setSinkClock(dut.clock)
  dut.clock.setTimeout(100000)

  fork {
    dut.input.enqueueSeq(inputBundles)
  }.fork {
    dut.output.expectDequeueSeq(outputBundles)
  }.join()
}