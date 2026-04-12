filepath = r'C:/Users/MS/Desktop/SC/SmartCity/pom.xml'
with open(filepath, 'r', encoding='utf-8', errors='replace') as f:
    content = f.read()

import re
# Find maven-compiler-plugin section
idx = content.find('maven-compiler-plugin')
if idx != -1:
    print('compiler plugin at:', idx)
    print(content[idx:idx+600])
print('---')
# Find javafx plugin
idx2 = content.find('javafx-maven-plugin')
if idx2 != -1:
    print('javafx plugin at:', idx2)
    print(content[idx2:idx2+600])
print('---')
# Find any existing add-exports
for m in re.finditer(r'add-exports|add-opens|compilerArg', content):
    print('found:', m.group(), 'at', m.start())
    print(content[m.start():m.start()+100])
