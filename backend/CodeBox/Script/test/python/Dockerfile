FROM alpine:latest

RUN apk update && \
    apk add bash && \
    apk add python3

WORKDIR /workspace

COPY Script.sh .

ENTRYPOINT ["bash","Script.sh"]