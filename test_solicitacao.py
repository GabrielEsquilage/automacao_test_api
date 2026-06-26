import os
import requests

login = os.environ.get("LOGIN", "dev_daynnamonteiro@gmail.com")
senha = os.environ.get("SENHA", "exAhePK1dwar")

url_login = "https://erp-api-dev-922117522963.us-central1.run.app/api-external/v1/portal/auth/login"
resp = requests.post(url_login, json={"login": login, "senha": senha})
token = resp.json()["token"]

# fetch matricula
url_mat = "https://erp-api-dev-922117522963.us-central1.run.app/api-external/v1/portal/matricula/matricula-disciplina"
resp_mat = requests.get(url_mat, headers={"Authorization": f"Bearer {token}"})
mat_id = resp_mat.json().get('cursosPos', [{'id': None}])[0]['id']
if not mat_id:
    mat_id = resp_mat.json().get('cursosGrad', [{'id': None}])[0]['id']

url_sol = "https://erp-api-dev-922117522963.us-central1.run.app/api-external/v1/portal/solicitacao"
resp_sol = requests.get(url_sol, headers={"Authorization": f"Bearer {token}", "matriculaId": str(mat_id)})
print("Solicitacao Status:", resp_sol.status_code)
# print(resp_sol.text) # Too long possibly, just want the status code
