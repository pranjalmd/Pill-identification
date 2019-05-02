FROM bvlc/caffe:cpu

COPY requirement.txt .

ADD . /workspace/

RUN mkdir /workspace/allsharp_source/allsharp_vgg && \
    apt-get update && \
    apt-get install -y python-tk && \
    rm -rf /var/lib/apt/lists/* && \
    pip install --upgrade pip && \
    pip install -r requirement.txt && \
    gsutil cp gs://cloud-computing-238803.appspot.com/allsharp_VGG_ILSVRC_16_layers_deploy.prototxt /workspace/allsharp_source/allsharp_vgg/ && \
    gsutil cp gs://cloud-computing-238803.appspot.com/allsharp_VGG_ILSVRC_16_layers.caffemodel /workspace/allsharp_source/allsharp_vgg/

ENV FLASK_APP main.py
ENV GOOGLE_APPLICATION_CREDENTIALS cloud-computing-4c1713408cea.json

EXPOSE 8080

CMD flask run --host=0.0.0.0 --port=8080