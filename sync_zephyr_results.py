import requests
import json
import sys
from datetime import datetime, timezone

ZEPHYR_API_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJjb250ZXh0Ijp7ImJhc2VVcmwiOiJodHRwczovL2xpbmd4aS16ZW5nLmF0bGFzc2lhbi5uZXQiLCJ1c2VyIjp7ImFjY291bnRJZCI6IjcxMjAyMDpjNDY5YjU1MS0xOGMzLTQ2MDUtOTMwZS0zNWYwYjU3MzAyZDQiLCJ0b2tlbklkIjoiZjIyNjc5MjgtMzY2Mi00NTQ4LTllOWMtMTcwNTkyZWZkM2YzIn0sImVudmlyb25tZW50SWQiOiIyYmFhZWI2OS0xNWFjLTQ5NTUtOGViNi1lMzQ2YWExNTY3YWEifSwiaXNzIjoiY29tLmthbm9haC50ZXN0LW1hbmFnZXIiLCJzdWIiOiJGT1JHRS1jNTc0NTcyOS0xMzUyLTQ4ZTMtYTM3Yi01MjEwMmQxNzQzZmUiLCJleHAiOjE4MTU0NzA3MzMsImlhdCI6MTc4MzkzNDczM30.zthXdO-glJEnvzYlHWTGQrcJT0bpRtlpJ8gYmNPUNVw"

API_BASE = "https://api.zephyrscale.smartbear.com/v2"
PROJECT_KEY = "LYQ"
TEST_CYCLE_KEY = "LYQ-R2"

HEADERS = {
    "Authorization": f"Bearer {ZEPHYR_API_TOKEN}",
    "Content-Type": "application/json"
}

with open("zephyr_test_mapping.json", "r", encoding="utf-8") as f:
    mapping = json.load(f)

test_results = json.load(open("snake-web/test-results/.last-run.json", "r", encoding="utf-8"))
all_passed = test_results.get("status") == "passed"
failed_tests = test_results.get("failedTests", [])

results_log = []

for m in mapping["mappings"]:
    tc_key = m["zephyrKey"]
    tc_name = m["testName"]
    tc_objective = m["objective"]

    if all_passed:
        status_name = "Pass"
        comment = f"[自动化同步] Playwright + Postman 测试通过 - {tc_name}"
    else:
        status_name = "Fail"
        comment = f"[自动化同步] 测试失败 - {tc_name}"

    payload = {
        "projectKey": PROJECT_KEY,
        "testCaseKey": tc_key,
        "testCycleKey": TEST_CYCLE_KEY,
        "statusName": status_name,
        "comment": comment,
        "actualEndDate": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")
    }

    try:
        resp = requests.post(f"{API_BASE}/testexecutions", headers=HEADERS, json=payload)
        if resp.status_code in (200, 201):
            data = resp.json()
            exec_key = data.get("key", "N/A")
            exec_id = data.get("id", "N/A")
            print(f"[PASS] {tc_key} - {tc_name} -> 执行: {exec_key} (ID: {exec_id})")
            results_log.append({"testCase": tc_key, "name": tc_name, "status": status_name, "executionKey": exec_key, "success": True})
        else:
            print(f"[FAIL] {tc_key} - {tc_name} -> HTTP {resp.status_code}: {resp.text[:200]}")
            results_log.append({"testCase": tc_key, "name": tc_name, "status": "ERROR", "error": resp.text[:200], "success": False})
    except Exception as e:
        print(f"[ERROR] {tc_key} - {tc_name} -> {str(e)}")
        results_log.append({"testCase": tc_key, "name": tc_name, "status": "ERROR", "error": str(e), "success": False})

success_count = sum(1 for r in results_log if r["success"])
fail_count = len(results_log) - success_count

report = {
    "syncTime": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
    "project": PROJECT_KEY,
    "testCycle": TEST_CYCLE_KEY,
    "totalTestCases": len(results_log),
    "successCount": success_count,
    "failCount": fail_count,
    "allAutomationPassed": all_passed,
    "details": results_log
}

with open("zephyr_sync_report.json", "w", encoding="utf-8") as f:
    json.dump(report, f, ensure_ascii=False, indent=2)

print("\n同步完成: {}/{} 成功, {} 失败".format(success_count, len(results_log), fail_count))
print("报告已保存到 zephyr_sync_report.json")
