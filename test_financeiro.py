import os
import requests

login = os.environ.get("LOGIN", "dev_daynnamonteiro@gmail.com")
senha = os.environ.get("SENHA", "exAhePK1dwar")

url_login = "https://erp-api-dev-922117522963.us-central1.run.app/api-external/v1/portal/auth/login"
resp = requests.post(url_login, json={"login": login, "senha": senha})
token = resp.json()["token"]

url_fin = "https://erp-api-dev-922117522963.us-central1.run.app/api-external/v1/portal/financeiro"
resp_fin = requests.get(url_fin, headers={"Authorization": f"Bearer {token}", "matriculaId": "1119420"})
print("Status:", resp_fin.status_code)
print("Body:", resp_fin.text)
