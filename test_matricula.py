import os
import requests

login = os.environ.get("LOGIN", "dev_daynnamonteiro@gmail.com")
senha = os.environ.get("SENHA", "exAhePK1dwar")

url_login = "https://erp-api-dev-922117522963.us-central1.run.app/api-external/v1/portal/auth/login"
resp = requests.post(url_login, json={"login": login, "senha": senha})
token = resp.json()["token"]

url_mat = "https://erp-api-dev-922117522963.us-central1.run.app/api-external/v1/portal/matricula/matricula-disciplina"
resp_mat = requests.get(url_mat, headers={"Authorization": f"Bearer {token}"})
print(resp_mat.json())
