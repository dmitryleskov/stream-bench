java -jar target/microbenchmarks.jar ^
  -jvmArgs "-server -Xcomp" ^
  -w 5 -r 5 -f 1 -wi 5 -i 10 -tu s ^
  -o Reduction.out -rf csv -rff Reduction.csv ^
  -p problemSize=1048576 ^
  ".*Traversal.*ManualSum"
rem  -p problemSize=1048576 ^
rem -gc true 