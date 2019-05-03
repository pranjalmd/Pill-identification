import http.client
import asyncio
import concurrent.futures
import requests, os, time, json

result = []

def test2():
    # print("Sending Request")
    # url = "https://cloud-computing-238803.appspot.com/upload"
    url = "http://localhost:5000/upload"
    directory = os.fsencode("/home/mayur/CloudComputing2/dc/")

    for file in os.listdir(directory):
        filename = os.fsdecode(file)
        print(file)
        if filename.endswith(".jpeg") or filename.endswith(".jpg"):
            if filename[0] == ".":
                filename = filename[2:]
            image_filename = os.path.join("/home/mayur/CloudComputing2/dc", filename)
            print(image_filename)
            multipart_form_data = {
                'image': (filename, open(image_filename, 'rb'))
            }

            response = requests.post(url,
                                    files=multipart_form_data)

            # print(response.text)
            result = json.loads(response.text)
            if result:
                print(filename + "-->" + result["NDC11Code"])

def test():
    print("Sending Request")
    url = "https://cloud-computing-238803.appspot.com/upload"
    # url = "http://localhost:5000/upload"
    image_filename = os.path.basename("/home/mayur/CloudComputing2/dc/2891.jpg")

    multipart_form_data = {
        'image': (image_filename, open("/home/mayur/CloudComputing2/dc/2891.jpg", 'rb'))
    }

    response = requests.post(url,
                             files=multipart_form_data)

    print(response.text)
    result.append(response.text)




async def main():
    
    with concurrent.futures.ThreadPoolExecutor(max_workers=20) as executor:

        loop = asyncio.get_event_loop()
        futures = [
            loop.run_in_executor(
                executor, 
                test
            )
            for i in range(100)
        ]
        for response in await asyncio.gather(*futures):
            pass
    
start = time.time()
loop = asyncio.get_event_loop()
loop.run_until_complete(main())
end = time.time() - start

print(end)
print("Total results = {0}".format(len(result)))