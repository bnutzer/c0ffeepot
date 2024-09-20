#!/bin/sh

# docker buildx create --name multiarchbuilder --use --bootstrap

docker buildx build \
	--platform linux/arm/v7,linux/arm64/v8,linux/amd64 \
	--tag bnutzer/c0ffeepot \
	.
