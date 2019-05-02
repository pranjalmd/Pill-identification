import os
from collections import defaultdict
import pickle

directory = "/home/mayur/CloudComputing2/dr/dr/"
data_text_to_file = defaultdict(set)
data_file_to_text = defaultdict(set)

for file in os.listdir(directory):
    # filename = os.fsdecode(file)
    print(file)
    if file.endswith(".res"):
        # if filename[0] == ".":
        #     filename = filename[2:]
        image_filename = os.path.join("/home/mayur/CloudComputing2/dr/dr", file)
        key = file.split("_")[0]
        with open(image_filename) as handler:
            for line in handler.readlines():
                data_text_to_file[line.rstrip("\n")].add(key)
                data_file_to_text[key].add(line.rstrip("\n"))

print(data_text_to_file)            
pickle_out = open("text_file_mapping","wb")
pickle.dump(data_text_to_file, pickle_out)

pickle_out = open("file_text_mapping","wb")
pickle.dump(data_file_to_text, pickle_out)

# test = ""
# with open('referece_data', 'rb') as handle:
#     test = pickle.load(handle)

# print(test)