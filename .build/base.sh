#!/bin/bash

if docker inspect base-image:1.0 &> /dev/null; then
    echo "Image base-image exists."
else
    echo "Building image base-image..."
    docker build -t base-image:1.0 .
fi