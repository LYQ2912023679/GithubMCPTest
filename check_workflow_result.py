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

print("等待 workflow 触发...")
time.sleep(10)

for attempt in range(12):
    runs_url = 'https://api.github.com/repos/LYQ2912023679/GithubMCPTest/actions/runs?per_page=3'
    runs_resp = requests.get(runs_url, headers=headers, verify=False)
    runs = runs_resp.json().get('workflow_runs', [])
    
    latest = runs[0] if runs else None
    if latest:
        status = latest['status']
        conclusion = latest.get('conclusion', 'N/A')
        print(f"[{attempt+1}/12] Run #{latest['run_number']} | status: {status} | conclusion: {conclusion}")
        
        if status == 'completed':
            if conclusion == 'success':
                print(f"\n✅ Workflow 成功!")
            else:
                print(f"\n❌ Workflow 失败 (conclusion: {conclusion})")
            
            jobs_url = f"https://api.github.com/repos/LYQ2912023679/GithubMCPTest/actions/runs/{latest['id']}/jobs"
            jobs_resp = requests.get(jobs_url, headers=headers, verify=False)
            jobs = jobs_resp.json().get('jobs', [])
            for job in jobs:
                for step in job.get('steps', []):
                    icon = "✅" if step.get('conclusion') == 'success' else "❌" if step.get('conclusion') == 'failure' else "⏭️"
                    print(f"  {icon} {step['name']}")
                
                if conclusion == 'failure':
                    for step in job.get('steps', []):
                        if step.get('conclusion') == 'failure':
                            logs_url = f"https://api.github.com/repos/LYQ2912023679/GithubMCPTest/actions/jobs/{job['id']}/logs"
                            logs_resp = requests.get(logs_url, headers=headers, verify=False)
                            if logs_resp.status_code == 200:
                                lines = logs_resp.text.split('\n')
                                for i, line in enumerate(lines):
                                    if 'error' in line.lower() or 'Error' in line or 'FAIL' in line or 'failed' in line.lower():
                                        start = max(0, i-1)
                                        end = min(len(lines), i+3)
                                        print(f"  --- 日志 (行 {i}) ---")
                                        for l in lines[start:end]:
                                            print(f"    {l}")
                                        break
            break
    else:
        print(f"[{attempt+1}/12] 暂无运行记录")
    
    time.sleep(10)

print(f"\n运行链接: {latest['html_url']}" if latest else "未找到运行记录")
