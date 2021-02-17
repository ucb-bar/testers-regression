// SPDX-License-Identifier: Apache-2.0

package gcd

import chisel3._
import chisel3.iotesters.{Driver, PeekPokeTester}
import chiseltest._
import chiseltest.RawTester
import chiseltest.internal.{TreadleBackendAnnotation, VerilatorBackendAnnotation}
import org.scalatest.freespec.AnyFreeSpec
import treadle.chronometry.Timer
import utest.test

import scala.util.Random

class LifeTests(c: Life, doPrint: Boolean, doPeeks: Boolean) extends PeekPokeTester(c) {
  var liveCount = 0L
  var deadCount = 0L

  def setMode(run: Boolean): Unit = {
    poke(c.io.running, run)
    step(1)
  }

  def clearBoard(): Unit = {
    poke(c.io.writeValue, 0)

    for {
      i <- 0 until c.rows
      j <- 0 until c.cols
    } {
      poke(c.io.writeRowAddress, i)
      poke(c.io.writeColAddress, j)
      step(1)
    }
  }

  def initBlinker(): Unit = {
    clearBoard()

    poke(c.io.writeValue, 1)
    poke(c.io.writeRowAddress, 3)
    for (addr <- Seq(3, 5)) {
      poke(c.io.writeColAddress, addr)
      step(1)
    }
    poke(c.io.writeRowAddress, 4)
    for (addr <- Seq(4, 5)) {
      poke(c.io.writeColAddress, addr)
      step(1)
    }
    poke(c.io.writeRowAddress, 5)
    for (addr <- Seq(4)) {
      poke(c.io.writeColAddress, addr)
      step(1)
    }

  }

  def initGlider(): Unit = {
    clearBoard()

    poke(c.io.writeValue, 1)
    poke(c.io.writeRowAddress, 3)
    for (addr <- Seq(3, 5)) {
      poke(c.io.writeColAddress, addr)
      step(1)
    }
    poke(c.io.writeRowAddress, 4)
    for (addr <- Seq(4, 5)) {
      poke(c.io.writeColAddress, addr)
      step(1)
    }
    poke(c.io.writeRowAddress, 5)
    for (addr <- Seq(4)) {
      poke(c.io.writeColAddress, addr)
      step(1)
    }
  }

  def randomize(): Unit = {
    clearBoard()

    for (addr <- 0 until c.rows * c.rows) {
      poke(c.io.writeValue, Random.nextBoolean())
      poke(c.io.writeRowAddress, addr)
      step(1)
    }
  }

  def printBoard(): Unit = {
    if (doPrint) {
      // Print column number
      print("   ")
      for (i <- 0 until c.cols)
        print(" " + i.toString.last)
      println()

      for (i <- 0 until c.rows) {
        // Print line number
        print(f"$i%2d")
        print(" ")

        // Print cell state
        for {
          j <- 0 until c.cols
        } {
          val s = peek(c.io.state(i)(j))
          if (s == 1)
            print(" *")
          else
            print("  ")
        }

        println()
      }
      println()
    } else if (doPeeks) {
      for (i <- 0 until dut.rows) {
        for (j <- 0 until dut.cols) {
          val s = peek(dut.io.state(i)(j))
          if (s == 1) {
            liveCount += 1L
          } else {
            deadCount += 1L
          }
        }
      }
    }
  }

  setMode(run = false)
  // uncomment one of these
  //  initBlinker
  initGlider()
  //  randomize()
  printBoard()

  setMode(run = true)

  for (time <- 0 until 100) {
    if (doPrint) {
      print("\u001b[2J")
      println(s"Period: $time")
    }
    printBoard()
    step(1)
    if (doPrint) {
      Thread.sleep(300)
    }
  }
  println(s"Live: $liveCount, Dead: $deadCount")
}

class ChiselTestLifeTest(dut: Life, doPrint: Boolean, doPeeks: Boolean) {
  var liveCount = 0L
  var deadCount = 0L

  def setMode(run: Boolean): Unit = {
    dut.io.running.poke(run.B)
    dut.clock.step(1)
  }

  def clearBoard(): Unit = {
    dut.io.writeValue.poke(0.B)

    for {
      i <- 0 until dut.rows
      j <- 0 until dut.cols
    } {
      dut.io.writeRowAddress.poke(i.U)
      dut.io.writeColAddress.poke(j.U)
      dut.clock.step(1)
    }
  }

