# springboot-template

## Import to Jenkins X
- `jx import`

## How to GitOps/ChatOps
- `jx create issue -t 'Do something cool'`
- `git checkout -b feature/1-Do-something-cool`
- `git commit -m '#1 Do something cool'`
- `git push --set-upstream origin feature/1-Do-something-cool`
- `jx create pr -t 'Do something cool'`
- Comment `/lgtm` to pull request
- Watch the magic happens :)

## Promote Environment
- `jx promote --version v0.0.1 --env production --timeout 1h`