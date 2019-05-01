import http.client
import asyncio
import concurrent.futures
import requests, os


def test():
    print("Sending Request")
    # url = "https://cloud-computing-238803.appspot.com/upload"
    url = "http://localhost:8080/upload"
    image_filename = os.path.basename("/home/mayur/CloudComputing2/dc/2.jpeg")

    multipart_form_data = {
        'image': (image_filename, open("/home/mayur/CloudComputing2/dc/2.jpeg", 'rb'))
    }

    response = requests.post(url,
                             files=multipart_form_data)

    print(response.text)



async def main():

    with concurrent.futures.ThreadPoolExecutor(max_workers=20) as executor:

        loop = asyncio.get_event_loop()
        futures = [
            loop.run_in_executor(
                executor, 
                test
            )
            for i in range(20)
        ]
        for response in await asyncio.gather(*futures):
            pass


loop = asyncio.get_event_loop()
loop.run_until_complete(main())