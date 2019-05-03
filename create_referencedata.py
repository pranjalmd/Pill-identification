import os
from collections import defaultdict
import pickle

directory = "/home/mayur/CloudComputing2/dr/dr/"
data_text_to_file = defaultdict(set)
data_file_to_text = defaultdict(set)

files = []

for file in os.listdir(directory):
    # filename = os.fsdecode(file)
    print(file)
    if file.endswith(".res"):
        # if filename[0] == ".":
        #     filename = filename[2:]
        image_filename = os.path.join("/home/mayur/CloudComputing2/dr/dr", file)
        key = file.split("_")[0]
        flag = True
        with open(image_filename) as handler:
            for line in handler.readlines():
                line = line.rstrip("\n")
                lst = line.split(" ")
                flag = False
                for text in lst:
                    data_text_to_file[text].add(key)
                    data_file_to_text[key].add(text)
        if flag:
            files.append(image_filename)
# print(files, str(len(files)))with open("filedelete") as delete:
#     input = delete.read()
#     files = input.split(",")
#     for file1 in files:
#         # file1 = file1.strip("'")
#         print(file1[2:-5])
#         os.remove(file1[1:-5].replace("'", ""))

# with open("filedelete") as delete:
#     input = delete.read()
#     files = input.split(",")
#     for file1 in files:
#         # file1 = file1.strip("'")
#         print(file1[2:-5])
#         os.remove(file1[1:-5].replace("'", ""))
# print(data_text_to_file)            
pickle_out = open("text_file_mapping","wb")
pickle.dump(data_text_to_file, pickle_out)

pickle_out = open("file_text_mapping","wb")
pickle.dump(data_file_to_text, pickle_out)

# test = ""
# with open('referece_data', 'rb') as handle:
#     test = pickle.load(handle)

# print(test)