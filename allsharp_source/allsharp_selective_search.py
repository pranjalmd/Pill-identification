#!/usr/bin/env python
# -*- coding: utf-8 -*-
import skimage.data
import skimage.io
from scipy import misc
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
import selectivesearch
import sys
import numpy as np
import skimage.exposure as exposure
import os  

SIGMA = 0.7
SCALE = 1000
MIN_SIZE = 100

WINDOW_EXPAND = 1.2
IMG_SIZE = 400

def main():
    image_names = []
    basepath = os.path.dirname(os.path.realpath(__file__)) + '/data/dc'
    for fn in os.listdir(basepath):
        image_names.append(fn)

    for i in range(1,100):
        print image_names[i]
        img = skimage.io.imread(basepath + '/' + image_names[i])
        candidates = get_ss_crops(img)
        # draw rectangles on the original image
        fig, ax = plt.subplots(ncols=1, nrows=1, figsize=(6, 6))
        
        ax.imshow(img)
        for x, y, w, h in candidates:
            print x, y, w, h
            rect = mpatches.Rectangle(
                (x, y), w, h, fill=False, edgecolor='red', linewidth=1)
            ax.add_patch(rect)

        plt.show()

def get_ss_crop(filename):
    img = skimage.io.imread(filename)

    candidates = get_ss_crops(img)

    x = 0
    y = 0
    h,w,channels = img.shape
    if len(candidates) > 0:
        x,y,w,h = candidates[-1] # take the last one, I guess. Arbitrary
    return img[int(y):int(y+h),int(x):int(x+w)]

def get_ss_crops(img):
    orig_img_height, orig_img_width, channels = img.shape
    #print 'orig: ', str(orig_img_width), str(orig_img_height)
    img = misc.imresize(img, (IMG_SIZE, IMG_SIZE))
    img_height, img_width, channels = img.shape

    #print float(img.size)
    #print 'max size', float(img.size)/3*0.8
    #print 'min size', float(img.size)/3*0.005

    img_exp = img
    #img_exp = exposure.equalize_adapthist(img, clip_limit=0.03)
    
    # perform selective search
    img_lbl, regions = selectivesearch.selective_search(
        img_exp, scale=SCALE, sigma=SIGMA, min_size=MIN_SIZE)

    candidates = set()
    for r in regions:
        # excluding same rectangle (with different segments)
        if r['rect'] in candidates:
            continue

        # distorted rects

        x, y, w, h = r['rect']
        if h == 0 or w == 0:
            continue
        if w / h > 2 or h / w > 2:
            continue
        if w > img_width*0.9 or h > img_height*0.9:
            continue

        rect_size = w*h
        #print rect_size
        #print x, y, w, h


        if rect_size > float(img.size)/3*0.8:
            #print 'too big'
            continue
        if rect_size < float(img.size)/3*0.005:
            #print 'too small'
            continue
        center_x = x + w/2
        center_y = y + h/2
        
        if center_x < img_width*0.15 or center_x > img_width*0.85 or center_y < img_height*0.15 or center_y > img_height*0.85:
            #print 'off edges'
            continue

        # non-max suppression
        candidates = [s for s in candidates if not (s[0] >= x or s[1] >= y or s[2] <= w or s[3] <= h)]

        too_small = False
        for larger in candidates:
            if larger[0] <= x and larger[1] <= y and larger[2] >= w and larger[3] >= h:

                #print x, y, w, h, " too small"
                too_small = True
        if too_small:
            continue

        new_width = int(w*WINDOW_EXPAND)
        new_height = int(h*WINDOW_EXPAND)
        x = max(0, center_x-new_width/2)
        y = max(0, center_y-new_height/2)
        w = min(img_width-x, new_width)
        h = min(img_height-y, new_height)
        #print x, y, w, h

        x_scale_factor = float(orig_img_width)/IMG_SIZE
        y_scale_factor = float(orig_img_height)/IMG_SIZE
        #print x_scale_factor, y_scale_factor
        x_scaled = x * x_scale_factor
        y_scaled = y * y_scale_factor
        w_scaled = w * x_scale_factor
        h_scaled = h * y_scale_factor

        candidates.append((x_scaled, y_scaled, w_scaled, h_scaled))
    return candidates

if __name__ == "__main__":
    main()
