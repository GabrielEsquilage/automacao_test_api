import os
import requests

login = os.environ.get("LOGIN", "dev_daynnamonteiro@gmail.com")
senha = os.environ.get("SENHA", "exAhePK1dwar")

url_login = "https://erp-api-dev-922117522963.us-central1.run.app/api-external/v1/portal/auth/login"
resp = requests.post(url_login, json={"login": login, "senha": senha})
token = resp.json()["token"]

url_mat = "https://erp-api-dev-922117522963.us-central1.run.app/api-external/v1/portal/matricula/matricula-disciplina"
resp_mat = requests.get(url_mat, headers={"Authorization": f"Bearer {token}"})
mat_id = resp_mat.json().get('cursosPos', [{'id': None}])[0]['id']
if not mat_id:
    mat_id = resp_mat.json().get('cursosGrad', [{'id': None}])[0]['id']

endpoints = [
    "/api-external/v1/portal/matricula/historico-completo",
    f"/api-external/v1/portal/matricula/disciplina-list/{mat_id}",
    "/api-external/v1/portal/matricula/coeficiente-rendimento"
]

for ep in endpoints:
    url = f"https://erp-api-dev-922117522963.us-central1.run.app{ep}"
    r = requests.get(url, headers={"Authorization": f"Bearer {token}", "matriculaId": str(mat_id)})
    print(f"{ep}: {r.status_code}")

