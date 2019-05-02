import requests
import json
def get_pill_details(ndc_code):
    print("NDC CODE To search {0}".format(ndc_code))
    r = requests.get('http://www.hipaaspace.com/api/ndc/search?q='+ ndc_code + '&rt=json&token=3932f3b0-cfab-11dc-95ff-0800200c9a663932f3b0-cfab-11dc-95ff-0800200c9a66')
    response_obj = json.loads(r.text)
    drug_pill = response_obj['NDC'][0]
    if drug_pill['NDC11Code'] == ndc_code:
        return drug_pill
    print(response_obj['NDC'][0])
    return None

if __name__ == "__main__" :
    drug_pill = get_pill_details("")
    print(drug_pill)
