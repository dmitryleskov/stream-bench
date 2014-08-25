for %%c in (List, Vector, Stream, EphemeralStream, SimpleList, IntList) do (
java -jar target/microbenchmarks.jar ^
  -jvmArgs "-server -Xcomp -XX:+PrintGCDetails -Xloggc:mc-%%c.gclog" ^
  -bm ss ^
  .*MemoryConsumption.test%%c
)
