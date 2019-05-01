import http.client
import asyncio
import concurrent.futures
import requests


def test():
    conn = http.client.HTTPSConnection("20190430t232440-dot-cloud-computing-238803.appspot.com")

    payload = "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"image\"; filename=\"2.jpeg\"\r\nContent-Type: image/jpeg\r\n\r\n\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW--"

    headers = {
        'content-type': "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW",
        'cache-control': "no-cache",
        'postman-token': "91d273ef-6d22-e8fb-2bc1-682673245f43"
        }

    conn.request("POST", "/upload", payload, headers)

    res = conn.getresponse()
    data = res.read()

    print(data.decode("utf-8"))



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