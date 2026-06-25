import json
import os

openapi_file = 'openapi.json'
output_dir = 'src/test/resources/schemas/'

os.makedirs(output_dir, exist_ok=True)

with open(openapi_file, 'r') as f:
    data = json.load(f)

schemas = data.get('components', {}).get('schemas', {})

def fix_refs(obj):
    if isinstance(obj, dict):
        if '$ref' in obj and isinstance(obj['$ref'], str):
            if obj['$ref'].startswith('#/components/schemas/'):
                obj['$ref'] = obj['$ref'].replace('#/components/schemas/', '') + '.json'
        for k, v in obj.items():
            fix_refs(v)
    elif isinstance(obj, list):
        for item in obj:
            fix_refs(item)

count = 0
for schema_name, schema_body in schemas.items():
    if "DTO" in schema_name:
        # Create a clean JSON schema
        json_schema = {
            "$schema": "http://json-schema.org/draft-04/schema#",
            "title": schema_name
        }
        json_schema.update(schema_body)
        
        # Recursively update any $ref
        fix_refs(json_schema)
        
        with open(os.path.join(output_dir, f"{schema_name}.json"), 'w') as out:
            json.dump(json_schema, out, indent=2)
        count += 1

print(f"Extracted {count} DTO schemas successfully!")
