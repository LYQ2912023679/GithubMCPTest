import requests

import os
token = os.environ.get('GITHUB_TOKEN', '')
headers = {
    'Authorization': f'token {token}',
    'Accept': 'application/vnd.github+json'
}

runs_url = 'https://api.github.com/repos/LYQ2912023679/GithubMCPTest/actions/runs?per_page=1'
runs_resp = requests.get(runs_url, headers=headers)
run = runs_resp.json()['workflow_runs'][0]

jobs_url = f"https://api.github.com/repos/LYQ2912023679/GithubMCPTest/actions/runs/{run['id']}/jobs"
jobs_resp = requests.get(jobs_url, headers=headers)
job = jobs_resp.json()['jobs'][0]

logs_url = f"https://api.github.com/repos/LYQ2912023679/GithubMCPTest/actions/jobs/{job['id']}/logs"
logs_resp = requests.get(logs_url, headers=headers)

lines = logs_resp.text.split('\n')
for i, line in enumerate(lines):
    if 'pytest' in line.lower() or 'test' in line.lower() and ('pass' in line.lower() or 'fail' in line.lower() or 'error' in line.lower()):
        if '##[group]' in line or '##[error]' in line or 'PASSED' in line or 'FAILED' in line or 'ERROR' in line or 'collected' in line or 'no tests' in line or 'exit code' in line:
            start = max(0, i-2)
            end = min(len(lines), i+5)
            for l in lines[start:end]:
                print(l)
            print("---")

print("\n=== Full pytest section ===")
in_pytest = False
for i, line in enumerate(lines):
    if 'Test with pytest' in line:
        in_pytest = True
    if in_pytest:
        print(line)
    if in_pytest and 'Post job cleanup' in line:
        break
