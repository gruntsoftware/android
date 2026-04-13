# 🌍 Brainwallet Android — Auto-Translation Setup Guide

## What this does

Every time code is pushed to `main`/`master` that touches
`app/src/main/res/values/strings.xml`, a GitHub Actions workflow:

1. Finds every locale `strings.xml` under `app/src/main/res/values-*/`
2. Detects strings that are missing or empty in each locale file
3. Calls the Claude API to translate the missing strings
4. Creates a new branch `translations/auto-YYYYMMDD-HHMMSS`
5. Commits the updated locale files
6. Opens a Pull Request with a table summarising what was translated

---

## Files to add to your repository

```
your-android-repo/
├── .github/
│   └── workflows/
│       └── auto-translate.yml      ← copy from this package
└── scripts/
    └── translate_strings.py        ← copy from this package
```

---

## Step 1 — Create a GitHub Personal Access Token (PAT)

The workflow needs permission to create branches and open PRs.

1. Go to **https://github.com/settings/tokens?type=beta**
   *(Fine-grained tokens — more secure than classic tokens)*

2. Click **"Generate new token"**

3. Fill in:
   | Field | Value |
   |---|---|
   | **Token name** | `brainwallet-translations` |
   | **Expiration** | 1 year (or "No expiration") |
   | **Repository access** | Only select repositories → `gruntsoftware/android` |

4. Under **Repository permissions**, set:
   | Permission | Access |
   |---|---|
   | **Contents** | Read and write |
   | **Pull requests** | Read and write |
   | **Workflows** | Read and write |
   | **Metadata** | Read-only *(auto-selected)* |

5. Click **"Generate token"** and **copy it immediately** — you won't see it again.

---

## Step 2 — Add secrets to the repository

Go to:
**https://github.com/gruntsoftware/android/settings/secrets/actions**

Add two secrets:

| Secret name | Value |
|---|---|
| `TRANSLATION_PAT` | The PAT you just created in Step 1 |
| `ANTHROPIC_API_KEY` | Your Anthropic API key from https://console.anthropic.com/settings/keys |

---

## Step 3 — Add the files to your repository

### Option A — via GitHub web UI

1. In your repo, navigate to `.github/workflows/`
   (create the folder if it doesn't exist)
2. Click **"Add file" → "Create new file"**
3. Name it `auto-translate.yml` and paste in the content
4. Repeat for `scripts/translate_strings.py`

### Option B — via Git (recommended)

```bash
# Clone the repo
git clone https://github.com/gruntsoftware/android.git
cd android

# Create the folders
mkdir -p .github/workflows scripts

# Copy the two files from this package, then:
git add .github/workflows/auto-translate.yml scripts/translate_strings.py
git commit -m "chore: add auto-translation GitHub Action"
git push origin main
```

---

## Step 4 — Create locale resource directories

The script writes to `app/src/main/res/values-{locale}/strings.xml`.
These directories are created automatically if they don't exist, but
you can create them manually too:

```
app/src/main/res/
├── values/          ← source English (already exists)
├── values-ar/       ← Arabic
├── values-zh-rTW/   ← Traditional Chinese
├── values-zh-rCN/   ← Simplified Chinese
├── values-fr/       ← French
├── values-de/       ← German
├── values-fa/       ← Farsi
├── values-pa/       ← Punjabi
├── values-pl/       ← Polish
├── values-in/       ← Indonesian
├── values-es/       ← Spanish
├── values-sv/       ← Swedish
├── values-uk/       ← Ukrainian
├── values-ru/       ← Russian
├── values-tr/       ← Turkish
├── values-ja/       ← Japanese
├── values-ko/       ← Korean
├── values-hi/       ← Hindi
├── values-it/       ← Italian
└── values-pt-rBR/   ← Brazilian Portuguese
```

---

## Step 5 — Test it

Either push a change to `values/strings.xml`, or trigger manually:

1. Go to **https://github.com/gruntsoftware/android/actions**
2. Select **"Auto-Translate strings.xml"**
3. Click **"Run workflow"** → **"Run workflow"**

After ~2–5 minutes a PR will appear at:
**https://github.com/gruntsoftware/android/pulls**

---

## Triggering behaviour

| Trigger | When |
|---|---|
| **Automatic** | Any push to `main`/`master` that changes `**/values/strings.xml` |
| **Manual** | Via Actions tab → "Run workflow" |

Pushes that don't touch `strings.xml` are **ignored** — no unnecessary API calls.

---

## Costs

- The script batches strings in groups of 40 per API call.
- Your `strings.xml` has ~273 translatable strings across 19 languages
  = roughly **130–140 API calls** on a full first run.
- Subsequent runs only translate **new/changed strings**, so costs drop
  significantly after the first pass.

---

## Troubleshooting

| Problem | Fix |
|---|---|
| `ERROR: ANTHROPIC_API_KEY not set` | Check the secret name is exactly `ANTHROPIC_API_KEY` |
| PR not created | Check `TRANSLATION_PAT` has `pull-requests: write` permission |
| "Resource not accessible by integration" | PAT needs `workflows: write` and `contents: write` |
| Empty translations | Check Actions logs for API errors; may be a rate limit |
| Branch already exists | Workflow uses timestamp — only happens if triggered twice in one second |
