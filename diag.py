# -*- coding: utf-8 -*-

# Diagnostic Agent ligne 1290
fag = 'src/main/java/com/smartcity/controller/AgentDashboardController.java'
lines = open(fag, encoding='utf-8').readlines()
print('Agent line 1288-1292:')
for i in range(1287, min(1293, len(lines))):
    print(f'  {i+1}: {repr(lines[i].rstrip())}')

# Chercher où affectationService est déclaré
for i, l in enumerate(lines):
    if 'affectationService' in l and ('private' in l or 'new Affectation' in l):
        print(f'  Declaration at line {i+1}: {repr(l.rstrip())}')

print()

# Diagnostic Admin ligne 491
fa = 'src/main/java/com/smartcity/controller/AdminDashboardController.java'
alines = open(fa, encoding='utf-8').readlines()
print('Admin line 489-499:')
for i in range(488, min(500, len(alines))):
    print(f'  {i+1}: {repr(alines[i].rstrip())}')

# Chercher où colSignalementCommentaire est déclaré
for i, l in enumerate(alines):
    if 'colSignalementCommentaire' in l and '@FXML' in l:
        print(f'  @FXML Declaration at line {i+1}: {repr(l.rstrip())}')