  def initBlinker(): Unit = {
    clearBoard()

    dut.io.writeValue.poke(1.B)
    dut.io.writeRowAddress.poke(3.U)
    for (addr <- Seq(3, 5)) {
      dut.io.writeColAddress.poke(addr.U)
      dut.clock.step(1)
    }
    dut.io.writeRowAddress.poke(4.U)
    for (addr <- Seq(4, 5)) {
      dut.io.writeColAddress.poke(addr.U)
      dut.clock.step(1)
    }
    dut.io.writeRowAddress.poke(5.U)
    for (addr <- Seq(4)) {
      dut.io.writeColAddress.poke(addr.U)
      dut.clock.step(1)
    }

  }

  def initGlider(): Unit = {
    clearBoard()

    dut.io.writeValue.poke(1.B)
    dut.io.writeRowAddress.poke(3.U)
    for (addr <- Seq(3, 5)) {
      dut.io.writeColAddress.poke(addr.U)
      dut.clock.step(1)
    }
    dut.io.writeRowAddress.poke(4.U)
    for (addr <- Seq(4, 5)) {
      dut.io.writeColAddress.poke(addr.U)
      dut.clock.step(1)
    }
    dut.io.writeRowAddress.poke(5.U)
    for (addr <- Seq(4)) {
      dut.io.writeColAddress.poke(addr.U)
      dut.clock.step(1)
    }
  }

  def randomize(): Unit = {
    clearBoard()

    for (addr <- 0 until dut.rows * dut.rows) {
      dut.io.writeValue.poke(Random.nextBoolean().B)
      dut.io.writeRowAddress.poke(addr.U)
      dut.clock.step(1)
    }
  }

  def printBoard(): Unit = {
    if (doPrint) {
      // Print column number
      print("   ")
      for (i <- 0 until dut.cols)
        print(" " + i.toString.last)
      println()

      for (i <- 0 until dut.rows) {
        // Print line number
        print(f"$i%2d")
        print(" ")

        // Print cell state
        for {
          j <- 0 until dut.cols
        } {
          val s = dut.io.state(i)(j).peek()
          if (s == 1) {
            print(" *")
          } else {
            print("  ")
          }
        }

        println()
      }
      println()
    } else if (doPeeks) {
      for (i <- 0 until dut.rows) {
        for (j <- 0 until dut.cols) {
          val s = dut.io.state(i)(j).peek().litValue().toInt
          if (s == 1) {
            liveCount += 1L
          } else {
            deadCount += 1L
          }
        }
      }
    }
  }

  setMode(run = false)
  // uncomment one of these
  //  initBlinker
  initGlider()
  //  randomize()
  printBoard()

  setMode(run = true)

  for (time <- 0 until 100) {
    if (doPrint) {
      print("\u001b[2J")
      println(s"Period: $time")
    }
    printBoard()
    dut.clock.step(1)
    if (doPrint) {
      // Thread.sleep(300)
    }
  }
  println(s"Live: $liveCount, Dead: $deadCount")
}

object LifeRegression {
  def main(args: Array[String]): Unit = {
    val t = new Timer

    val (backendName, backendAnnotation) = if (args.nonEmpty) {
      ("verilator", VerilatorBackendAnnotation)
    } else {
      ("treadle", TreadleBackendAnnotation)
    }

    Driver.execute(Array("--backend-name", backendName), () => new Life(30, 30)) { dut =>
      t("iotesters,peek=false") {
        new LifeTests(dut, doPrint = false, doPeeks = false)
      }
      t("iotesters,peek=false") {
        new LifeTests(dut, doPrint = false, doPeeks = false)
      }
      t("iotesters,peek=false") {
        new LifeTests(dut, doPrint = false, doPeeks = false)
      }

      t("iotesters,peek=true") {
        new LifeTests(dut, doPrint = false, doPeeks = true)
      }
      t("iotesters,peek=true") {
        new LifeTests(dut, doPrint = false, doPeeks = true)
      }
      t("iotesters,peek=true") {
        new LifeTests(dut, doPrint = false, doPeeks = true)
      }
    }

    RawTester.test(new Life(30, 30), Seq(backendAnnotation)) { dut =>
      t("chiseltest:peek=false") {
        new ChiselTestLifeTest(dut, doPrint = false, doPeeks = false)
      }
      t("chiseltest:peek=false") {
        new ChiselTestLifeTest(dut, doPrint = false, doPeeks = false)
      }
      t("chiseltest:peek=false") {
        new ChiselTestLifeTest(dut, doPrint = false, doPeeks = false)
      }

      t("chiseltest:peek=true") {
        new ChiselTestLifeTest(dut, doPrint = false, doPeeks = true)
      }
      t("chiseltest:peek=true") {
        new ChiselTestLifeTest(dut, doPrint = false, doPeeks = true)
      }
      t("chiseltest:peek=true") {
        new ChiselTestLifeTest(dut, doPrint = false, doPeeks = true)
      }
    }
    println(t.report())
  }
}
