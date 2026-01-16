# 🔐 Fix GitHub Authentication Error

## Problem
```
remote: Invalid username or token. Password authentication is not supported for Git operations.
```

## Solution: Use Personal Access Token (PAT)

GitHub no longer accepts passwords. You need to create a **Personal Access Token**.

---

## Step-by-Step Fix

### Step 1: Create Personal Access Token on GitHub

1. **Go to GitHub Settings:**
   - Click your profile picture (top right)
   - Click **Settings**
   - Or go directly: https://github.com/settings/tokens

2. **Create New Token:**
   - Scroll down to **Developer settings** (left sidebar)
   - Click **Personal access tokens**
   - Click **Tokens (classic)**
   - Click **Generate new token** → **Generate new token (classic)**

3. **Configure Token:**
   - **Note:** `gocomet-push-token` (or any name)
   - **Expiration:** Choose 90 days or No expiration
   - **Scopes:** Check these boxes:
     - ✅ `repo` (Full control of private repositories)
     - ✅ `workflow` (if you plan to use GitHub Actions)

4. **Generate:**
   - Click **Generate token** at the bottom
   - **⚠️ IMPORTANT:** Copy the token immediately! You won't see it again!
   - It looks like: `ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`

### Step 2: Use Token to Push

**Option A: Use Token as Password (Easiest)**

```bash
cd /Users/tanmay.mallick/Documents/gocomet

# When prompted for password, paste your token
git push -u origin main

# Username: Tanmay2812
# Password: [paste your token here]
```

**Option B: Store Token in Git Credential Helper (Recommended)**

```bash
# Store credentials (will prompt for username and token)
git push -u origin main

# Or configure credential helper to store it
git config --global credential.helper osxkeychain

# Then push (will ask once, then remember)
git push -u origin main
```

**Option C: Use Token in URL (Quick but less secure)**

```bash
# Replace YOUR_TOKEN with your actual token
git remote set-url origin https://YOUR_TOKEN@github.com/Tanmay2812/Gomet-Ride-Hailing-Tanmay-Mallick.git

# Then push
git push -u origin main
```

---

## Alternative: Use SSH (More Secure)

### Step 1: Check if you have SSH key

```bash
ls -la ~/.ssh
```

### Step 2: Generate SSH key (if you don't have one)

```bash
# Generate new SSH key
ssh-keygen -t ed25519 -C "your_email@example.com"

# Press Enter to accept default location
# Enter a passphrase (optional but recommended)
```

### Step 3: Add SSH key to GitHub

```bash
# Copy your public key
cat ~/.ssh/id_ed25519.pub
# Or if you have id_rsa:
# cat ~/.ssh/id_rsa.pub
```

1. **Go to GitHub Settings:**
   - https://github.com/settings/keys
   - Click **New SSH key**

2. **Add Key:**
   - **Title:** `MacBook` (or any name)
   - **Key:** Paste the output from `cat ~/.ssh/id_ed25519.pub`
   - Click **Add SSH key**

### Step 4: Change Remote to SSH

```bash
# Change remote URL to SSH
git remote set-url origin git@github.com:Tanmay2812/Gomet-Ride-Hailing-Tanmay-Mallick.git

# Test SSH connection
ssh -T git@github.com

# Should see: "Hi Tanmay2812! You've successfully authenticated..."

# Now push
git push -u origin main
```

---

## Quick Fix (Copy-Paste Ready)

### Method 1: Personal Access Token (Fastest)

1. **Create token:** https://github.com/settings/tokens/new
   - Check `repo` scope
   - Copy the token

2. **Push:**
```bash
cd /Users/tanmay.mallick/Documents/gocomet
git push -u origin main
# Username: Tanmay2812
# Password: [paste token here]
```

### Method 2: SSH (Best for long-term)

```bash
# Generate SSH key (if needed)
ssh-keygen -t ed25519 -C "your_email@example.com"

# Copy public key
cat ~/.ssh/id_ed25519.pub

# Add to GitHub: https://github.com/settings/keys

# Change to SSH
cd /Users/tanmay.mallick/Documents/gocomet
git remote set-url origin git@github.com:Tanmay2812/Gomet-Ride-Hailing-Tanmay-Mallick.git

# Push
git push -u origin main
```

---

## Verify It Worked

After pushing, check your GitHub repository:
- Go to: https://github.com/Tanmay2812/Gomet-Ride-Hailing-Tanmay-Mallick
- You should see all your files!

---

## Troubleshooting

### "Permission denied" error
- Make sure you checked the `repo` scope when creating the token
- Token might have expired (create a new one)

### "Repository not found" error
- Verify the repository exists: https://github.com/Tanmay2812/Gomet-Ride-Hailing-Tanmay-Mallick
- Make sure you have access to it

### SSH "Host key verification failed"
```bash
ssh-keyscan github.com >> ~/.ssh/known_hosts
```

---

**Choose the method you prefer! Personal Access Token is fastest, SSH is more secure for long-term use.**
