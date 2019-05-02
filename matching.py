import numpy as np
from scipy import misc
import matplotlib
import matplotlib.pyplot as plt
import sys
sys.path.insert(0, '$CAFFE_ROOT/python')
import caffe
import os
import pickle
import math
import csv
import copy
import allsharp_source.allsharp_selective_search as ss
from operator import add
from collections import defaultdict
# set constants
from google.cloud import vision
import io
import skimage.io

pretrained = 'allsharp_source/allsharp_vgg/allsharp_VGG_ILSVRC_16_layers.caffemodel'
model = 'allsharp_source/allsharp_vgg/allsharp_VGG_ILSVRC_16_layers_deploy.prototxt'

net = caffe.Net(model, pretrained, caffe.TEST)
dr_names = []
dr_results = []
with open('dr_names.pickle', 'rb') as handle:
    dr_names = pickle.load(handle)

text_to_file = ""
with open('text_file_mapping', 'rb') as handle:
    text_to_file = pickle.load(handle)

file_to_text = ""
with open('file_text_mapping', 'rb') as handle:
    file_to_text = pickle.load(handle)

with open('filename.pickle', 'rb') as handle:
    dr_results = pickle.load(handle)

class CNN:
    def __init__(self):

        self.img_size = 224
        self.output_blob_name = "fc8"        
        # initialize input image array
        self.dc_names = []
        caffe.set_mode_cpu()
        self.transformer = caffe.io.Transformer({'data': net.blobs['data'].data.shape})
        self.transformer.set_transpose('data', (2,1,0))
        self.dic = defaultdict(int)

    def match(self, dc_filepath):
        
        client = vision.ImageAnnotatorClient()
        content = ""
        with io.open(dc_filepath, 'rb') as image_file:
            content = image_file.read()
            
        image = vision.types.Image(content=content)
        result_text = []
        response = client.text_detection(image=image)
        if response.text_annotations:
            result_text = response.text_annotations[0].description.split("\n")
        
        result = []
        for text in result_text:
            result.append(text_to_file[text])
        if result:     
            result = set(result[0]).intersection(*result)
        
        if result:
            print(result)
            res = result.pop()
            print(file_to_text[res])
            if len(file_to_text[res]) == len(result_text):
                return res

        dc_name = dc_filepath.split('/')[-1]
        self.dc_names.append(dc_name)
        img = ss.get_ss_crop(dc_filepath)
        img = misc.imresize(img, (self.img_size, self.img_size))

        input_data = self.transformer.preprocess('data', img)
        net.blobs['data'].data[...] = np.array(input_data)
        output = net.forward()
        input_result = output[self.output_blob_name]
        # find the distances
        distances = [np.linalg.norm(input_result-dr_result) for dr_result in dr_results]

        # calculate ranking
        dist_array = np.array(distances)
        
        sorted_indices = dist_array.argsort().tolist()
        rank_lookup = {}
        rank = 1
        
        for i in sorted_indices:
            rank_lookup[dr_names[i]] = rank
            rank = rank + 1
        
        dr_reranked = []
        for dr_name, dr_rank in rank_lookup.iteritems():
            if dr_name in dr_reranked:
                continue
                
            # find the other name
            if "SF" in dr_name:
                dr_counterpart = dr_name.replace("SF", "SB")
            elif "SB" in dr_name:
                dr_counterpart = dr_name.replace("SB", "SF")
            else:
                dr_counterpart = dr_name
                
            if dr_counterpart in dr_reranked:
                continue
                
            if rank_lookup[dr_counterpart] < rank_lookup[dr_name]:
                #print dr_counterpart + "=" + str(rank_lookup[dr_counterpart]) + " has lower rank than this image, " + dr_name + "=" + str(rank_lookup[dr_name])
                rank_lookup[dr_name] = rank_lookup[dr_counterpart] + 0.5
            elif rank_lookup[dr_name] < rank_lookup[dr_counterpart]:
                rank_lookup[dr_counterpart] = rank_lookup[dr_name] + 0.5
            else:
                print "error! ranks are the same between image and counterpart."                
            dr_reranked.append(dr_name)
        
        adjusted_ranks = []
        for dr_name in dr_names:
            adjusted_ranks.append(rank_lookup[dr_name])
        rank_to_dr = {v: k for k, v in rank_lookup.iteritems()}
        self.dic[dc_name] = rank_to_dr[1]  

        rerank_dist_array = np.array(adjusted_ranks)
        sorted_indices = rerank_dist_array.argsort()
        ranks = np.empty(len(rerank_dist_array),int)
        ranks[sorted_indices] = np.add(np.arange(len(dist_array)), 1).tolist()

        for key, val in self.dic.iteritems():
            print str(key) + " : "+ val 
        print 'done.'
        return self.dic[dc_name].split("_")[0]