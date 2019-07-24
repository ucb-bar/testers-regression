// See README.md for license details.

package gcd

object SimpleTimer {
  def apply(name: String)(thunk: => Unit): Unit = {
    val startTime = System.nanoTime()

    thunk

    val endTime = System.nanoTime()

    val elapsedSeconds = (endTime - startTime).toDouble / 1000000000.0

    println(f":::::: $name%-40.40s ${elapsedSeconds}%10.5f seconds")
  }
}
