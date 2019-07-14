# torrent-search

## Import to Jenkins X
- `jx import`

## DevPod
### VS Code
- `jx create devpod`
### Desktop IDE
- `jx sync`
- `jx create devpod --reuse --sync`
### Shell
- `jx rsh -d`

## How to GitOps/ChatOps
- `jx create issue -t 'Do something cool'`
- `git checkout -b feature/1-Do-something-cool`
- `git commit -m '#1 Do something cool'`
- `git push --set-upstream origin feature/1-Do-something-cool`
- `jx create pr -t 'Do something cool'`
- Watch the magic happens :)

## Approve PR
- Comment `/lgtm` or `/approve` to approve pull request

## Un-approve PR
- Comment `/lgtm cancel` or `/approve cancel` to un-approve

## Promote Environment
- `jx promote --version v0.0.1 --env production --timeout 1h`