import json
import re

with open('openapi.json', 'r') as f:
    data = json.load(f)

listagem_data = {
    "openapi": data.get("openapi"),
    "info": data.get("info"),
    "servers": data.get("servers"),
    "paths": {},
    "components": data.get("components")
}

paths_to_remove = []

def is_list_endpoint(path, path_item):
    for method, op in path_item.items():
        op_id = op.get("operationId", "")
        if re.search(r'(?i)(list|page|findall|buscartodos|getall)', op_id):
            return True
        
        # Check responses for array or Page/List references
        responses = op.get("responses", {})
        for code, resp in responses.items():
            content = resp.get("content", {})
            for ctype, cdetails in content.items():
                schema = cdetails.get("schema", {})
                if schema.get("type") == "array":
                    return True
                ref = schema.get("$ref", "")
                if "Page" in ref or "List" in ref:
                    return True
                
        # Also, check if path itself screams list
        if method == "get" and (path.endswith("/list") or path.endswith("/page") or path.endswith("/all")):
            return True

    return False

for path, path_item in list(data["paths"].items()):
    if is_list_endpoint(path, path_item):
        listagem_data["paths"][path] = path_item
        paths_to_remove.append(path)

for path in paths_to_remove:
    del data["paths"][path]

with open('openapi_listagem.json', 'w') as f:
    json.dump(listagem_data, f, indent=2)

with open('openapi.json', 'w') as f:
    json.dump(data, f, indent=2)

print(f"Extracted {len(paths_to_remove)} endpoints to openapi_listagem.json")
