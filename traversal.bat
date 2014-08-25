java -jar target/microbenchmarks.jar ^
  -jvmArgs "-server -Xcomp" ^
  -w 5 -r 5 -f 1 -wi 5 -i 10 -tu s ^
  -o Traversal.out -rf csv -rff Traversal.csv ^
  -p problemSize=1048576 ^
  ".*Traversal.*Length"
rem  -p problemSize=1048576 ^
rem -gc true 