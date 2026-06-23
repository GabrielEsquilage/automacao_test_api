import os
import re

TEST_DIR = 'src/test/java/api/listagens/'

def process_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # If it's already modified, skip
    if 'isA(java.util.List.class)' in content:
        return

    # Fix imports
    content = re.sub(r'import static org\.hamcrest\.Matchers\.notNullValue;', 'import static org.hamcrest.Matchers.*;', content)

    # Find blocks like:
    # .get("...")
    # .then()
    # .log().status()
    # .statusCode(200)
    # .body("$", notNullValue())
    
    # We will replace `.body("$", notNullValue())` based on the URL above it.
    
    # Strategy: 
    # Regex to find the whole request block up to extract
    pattern = r'(\.get\("([^"]+)"\)\s*\.then\(\)\s*\.log\(\)\.status\(\)\s*\.statusCode\(200\)\s*)\.body\("\$", notNullValue\(\)\)'
    
    def replacer(match):
        prefix = match.group(1)
        url = match.group(2)
        
        if '/list' in url or '/clean' in url or url.endswith('/all'):
            # It's a list
            replacement = """// Valida a estrutura
                .body("$", isA(java.util.List.class))
                .body("size()", greaterThan(0))
                .body("[0]", hasKey("id"))"""
        else:
            # Assume it's a page
            replacement = """// Valida a estrutura da paginação
                .body("$", hasKey("content"))
                .body("content", isA(java.util.List.class))
                .body("$", hasKey("totalElements"))
                .body("$", hasKey("totalPages"))"""
        return prefix + replacement

    new_content = re.sub(pattern, replacer, content)

    # Let's also clean up the try-catch for list sizes if any.
    # We can keep it or remove it. It's fine to keep it.
    
    if new_content != content:
        with open(filepath, 'w') as f:
            f.write(new_content)
        print(f"Updated {filepath}")

for root, dirs, files in os.walk(TEST_DIR):
    for f in files:
        if f.endswith('Test.java'):
            process_file(os.path.join(root, f))
