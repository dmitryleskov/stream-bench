/*
 * Originally (c) 2014 Dmitry Leskov, http://www.dmitryleskov.com
 * Released into the public domain under the Unlicense, http://unlicense.org
 */

package com.dmitryleskov.jmh.test {

  import org.openjdk.jmh.annotations._
  import scala.annotation.tailrec
  import scalaz.EphemeralStream

/** Tests to compare the amount of memory consumed by various strict and lazy
 *  linear Scala collections - standard, third-party, and written by hand.
 *
 *  Run with `"-XX:+PrintGCDetails -Xloggc:gc.log"` separately for each collection,
 *  then load `gc.log` into [[http://www.tagtraum.com/gcviewer.html] GCViewer]]
 *  or some other tool to calculate the total amount of memory *freed* by GC,
 *  in ''megabytes''. Storing a single `Int` in the given collection requires
 *  approximately the same amount of thousands of ''bytes'', because on each
 *  of the thousand iterations 1024^2 `Int`s get placed into a new collection.
 */
  @Warmup(iterations = 0)
  @Measurement(iterations = 1000)
  @Fork(1)
//  @BenchmarkMode(Mode.SingleShotTime)
  @State(Scope.Benchmark)
  class MemoryConsumption {
    
    val problemSize: Int = 1024 * 1024;
    
    def il: IntList = IntList.range(1, problemSize)
    def sl: SimpleList[Int] = SimpleList.range(1, problemSize)
    def l: List[Int] = List.range(1, problemSize)
    def v: Vector[Int] = Vector.range(1, problemSize)
    def s: Stream[Int] = Stream.range(1, problemSize)
    def es: EphemeralStream[Int] = EphemeralStream.range(1, problemSize)
    
    @Setup
    def setup = {}

    @TearDown
    def tearDown = {
      System.gc
    }
    
    @GenerateMicroBenchmark
    def testIntList(): Any = il
    
    @GenerateMicroBenchmark
    def testSimpleList(): Any = sl

    @GenerateMicroBenchmark
    def testList(): Any = l

    @GenerateMicroBenchmark
    def testVector(): Any = v

    @GenerateMicroBenchmark
    def testStream(): Any = s.force

    @GenerateMicroBenchmark
    def testEphemeralStream(): Any = {
      // Make sure each element gets computed
      def force(xs: EphemeralStream[Int]): Unit = {
        @tailrec
        def loop(acc: Int, xs: EphemeralStream[Int]): Int =
          if (xs.isEmpty) acc else loop(acc + xs.head(), xs.tail())
        loop(0, xs)
      }
      force(es)
    }
    
  }
}
