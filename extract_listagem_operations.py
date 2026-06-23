import json
import re

# Read current state
with open('openapi.json', 'r') as f:
    main_data = json.load(f)
    
try:
    with open('openapi_listagem.json', 'r') as f:
        list_data = json.load(f)
except Exception:
    list_data = {"paths": {}}

# Merge back into one big data dictionary
all_paths = {}
for p, p_item in main_data.get("paths", {}).items():
    if p not in all_paths:
        all_paths[p] = {}
    all_paths[p].update(p_item)

for p, p_item in list_data.get("paths", {}).items():
    if p not in all_paths:
        all_paths[p] = {}
    all_paths[p].update(p_item)

main_data["paths"] = all_paths

listagem_data = {
    "openapi": main_data.get("openapi"),
    "info": main_data.get("info"),
    "servers": main_data.get("servers"),
    "paths": {},
    "components": main_data.get("components")
}

def is_list_operation(method, op, path):
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

    # If it's a POST and it ends in /list or /page or /search, it's also a search/list
    if method == "post" and (path.endswith("/list") or path.endswith("/page") or path.endswith("/search")):
        return True

    return False

# Now split by METHOD
paths_to_keep_main = {}
paths_to_keep_list = {}

count_list = 0

for path, path_item in list(main_data["paths"].items()):
    for method, op in path_item.items():
        if is_list_operation(method, op, path):
            if path not in paths_to_keep_list:
                paths_to_keep_list[path] = {}
            paths_to_keep_list[path][method] = op
            count_list += 1
        else:
            if path not in paths_to_keep_main:
                paths_to_keep_main[path] = {}
            paths_to_keep_main[path][method] = op

main_data["paths"] = paths_to_keep_main
listagem_data["paths"] = paths_to_keep_list

with open('openapi_listagem.json', 'w') as f:
    json.dump(listagem_data, f, indent=2)

with open('openapi.json', 'w') as f:
    json.dump(main_data, f, indent=2)

print(f"Extracted {count_list} list operations to openapi_listagem.json")
