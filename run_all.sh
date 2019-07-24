#!/usr/bin/env bash

sbt test > test.out

grep :::: test.out