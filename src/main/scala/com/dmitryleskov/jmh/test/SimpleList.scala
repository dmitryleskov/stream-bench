/*
 * Originally (c) 2014 Dmitry Leskov, http://www.dmitryleskov.com
 * Released into the public domain under the Unlicense, http://unlicense.org
 */

/** Bare-bones implementation of a generic immutable list */

package com.dmitryleskov.jmh.test {

  sealed abstract class SimpleList[+A]{
    def isEmpty: Boolean
    def head: A
    def tail: SimpleList[A]
    def length: Int = {
      var l = 0
      var scan = this
      while (!scan.isEmpty) {l += 1; scan = scan.tail}
      l
    }
  }

  case class SimpleCons[A] (
    val head: A,
    val tail: SimpleList[A]
  ) extends SimpleList[A] {
    def isEmpty = false
  }

  case object SimpleNil extends SimpleList[Nothing] {
    override def isEmpty = true
    override def head = throw new Error("Head of empty SimpleList")
    override def tail = throw new Error("Tail of empty SimpleList")
  }

  object SimpleList {
    def fill[A](n: Int)(elem: A): SimpleList[A] = {
      var sl: SimpleList[A] = SimpleNil
      for (i <- 0 to n) {
        sl = SimpleCons[A](elem, sl)
      }
      sl
    }
    def range(f: Int, t: Int): SimpleList[Int] = {
      var sl: SimpleList[Int] = SimpleNil
      for (i <- t to f by -1) {
        sl = SimpleCons[Int](i, sl)
      }
      sl
    }
  }

}
