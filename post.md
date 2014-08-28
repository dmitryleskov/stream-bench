While working on [Part III](!!!) of my *Scala Stream Hygiene* series,
I stumbled upon the implementation of weak memoization in
Scalaz `EphemeralStream`:

~~~ {.scala}
def cons[A](a: => A, as: => EphemeralStream[A]) = new EphemeralStream[A] {
  def isEmpty = false

  val head = weakMemo(a)
  val tail = weakMemo(as)
}
   .  .  .

def weakMemo[V](f: => V): () => V = {
  val latch = new Object
  // TODO I don't think this annotation does anything, as `v` isn't a class member.
  @volatile var v: Option[WeakReference[V]] = None
  () => {
    val a = v.map(x => x.get)
    if (a.isDefined && a.get != null) a.get
    else latch.synchronized {
      val x = f
      v = Some(new WeakReference(x))
      x
    }
  }
}
~~~

As you may see, `weakMemo()` returns a closure with three free variables:
`f`, `latch` and `v`, the latter being a `VolatileObjectRef` to an `Option` that
contains a `WeakReference` to the object being memoized. And a cons cell
contains *two* such closures:

![EphemeralStream cons cell class diagram](EphemeralStream.png)

This translates to 192 bytes of fixed overhead per cons cell (232 bytes on 64-bit,
all figures for Oracle JRE 7u55 with default settings), plus sizes of the type `V`
itself and the closures created for the by-name `cons` parameters. 
For instance, traversal of an `EphemeralStream[Int].range(a, b)` allocates 
256 bytes from the heap per cached object. That's 64 (sixty-four) times the
size of an `Int`!

On the one hand, the garbage collector does not follow weak references, so
it effectively swipes an entire `EphemeralStream` at once when invoked. On the
other hand, all these objects (fourteen per cons cell) pollute the heap, so
the GC is invoked more often.

What about the standard `Stream` class? The most trivial case is 
`Stream.continually(x)`, and it seems to need just 40 bytes per cons cell, 
both on 32-bit and 64-bit HotSpot. But actually that is just the overhead, 
because `x` is not duplicated. Where does that overhead come from?

When a stream cell has its `tail` evaluated for the first time, it caches the result:

~~~ {.scala}
  /** A lazy cons cell, from which streams are built. */
  @SerialVersionUID(-602202424901551803L)
  final class Cons[+A](hd: A, tl: => Stream[A]) extends Stream[A] with Serializable {
    override def isEmpty = false
    override def head = hd
    @volatile private[this] var tlVal: Stream[A] = _
    def tailDefined: Boolean = tlVal ne null
    override def tail: Stream[A] = {
      if (!tailDefined)
        synchronized {
          if (!tailDefined) tlVal = tl
        }

      tlVal
    }
  }
~~~

After that, the `tl` closure is no longer needed, but because `tl` is a `val`,
it cannot be assigned `null` so as to have the GC free the closure object.

In an unsafe language, we could have gone even further and make 
`tl` and `tlVal` fields of a (tagged) union type. E.g. if Modula-2 had
generics, we could have written something like:

~~~ {.mod}
TYPE Cons[T] = 
  RECORD 
    CASE isEmpty: BOOLEAN OF 
    |TRUE: /* nothing */
    |FALSE: 
      CASE tlDefined: BOOLEAN OF
      |FALSE: tl: PROCEDURE(): Cons[T];
      |TRUE: tlVal: T;
      END;
    END;
  END;
~~~

Without taking alignment into account, a `Cons[T]` would need at most `SIZE(T)`
plus two bytes (Okay, a thread-safe version would need an extra field for 
something to synchronize on.) But I digress.

