# Cheddar — Claude Code Guide

## Git Workflow

This repo uses a **feature branch workflow** — never commit or push directly to `master`.

**Remotes (post-migration):**
- `origin` → `git@github.com:travel-cloud/Cheddar.git` (org repo)
- `fork` → `git@github.com:clicktravel-chrishern/Cheddar.git` (personal fork, kept for reference)

**Branch naming:**
- With a Jira ticket: `CREW-1234-short-description`
- Without a ticket: `no-jira-short-description`

**Starting new work:**
1. Ensure you're on master and it's up to date: `git checkout master && git pull origin master`
2. Create a feature branch: `git checkout -b CREW-1234-my-feature`
3. Commit and push: `git push origin CREW-1234-my-feature`
4. Open PR targeting `master`: `gh pr create --base master`
5. After merge: delete the branch locally and update master

**Claude Code rules:**
- Always check the current branch before making changes — if on `master`, create a feature branch first
- Push to `origin` (the org repo, not the fork)
- PR command: `gh pr create --base master`
