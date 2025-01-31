#!/bin/bash

filename=$1

mkdir -p bin

nasm -f elf64 -o bin/${filename}.o bin/${filename}.asm
gcc -Wall -Wextra -no-pie -o bin/${filename} bin/${filename}.o -lc
chmod +x bin/${filename}