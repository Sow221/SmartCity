import re, os

BASE = os.path.dirname(os.path.abspath(__file__))

fxml_files = {
    'admin':   'src/main/resources/fxml/admin_dashboard.fxml',
    'agent':   'src/main/resources/fxml/agent_dashboard.fxml',
    'citizen': 'src/main/resources/fxml/citizen_dashboard.fxml',
    'login':   'src/main/resources/fxml/login.fxml',
    'register':'src/main/resources/fxml/register.fxml',
}
controllers = {
    'admin':   'src/main/java/com/smartcity/controller/AdminDashboardController.java',
    'agent':   'src/main/java/com/smartcity/controller/AgentDashboardController.java',
    'citizen': 'src/main/java/com/smartcity/controller/CitizenDashboardController.java',
    'login':   'src/main/java/com/smartcity/controller/LoginController.java',
    'register':'src/main/java/com/smartcity/controller/RegisterController.java',
}
css_file = 'src/main/resources/css/design-system.css'

def read(path):
    return open(os.path.join(BASE, path), encoding='utf-8', errors='ignore').read().replace('\r\n', '\n').replace('\r', '\n')

print("=" * 60)
print("RAPPORT DE COHERENCE SMARTCITY")
print("=" * 60)

# ── 1. CSS classes used in FXML but not defined ──────────────
css = read(css_file)
css_defined = set(re.findall(r'\.([\w-]+)[\s{,:]', css))
# Remove numeric false positives (section numbers in comments)
css_defined = {c for c in css_defined if not re.match(r'^\d+$', c)}

all_used_classes = set()
for name, path in fxml_files.items():
    content = read(path)
    for m in re.finditer(r'styleClass="([^"]+)"', content):
        raw = m.group(1)
        for part in raw.split(','):
            cls = part.strip()
            # A valid CSS class has no spaces — skip multi-word false positives
            if cls and ' ' not in cls:
                all_used_classes.add(cls)

missing_css = sorted(all_used_classes - css_defined)
print(f"\n[1] CLASSES CSS MANQUANTES ({len(missing_css)} sur {len(all_used_classes)} utilisees)")
if missing_css:
    for c in missing_css:
        print(f"    MANQUANT: .{c}")
else:
    print("    OK")

# ── 2. fx:id in FXML vs @FXML fields in controller ───────────
# TableColumn fx:id are wired via setCellValueFactory in code — not @FXML injected
# Pure display labels added in redesign (avatar, nomDisplay) are optional @FXML
COLUMN_PREFIXES = ('col',)
DISPLAY_ONLY = {
    'adminAvatarLabel','adminProfilAvatarLabel','adminProfilNomDisplay',
    'agentAvatarLabel','agentProfilAvatarLabel','agentProfilNomDisplay',
    'citizenAvatarLabel','citizenProfilAvatarLabel','citizenProfilNomDisplay',
}

print("\n[2] INCOHERENCES fx:id FXML <-> @FXML CONTROLLER")
pairs = [('admin','admin'),('agent','agent'),('citizen','citizen'),('login','login'),('register','register')]
for fxml_key, ctrl_key in pairs:
    fxml_content = read(fxml_files[fxml_key])
    ctrl_content = read(controllers[ctrl_key])

    fxml_ids = set(re.findall(r'fx:id="([^"]+)"', fxml_content))
    # Match @FXML fields including generic types like BarChart<String, Number>
    ctrl_fields = set(re.findall(r'@FXML\s+(?:private|public)\s+[\w<>, .]+\s+(\w+)\s*;', ctrl_content))

    # Filter out columns (managed via code) and pure display-only labels
    fxml_ids_real = {x for x in fxml_ids
                     if not any(x.startswith(p) for p in COLUMN_PREFIXES)
                     and x not in DISPLAY_ONLY}

    in_fxml_not_ctrl = fxml_ids_real - ctrl_fields
    in_ctrl_not_fxml = ctrl_fields - fxml_ids  # field declared but no matching fx:id = real problem

    print(f"\n  [{fxml_key}]")
    if in_fxml_not_ctrl:
        for x in sorted(in_fxml_not_ctrl):
            print(f"    REEL - fx:id dans FXML sans @FXML controller: {x}")
    if in_ctrl_not_fxml:
        for x in sorted(in_ctrl_not_fxml):
            print(f"    REEL - @FXML controller sans fx:id dans FXML: {x}")
    if not in_fxml_not_ctrl and not in_ctrl_not_fxml:
        print("    OK")

