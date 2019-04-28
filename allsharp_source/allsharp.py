#!/usr/bin/env python

# Test siamese network for pill recognition. Loads images from a list file
#  and compare the results of passing those images through the network. Matching pairs
#  should have responses that are close together.
# Adam Allevato
# April 12, 2016

# load all the things
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
import allsharp_selective_search as ss
from operator import add
from collections import defaultdict
# set constants

img_size = 224
output_blob_name = "fc8"


# check command line arguments
if len(sys.argv) < 4:
	print 'Not enough command line arguments.'
	print 'usage is: allsharp.py DC DR output.csv'
	exit()

pretrained = 'allsharp_source/allsharp_vgg/allsharp_VGG_ILSVRC_16_layers.caffemodel'
model = 'allsharp_source/allsharp_vgg/allsharp_VGG_ILSVRC_16_layers_deploy.prototxt'
dc_path = sys.argv[1]
dr_path = sys.argv[2]
output_path = sys.argv[3]

MAX_IMGS = 20

# initialize input image array
dr_results = []
dr_names = []
dc_names = []
rank_matrix = []
rank_matrix.append([''])

caffe.set_mode_cpu()
net = caffe.Net(model, pretrained, caffe.TEST)

transformer = caffe.io.Transformer({'data': net.blobs['data'].data.shape})
transformer.set_transpose('data', (2,1,0))

print 'processing reference folder ' + dr_path + '. This could take some time, please wait....'
# read the image paths from the file
# dr_image_paths = []
# print dr_path
# for fn in os.listdir(dr_path):
#     dr_image_paths.append(fn)

# img_index = 0
# while img_index < len(dr_image_paths):
# 	img_count = 0
# 	input_imgs = []
# 	while img_count < MAX_IMGS and img_index < len(dr_image_paths):
# 		dr_filepath = dr_path + '/' + dr_image_paths[img_index]
			
# 		# put all the images into a single input array which will then be passed
# 		# through the network all at once. N images will be stored in an array
# 		# of size N x 3 x img_size x img_size. This is N images with 3 channels (RGB) that
# 		# are img_size x img_size pixels

# 		dr_name = dr_filepath.split('/')[-1]
# 		dr_names.append(dr_name)
# 		img_count = img_count + 1
# 		img = ss.get_ss_crop(dr_filepath)

# 		input_imgs.append(misc.imresize(img, (img_size, img_size)))
# 		rank_matrix[0].append(dr_name)
# 		img_index = img_index + 1
# 	if len(input_imgs) > 0:	
# 		input_data = []
# 		for i in range(len(input_imgs)):
# 			input_data.append(transformer.preprocess('data', input_imgs[i]))
		
# 		print 'found {} reference images'.format(len(input_imgs))
# 		input_data_np = np.array(input_data)
# 		net.blobs['data'].reshape(len(input_imgs), 3, img_size, img_size)
# 		net.reshape()
# 		net.blobs['data'].data[...] = input_data_np
# 		output = net.forward()
# 		print output
# 		dr_results.extend(copy.deepcopy(output[output_blob_name]))

# net.blobs['data'].reshape(1, 3, img_size, img_size)
# net.reshape()
# # now loop over input images


# with open('filename.pickle', 'wb') as handle:
#     pickle.dump(dr_results, handle, protocol=pickle.HIGHEST_PROTOCOL)

# with open('dr_names.pickle', 'wb') as handle:
#     pickle.dump(dr_names, handle, protocol=pickle.HIGHEST_PROTOCOL)

with open('dr_names.pickle', 'rb') as handle:
    dr_names = pickle.load(handle)

with open('filename.pickle', 'rb') as handle:
    dr_results = pickle.load(handle)
dic = defaultdict(int)
# net.save('test.caffemodel') 
# test = 'test.caffemodel'
# net = caffe.Net(model, test, caffe.TEST)
# transformer = caffe.io.Transformer({'data': net.blobs['data'].data.shape})
# transformer.set_transpose('data', (2,1,0))

# read the image paths from the file
print 'processing consumer folder ' + dc_path + '. This could take some time, please wait....'
# read the image paths from the file
dc_image_paths = []
for fn in os.listdir(dc_path):
    dc_image_paths.append(fn)

for i,input_item in enumerate(dc_image_paths):
	dc_filepath = dc_path + '/' + input_item
	dc_name = dc_filepath.split('/')[-1]
	dc_names.append(dc_name)
	if i%10 == 0:
		print 'processing input image ' + str(i) + '/' + str(len(dc_image_paths)) + ': '  + dc_name
	img = ss.get_ss_crop(dc_filepath)
	img = misc.imresize(img, (img_size, img_size))

	input_data = transformer.preprocess('data', img)
	net.blobs['data'].data[...] = np.array(input_data)
	output = net.forward()
	input_result = output[output_blob_name]

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
	dic[dc_name] = rank_to_dr[1]  

	rerank_dist_array = np.array(adjusted_ranks)
	sorted_indices = rerank_dist_array.argsort()
	ranks = np.empty(len(rerank_dist_array),int)
	ranks[sorted_indices] = np.add(np.arange(len(dist_array)), 1).tolist()
	


	#insert cr name and push to full ranking matrix
	ranks_list = ranks.tolist()
	ranks_list.insert(0,dc_name)
	rank_matrix.append(ranks_list)
	
print 'writing to output file ' + output_path

with open(output_path, 'w') as csvfile:
	thewriter = csv.writer(csvfile)
	for row in rank_matrix:
		thewriter.writerow(row)

for key, val in dic.iteritems():
	print str(key) + " : "+ val 
print 'done.'