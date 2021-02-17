Chisel Testers Regression
=======================

Uses a GCD implementation to compare testers and testers2.
Testers2 includes two tests one simple, the other uses the decoupled helpers.

Running
=======

Run the simple report script
```
./run_all.sh
```

Creating Flamegraphs
====================

You will need copies of:
- https://github.com/cykl/hprof2flamegraph
- https://github.com/brendangregg/FlameGraph

Now you can create a flame graph like this (only tested with OpenJDK 8,
does not work with OpenJDK 11):

```
# build the JAR
sbt assembly
# sample function call stack while running the test
java -agentlib:hprof=cpu=samples,depth=400,interval=100,lineno=y,thread=y,file=out.hprof -cp ./target/scala-2.12/testers-regression-assembly-3.2-SNAPSHOT.jar test.RunRegression io-testers treadle 5
# convert to SVG
../hprof2flamegraph/stackcollapse_hprof.py out.hprof > out-folded.txt
../FlameGraph/flamegraph.pl out-folded.txt > out.svg
# open the flamegraph
firefox out.svg
```


## License
This is free and unencumbered software released into the public domain.

Anyone is free to copy, modify, publish, use, compile, sell, or
distribute this software, either in source code form or as a compiled
binary, for any purpose, commercial or non-commercial, and by any
means.

In jurisdictions that recognize copyright laws, the author or authors
of this software dedicate any and all copyright interest in the
software to the public domain. We make this dedication for the benefit
of the public at large and to the detriment of our heirs and
successors. We intend this dedication to be an overt act of
relinquishment in perpetuity of all present and future rights to this
software under copyright law.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

For more information, please refer to <http://unlicense.org/>
