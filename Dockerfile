FROM bvlc/caffe:cpu

COPY requirement.txt .

RUN apt-get update && \
    apt-get install -y python-tk && \
    rm -rf /var/lib/apt/lists/* && \
    pip install -r requirement.txt
