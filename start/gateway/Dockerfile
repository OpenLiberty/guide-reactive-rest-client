FROM openliberty/open-liberty:kernel-java8-openj9-ubi

ARG VERSION=1.0
ARG REVISION=SNAPSHOT

LABEL \
  org.opencontainers.image.authors="Your Name" \
  org.opencontainers.image.vendor="IBM" \
  org.opencontainers.image.url="local" \
  org.opencontainers.image.source="https://github.com/OpenLiberty/guide-jax-rs-reactive-client" \
  org.opencontainers.image.version="$VERSION" \
  org.opencontainers.image.revision="$REVISION" \
  vendor="Open Liberty" \
  name="gateway" \
  version="$VERSION-$REVISION" \
  summary="The gateway microservice from the Consuming RESTful services using reactive JAX-RS client guide" \
  description="This image contains the gateway microservice running with the Open Liberty runtime."

COPY --chown=1001:0 src/main/liberty/config /config/
COPY --chown=1001:0 target/gateway.war /config/apps

# It is recommended to run the configure.sh when build image for production.
# RUN configure.sh