import requests
import json

import os
token = os.environ.get('GITHUB_TOKEN', '')
headers = {
    'Authorization': f'token {token}',
    'Accept': 'application/vnd.github+json'
}

runs_url = 'https://api.github.com/repos/LYQ2912023679/GithubMCPTest/actions/runs?per_page=5'
runs_resp = requests.get(runs_url, headers=headers)
runs = runs_resp.json().get('workflow_runs', [])

for run in runs[:5]:
    print(f"Run #{run['run_number']} | ID: {run['id']} | status: {run['status']} | conclusion: {run.get('conclusion', 'N/A')} | event: {run['event']}")
    
    if run['status'] == 'completed' and run.get('conclusion') == 'failure':
        jobs_url = f"https://api.github.com/repos/LYQ2912023679/GithubMCPTest/actions/runs/{run['id']}/jobs"
        jobs_resp = requests.get(jobs_url, headers=headers)
        jobs = jobs_resp.json().get('jobs', [])
        
        for job in jobs:
            print(f"  Job: {job['name']} | conclusion: {job.get('conclusion', 'N/A')}")
            for step in job.get('steps', []):
                step_conclusion = step.get('conclusion')
                if step_conclusion == 'success':
                    status_icon = "OK"
                elif step_conclusion == 'failure':
                    status_icon = "FAIL"
                else:
                    status_icon = step_conclusion or '?'
                print(f"    [{status_icon}] Step: {step['name']}")
                
                if step.get('conclusion') == 'failure':
                    logs_url = f"https://api.github.com/repos/LYQ2912023679/GithubMCPTest/actions/jobs/{job['id']}/logs"
                    logs_resp = requests.get(logs_url, headers=headers)
                    if logs_resp.status_code == 200:
                        lines = logs_resp.text.split('\n')
                        for i, line in enumerate(lines):
                            if 'error' in line.lower() or 'failed' in line.lower() or 'Error' in line:
                                start = max(0, i-2)
                                end = min(len(lines), i+5)
                                print(f"    --- Log around line {i} ---")
                                for l in lines[start:end]:
                                    print(f"      {l}")
                                print("      ...")
                    else:
                        print(f"    (Failed to get logs: {logs_resp.status_code})")
    print()
