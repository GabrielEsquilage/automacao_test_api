import os
import requests

login = os.environ.get("LOGIN", "admin")
senha = os.environ.get("SENHA", "7Y/6p0p\iYd{")

url_login = "https://erp-api-dev-922117522963.us-central1.run.app/api/v1/app/auth/login"
resp = requests.post(url_login, data={"login": login, "password": senha})
token = resp.json()["access_token"]

url_atual = "https://erp-api-dev-922117522963.us-central1.run.app/api/v1/usuario/atual"
r = requests.get(url_atual, headers={"Authorization": f"Bearer {token}"})
print(f"Status: {r.status_code}")
if r.status_code == 200:
    print(r.json())
