import logging
import time
import os
from flask import Flask, request, jsonify
from matching import CNN
from helper import get_pill_details
import tempfile, json
import uuid

app = Flask(__name__)
UPLOAD_FOLDER = os.path.basename('uploads')
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER


@app.route('/')
def hello():
    time.sleep(4)
    return 'Hello World!'

@app.route('/upload', methods=['POST'])
def upload_file():
    file = request.files['image']
    filename = uuid.uuid4().hex
    f = open(os.path.join(UPLOAD_FOLDER, filename), "w+b")
    print(f.name)
    filename = f.name.split("/")[-1]
    # add your custom code to check that the uploaded file is a valid image and not a malicious file (out-of-scope for this post)
    file.save(f)
    model = CNN()
    f.close()
    dectected_file = model.match(f.name)
    # dectected_file = dectected_dict[filename]
    # ndc_code_parts = dectected_file.split("_")
    # ndc_code = ndc_code_parts[0]
    return jsonify(get_pill_details(dectected_file))

@app.errorhandler(500)
def server_error(e):
    # Log the error and stacktrace.
    logging.exception('An error occurred during a request.')
    return 'An internal error occurred.', 500
# [END app]