# ── 3. onAction handlers in FXML vs @FXML methods ────────────
print("\n[3] INCOHERENCES onAction FXML <-> @FXML METHODES")
for fxml_key, ctrl_key in pairs:
    fxml_content = read(fxml_files[fxml_key])
    ctrl_content = read(controllers[ctrl_key])
    fxml_handlers = set(re.findall(r'onAction="#([^"]+)"', fxml_content))
    ctrl_methods  = set(re.findall(r'@FXML\s+[\s\S]{0,80}?(?:private|public)\s+\w+\s+(\w+)\s*\(', ctrl_content))
    missing = fxml_handlers - ctrl_methods
    print(f"\n  [{fxml_key}]")
    if missing:
        for m in sorted(missing):
            print(f"    MANQUANT: #{m} reference dans FXML mais absent du controller")
    else:
        print("    OK")

# ── 4. Duplicate fx:id ────────────────────────────────────────
print("\n[4] fx:id DUPLIQUES")
for name, path in fxml_files.items():
    content = read(path)
    ids = re.findall(r'fx:id="([^"]+)"', content)
    seen = {}
    for id_ in ids:
        seen[id_] = seen.get(id_, 0) + 1
    dups = [k for k, v in seen.items() if v > 1]
    print(f"  [{name}]: " + ("DUPLIQUES: " + ", ".join(dups) if dups else "OK"))

# ── 5. Inline styles in FXML ─────────────────────────────────
# Acceptable: -fx-font-size on emoji icon labels
# Not acceptable: colors, backgrounds, padding on real UI components
print("\n[5] STYLES INLINE FXML (hors emoji font-size)")
for name, path in fxml_files.items():
    content = read(path)
    all_inlines = re.findall(r'style="([^"]+)"', content)
    real = [s for s in all_inlines
            if not re.match(r'^-fx-font-size:\s*\d+px;?$', s.strip())]
    if real:
        print(f"  [{name}]: {len(real)} style(s) inline non-triviaux")
        for s in real[:5]:
            print(f"    -> {s[:100]}")
        if len(real) > 5:
            print(f"    ... et {len(real)-5} autres")
    else:
        print(f"  [{name}]: OK")

