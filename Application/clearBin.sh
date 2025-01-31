#!/bin/bash

# Define the directory
dir="./bin/"

# Check if directory exists
if [ -d "$dir" ]; then
    # Remove all files in directory
    rm -rf "$dir"/*
    echo "All files in $dir have been removed."
else
    echo "Directory $dir does not exist."
fi