FROM alpine:3.16

RUN  apk update \
  && apk upgrade \
  && apk add openjdk17 \
  && apk add maven \
  && rm -rf /var/cache/apk/*
  
WORKDIR /javadata
VOLUME ["/javadata"]