# ── 6. CSS defined but never used ────────────────────────────
# Exclude utility/animation/pseudo classes that are used programmatically
EXCLUDED = {
    'app-root','dark-mode','hover','focused','selected','odd','pressed','disabled',
    'filled','empty','even','first','last','cell','row','column','header','track',
    'thumb','bar','mark','dot','radio','box','arrow','text','content','viewport',
    'scroll','increment','decrement','button','label','separator','title','graphic',
    'tab','pane','area','field','indicator','percentage','legend','symbol','plot',
    'chart','axis','tick','line','node','series','data','layer','overlay','popup',
    'tooltip','dialog','header-panel','button-bar','ripple','ripple-active',
    'ripple-container','skeleton','loading-pulse','fade-in','fade-out',
    'slide-in-left','slide-out-left','slide-in-right','slide-out-right',
    'slide-in-up','slide-out-up','slide-in-down','slide-out-down',
    'scale-in','scale-out','icon-spin','icon-bounce','hover-reveal',
    'hover-reveal-visible','row-action-btn','counter-animated','highlight-flash',
    'card-number-pulse','focused-ring','focused-ring-agent','status-dot',
    'status-dot-offline','status-dot-busy','status-dot-away','status-dot-pulse',
    'badge','badge-success','badge-warning','badge-info','badge-neutral',
    'input-error','input-success','input-warning','empty-state','empty-state-icon',
    'empty-state-description','toast','toast-success','toast-error','toast-warning',
    'toast-info','toast-enter','toast-exit','message-success','message-error',
    'message-warning','floating-message','auth-root','auth-card','auth-input',
    'auth-button','auth-link','auth-footer','auth-subtitle','btn-primary',
    'btn-secondary','btn-outline','btn-danger','btn-success','btn-ghost',
    'card-agent','card-premium','card-number','card-label','card-total',
    'card-attente','card-encours','card-termine','text-primary','text-secondary',
    'text-muted','text-success','text-warning','text-error','text-bold',
    'text-semibold','text-sm','text-base','text-lg','text-xl','text-2xl',
    'bg-white','bg-gray-100','bg-primary-100','bg-secondary-100',
    'rounded-sm','rounded-md','rounded-lg','rounded-xl',
    'shadow-sm','shadow-md','shadow-lg','mt-1','mt-2','mt-3','mt-4',
    'mb-1','mb-2','mb-3','mb-4','button-agent-success','button-agent',
    'reports-header','reports-title','reports-section-title','reports-footer',
    'reports-hint','quick-actions-bar','quick-actions-label','action-rapid-btn',
    'check-box','radio-button','toggle-button','tab-pane','scroll-pane',
    'dialog-pane','progress-bar','progress-indicator','combo-box',
    'text-field','text-area','password-field','form-label','data-table',
    'data-table-agent','chart','chart-legend','chart-pie-label',
    'sidebar-button-active','sidebar-agent-button-active',
}
unused_css = sorted(css_defined - all_used_classes - EXCLUDED)
print(f"\n[6] CLASSES CSS INUTILISEES ({len(unused_css)}) [utilitaires/animations exclus]")
if unused_css:
    for c in unused_css[:15]:
        print(f"    INUTILISEE: .{c}")
    if len(unused_css) > 15:
        print(f"    ... et {len(unused_css)-15} autres")
else:
    print("    OK")

# ── 7. FXML structure ─────────────────────────────────────────
print("\n[7] STRUCTURE FXML")
for name, path in fxml_files.items():
    content = read(path)
    issues = []
    if 'fx:controller=' not in content:
        issues.append("fx:controller manquant")
    if 'stylesheets=' not in content:
        issues.append("stylesheets manquant")
    if 'UTF-8' not in content:
        issues.append("encoding UTF-8 manquant")
    print(f"  [{name}]: " + ("PROBLEMES: " + " | ".join(issues) if issues else "OK"))

# ── 8. Controllers ────────────────────────────────────────────
print("\n[8] CONTROLLERS")
for name, path in controllers.items():
    content = read(path)
    issues = []
    if 'setMainApp' not in content and name not in ('login', 'register'):
        issues.append("setMainApp manquant")
    if 'SessionManager' not in content and name not in ('login', 'register'):
        issues.append("SessionManager non utilise")
    # Only flag setStyle calls that set colors/backgrounds on non-button elements
    # Button state changes in cell factories are acceptable
    inline_styles = re.findall(r'\.setStyle\("([^"]+)"\)', content)
    # Filter: keep only those NOT in a cell factory context (simple heuristic: no 'background-radius')
    complex_styles = [s for s in inline_styles
                      if 'background-color' in s and 'background-radius' not in s]
    if complex_styles:
        issues.append(f"{len(complex_styles)} setStyle() sans border-radius (potentiellement a externaliser)")
    print(f"  [{name}]: " + ("ATTENTION: " + " | ".join(issues) if issues else "OK"))

print("\n" + "=" * 60)
print("FIN DU RAPPORT")
print("=" * 60)
