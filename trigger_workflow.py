import requests
import urllib3
import time

urllib3.disable_warnings()

import os
token = os.environ.get('GITHUB_TOKEN', '')
headers = {
    'Authorization': f'token {token}',
    'Accept': 'application/vnd.github+json'
}

url = 'https://api.github.com/repos/LYQ2912023679/GithubMCPTest/actions/workflows/python-app.yml/dispatches'
resp = requests.post(url, headers=headers, json={'ref': 'main'}, verify=False)
print(f'Trigger status: {resp.status_code}')
if resp.status_code == 204:
    print('Workflow dispatch triggered successfully!')
else:
    print(f'Response: {resp.text[:500]}')

time.sleep(3)

runs_url = 'https://api.github.com/repos/LYQ2912023679/GithubMCPTest/actions/runs?per_page=3'
runs_resp = requests.get(runs_url, headers=headers, verify=False)
if runs_resp.status_code == 200:
    runs = runs_resp.json().get('workflow_runs', [])
    for run in runs[:3]:
        print(f"  Run #{run['run_number']} | {run['status']} | {run['conclusion'] or 'N/A'} | {run['html_url']}")
else:
    print(f'Failed to get runs: {runs_resp.status_code}')
