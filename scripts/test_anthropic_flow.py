name: Test Anthropic API Key

on:
  workflow_dispatch:

jobs:
  test-key:
    runs-on: ubuntu-latest

    steps:
      - name: Test Anthropic API Key
        env:
          ANTHROPIC_API_KEY: ${{ secrets.ANTHROPIC_API_KEY }}
        run: |
          pip install anthropic -q
          python - <<'PYTHON'
import anthropic
import os

api_key = os.environ.get("ANTHROPIC_API_KEY")
if not api_key:
    raise Exception("❌ ANTHROPIC_API_KEY is not set")

client = anthropic.Anthropic(api_key=api_key)
message = client.messages.create(
    model="claude-opus-4-5",
    max_tokens=32,
    messages=[{"role": "user", "content": "Reply with OK only."}],
)
print(f"✅ API key works! Response: {message.content[0].text}")
PYTHON