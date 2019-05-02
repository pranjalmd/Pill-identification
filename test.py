import http.client
import asyncio
import concurrent.futures
import requests, os, time

result = []

def test():
    print("Sending Request")
    url = "https://cloud-computing-238803.appspot.com/upload"
    #url = "http://localhost:5000/upload"
    image_filename = os.path.basename("/home/mayur/CloudComputing2/dc/2.jpeg")

    multipart_form_data = {
        'image': (image_filename, open("/home/mayur/CloudComputing2/dc/2.jpeg", 'rb'))
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
            for i in range(1000)
        ]
        for response in await asyncio.gather(*futures):
            pass
    
start = time.time()
loop = asyncio.get_event_loop()
loop.run_until_complete(main())
end = time.time() - start

print(end)
print("Total results = {0}".format(len(result)))