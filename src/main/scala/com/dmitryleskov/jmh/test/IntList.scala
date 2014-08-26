/*
 * Originally (c) 2014 Dmitry Leskov, http://www.dmitryleskov.com
 * Released into the public domain under the Unlicense, http://unlicense.org
 */

package com.dmitryleskov.jmh.test {

/** [[SimpleList]] specialized for `Int` */
  sealed abstract class IntList {
    def isEmpty: Boolean
    def head: Int
    def tail: IntList
    def length: Int = {
      var l = 0
      var scan = this
      while (!scan.isEmpty) {l += 1; scan = scan.tail}
      l
    }
  }

  case class IntCons (
    val head: Int,
    val tail: IntList
  ) extends IntList {
    def isEmpty = false
  }

  case object IntNil extends IntList {
    override def isEmpty = true
    override def head = throw new Error("Head of empty SimpleList")
    override def tail = throw new Error("Tail of empty SimpleList")
  }

  object IntList {
    def fill(n: Int)(elem: Int): IntList = {
      var sl: IntList = IntNil
      for (i <- 0 to n) {
        sl = IntCons(elem, sl)
      }
      sl
    }
    def range(f: Int, t: Int): IntList = {
      var il: IntList = IntNil
      for (i <- t to f by -1) {
        il = IntCons(i, il)
      }
      il
    }
  }
}
