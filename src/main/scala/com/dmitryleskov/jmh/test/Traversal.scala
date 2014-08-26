/*
 * Originally (c) 2014 Dmitry Leskov, http://www.dmitryleskov.com
 * Released into the public domain under the Unlicense, http://unlicense.org
 *
 * Reducing maximum heap size (-Xmx) setting will cause the failing tests
 * to fail sooner, reducing the overall run time.
 */

package com.dmitryleskov.jmh.test {

import org.openjdk.jmh.annotations._
import scala.annotation.tailrec
import scalaz.EphemeralStream
  
  /** Tests to compare the traversal overheads of various strict and lazy
   *  linear Scala collections - standard, third-party, and written by hand.
   *
   *  The `*Length` tests only traverse the respective collection without
   *  accessing the stored elements.
   *
   *  The `*Sum` tests compute a sum of all elements.
   */
  @Warmup(iterations = 5)
  @Measurement(iterations = 10)
  @Fork(1)
  @State(Scope.Benchmark)
  class Traversal {
    
    @Param(Array("10000", "100000", "1000000", "10000000"))
    var problemSize: Int = _

    def il: IntList = IntList.fill(problemSize)(1)
    def sl: SimpleList[Int] = SimpleList.fill(problemSize)(1)
    def l: List[Int] = List.fill(problemSize)(1)
    def s: Stream[Int] = {
      def s1: Stream[Int] = 1 #:: s1
      s1 take problemSize
    }
    def es: EphemeralStream[Int] = {
      def e1: EphemeralStream[Int] = 1 ##:: e1
      e1 take problemSize
    }
    
    @Setup
    def setup = {}

    @TearDown
    def tearDown = {
      System.gc
    }
    
    @GenerateMicroBenchmark
    def testIntListLength(): Any = {
      il.length
    }
    @GenerateMicroBenchmark
    def testSimpleListLength(): Any = {
      sl.length
    }
    @GenerateMicroBenchmark
    def testListLength(): Any = {
      l.length
    }
    @GenerateMicroBenchmark
    def testStreamLength(): Any = {
      s.length
    }
    @GenerateMicroBenchmark
    def testEphemeralStreamLength(): Any = {
      es.length
    }
    
    @GenerateMicroBenchmark
    def testListManualSum(): Any = {
      def sum(xs: List[Int]): Int = {
        @tailrec
        def loop(acc: Int, xs: List[Int]): Int =
          if (xs.isEmpty) acc else loop(acc+xs.head, xs.tail)
        loop(0, xs)
      }
      sum(l)
    }
    @GenerateMicroBenchmark
    def testStreamManualSum(): Any = {
      def sum(xs: Stream[Int]): Int = {
        @tailrec
        def loop(acc: Int, xs: Stream[Int]): Int =
          if (xs.isEmpty) acc else loop(acc+xs.head, xs.tail)
        loop(0, xs)
      }
      sum(s)
    }
    @GenerateMicroBenchmark
    def testEphemeralStreamSum(): Any = {
      es.foldLeft(0)(x => y => x + y)
    }
    @GenerateMicroBenchmark
    def testEphemeralStreamManualSum(): Any = {
      def sum(xs: EphemeralStream[Int]): Int = {
        @tailrec
        def loop(acc: Int, xs: EphemeralStream[Int]): Int =
          if (xs.isEmpty) acc else loop(acc+xs.head(), xs.tail())
        loop(0, xs)
      }
      sum(es)
    }
  }
}
