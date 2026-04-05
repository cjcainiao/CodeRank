FROM alpine:latest

RUN apk update && \
    apk add bash && \
    apk add openjdk8

WORKDIR /workspace

COPY Script.sh .

ENTRYPOINT ["bash","Script.sh"]