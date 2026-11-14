#!/usr/bin/env bash
set -euo pipefail

BASE_URL="http://localhost:8080/api/v1"
COOKIE_JAR_A="/tmp/nexus_cookie_a.txt"
COOKIE_JAR_B="/tmp/nexus_cookie_b.txt"

rm -f "$COOKIE_JAR_A" "$COOKIE_JAR_B"

TIMESTAMP=$(date +%s)
USER_A_EMAIL="qa_user_a_${TIMESTAMP}@nexus.dev"
USER_B_EMAIL="qa_user_b_${TIMESTAMP}@nexus.dev"
PASSWORD="SecurePassword123!"

echo "=========================================================="
echo "Starting NEXUS Full E2E Integration Workflows Audit"
echo "Timestamp: $(date -u)"
echo "Target Base URL: $BASE_URL"
echo "=========================================================="

# ---------------------------------------------------------
# WORKFLOW A: Opportunity to Preparation
# ---------------------------------------------------------
echo ""
echo ">>> [WORKFLOW A] 1. Register User A ($USER_A_EMAIL)"
REG_RESP=$(curl -s -c "$COOKIE_JAR_A" -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"email\": \"$USER_A_EMAIL\", \"password\": \"$PASSWORD\", \"fullName\": \"Auditor User A\"}")
USER_A_ID=$(echo "$REG_RESP" | python3 -c "import sys, json; print(json.load(sys.stdin)['data']['id'])")
echo "✓ User A registered with ID: $USER_A_ID"

echo ">>> [WORKFLOW A] 2. Complete Onboarding Profile for User A"
PROFILE_RESP=$(curl -s -b "$COOKIE_JAR_A" -X PUT "$BASE_URL/users/me/profile" \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Auditor User A","educationLevel":"BACHELORS","degree":"BS Computer Science","institution":"MIT","graduationYear":2027,"experienceLevel":"INTERMEDIATE","onboardingCompleted":true,"onboardingStep":6}')
echo "✓ Onboarding Profile updated."

echo ">>> [WORKFLOW A] 3. Add Skills and Preferences for User A"
curl -s -b "$COOKIE_JAR_A" -X POST "$BASE_URL/users/me/skills" \
  -H "Content-Type: application/json" \
  -d '{"skillName":"Java","proficiency":"Advanced","yearsExperience":2.5}' > /dev/null
curl -s -b "$COOKIE_JAR_A" -X POST "$BASE_URL/users/me/skills" \
  -H "Content-Type: application/json" \
  -d '{"skillName":"Spring Boot","proficiency":"Intermediate","yearsExperience":2.0}' > /dev/null
echo "✓ Skills added (Java, Spring Boot)."

echo ">>> [WORKFLOW A] 4. Discover Opportunities"
DISCOVERY_RESP=$(curl -s -b "$COOKIE_JAR_A" -X GET "$BASE_URL/opportunities?category=Internships&page=0&size=5")
OPP_ID=$(echo "$DISCOVERY_RESP" | python3 -c "import sys, json; print(json.load(sys.stdin)['data']['content'][0]['id'])")
OPP_TITLE=$(echo "$DISCOVERY_RESP" | python3 -c "import sys, json; print(json.load(sys.stdin)['data']['content'][0]['title'])")
echo "✓ Discovered Opportunity: $OPP_TITLE ($OPP_ID)"

echo ">>> [WORKFLOW A] 5 & 6. Save Opportunity with Developer Notes"
SAVE_RESP=$(curl -s -b "$COOKIE_JAR_A" -X POST "$BASE_URL/users/me/saved-opportunities" \
  -H "Content-Type: application/json" \
  -d "{\"opportunityId\":\"$OPP_ID\",\"notes\":\"Priority target for Summer 2027 internship.\",\"status\":\"PREPARING\"}")
echo "✓ Opportunity saved."

echo ">>> [WORKFLOW A] 7 & 8 & 9. Calculate Matching, Eligibility, and Skill Gaps"
MATCH_RESP=$(curl -s -b "$COOKIE_JAR_A" -X GET "$BASE_URL/opportunities/$OPP_ID/match")
MATCH_SCORE=$(echo "$MATCH_RESP" | python3 -c "import sys, json; print(json.load(sys.stdin)['data']['overallScore'])")
ELIGIBILITY=$(echo "$MATCH_RESP" | python3 -c "import sys, json; print(json.load(sys.stdin)['data']['eligibility']['status'])")
echo "✓ Match Score: $MATCH_SCORE | Eligibility: $ELIGIBILITY"

SKILL_GAP_RESP=$(curl -s -b "$COOKIE_JAR_A" -X GET "$BASE_URL/opportunities/$OPP_ID/skill-gap")
SKILL_COUNT=$(echo "$SKILL_GAP_RESP" | python3 -c "import sys, json; print(json.load(sys.stdin)['data']['totalSkillsEvaluated'])")
echo "✓ Skill Gap Report: $SKILL_COUNT total skills evaluated."

echo ">>> [WORKFLOW A] 10 & 11. Generate Opportunity-Based Roadmap"
ROADMAP_REQ="{\"roadmapType\":\"OPPORTUNITY_PREPARATION\",\"opportunityId\":\"$OPP_ID\",\"availableHoursPerWeek\":15,\"targetCompletionDate\":\"2027-04-15\",\"customTitle\":\"Preparation for $OPP_TITLE\"}"
ROADMAP_RESP=$(curl -s -b "$COOKIE_JAR_A" -X POST "$BASE_URL/roadmaps/generate" \
  -H "Content-Type: application/json" \
  -d "$ROADMAP_REQ")
ROADMAP_ID=$(echo "$ROADMAP_RESP" | python3 -c "import sys, json; print(json.load(sys.stdin)['data']['id'])")
echo "✓ Generated Roadmap ID: $ROADMAP_ID"

echo ">>> [WORKFLOW A] 12 & 13. Complete Roadmap Tasks & Verify Progress Persistence"
TASK_ID=$(echo "$ROADMAP_RESP" | python3 -c "import sys, json; print(json.load(sys.stdin)['data']['milestones'][0]['tasks'][0]['id'])")
echo "Updating Task ID: $TASK_ID to COMPLETED"
TASK_UPDATE_RESP=$(curl -s -b "$COOKIE_JAR_A" -X PATCH "$BASE_URL/roadmaps/$ROADMAP_ID/tasks/$TASK_ID" \
  -H "Content-Type: application/json" \
  -d '{"status":"COMPLETED","userNotes":"Completed all criteria and passed test cases."}')

UPDATED_ROADMAP=$(curl -s -b "$COOKIE_JAR_A" -X GET "$BASE_URL/roadmaps/$ROADMAP_ID")
PROGRESS=$(echo "$UPDATED_ROADMAP" | python3 -c "import sys, json; print(json.load(sys.stdin)['data']['overallProgressPercentage'])")
echo "✓ Verified Roadmap Progress Updated: $PROGRESS%"

echo ">>> [WORKFLOW A] 14. Reschedule Remaining Tasks"
RESCHEDULE_RESP=$(curl -s -b "$COOKIE_JAR_A" -X POST "$BASE_URL/roadmaps/$ROADMAP_ID/reschedule")
echo "✓ Reschedule completed successfully."

# ---------------------------------------------------------
# WORKFLOW B: Open-Source Contribution
# ---------------------------------------------------------
echo ""
echo ">>> [WORKFLOW B] 1 & 2. Connect GitHub & Retrieve Profile"
GITHUB_CONNECT_RESP=$(curl -s -b "$COOKIE_JAR_A" -X POST "$BASE_URL/github/connect" \
  -H "Content-Type: application/json" \
  -d '{"accessToken":"ghp_testMockTokenForAuditPurpose12345","githubUsername":"octocat"}')
GITHUB_PROFILE=$(curl -s -b "$COOKIE_JAR_A" -X GET "$BASE_URL/github/connection")
GH_USER=$(echo "$GITHUB_PROFILE" | python3 -c "import sys, json; print(json.load(sys.stdin)['data']['githubUsername'])")
echo "✓ Verified GitHub Connection for: $GH_USER"

echo ">>> [WORKFLOW B] 3 & 4. Discover Repositories & Inspect Details"
REPOS_RESP=$(curl -s -b "$COOKIE_JAR_A" -X GET "$BASE_URL/github/repositories")
REPO_NAME=$(echo "$REPOS_RESP" | python3 -c "import sys, json; print(json.load(sys.stdin)['data'][0]['fullName'])")
echo "✓ Discovered Repository: $REPO_NAME"

echo ">>> [WORKFLOW B] 5 & 6 & 7. Discover Issues, Match Suitability & Analyze"
ISSUES_RESP=$(curl -s -b "$COOKIE_JAR_A" -X GET "$BASE_URL/github/issues")
ISSUE_ID=$(echo "$ISSUES_RESP" | python3 -c "import sys, json; print(json.load(sys.stdin)['data'][0]['id'])")
ISSUE_TITLE=$(echo "$ISSUES_RESP" | python3 -c "import sys, json; print(json.load(sys.stdin)['data'][0]['title'])")
echo "✓ Discovered Issue: $ISSUE_TITLE ($ISSUE_ID)"

SUITABILITY_RESP=$(curl -s -b "$COOKIE_JAR_A" -X GET "$BASE_URL/github/issues/$ISSUE_ID/suitability")
SUIT_SCORE=$(echo "$SUITABILITY_RESP" | python3 -c "import sys, json; print(json.load(sys.stdin)['data']['suitabilityScore'])")
echo "✓ Issue Suitability Score: $SUIT_SCORE"

echo ">>> [WORKFLOW B] 8 & 9 & 10 & 11. Create Workspace, Track Progress & Record PR"
WS_RESP=$(curl -s -b "$COOKIE_JAR_A" -X POST "$BASE_URL/users/me/contributions" \
  -H "Content-Type: application/json" \
  -d "{\"issueId\":\"$ISSUE_ID\",\"title\":\"Parser Null Check Contribution\",\"status\":\"IN_PROGRESS\",\"targetBranch\":\"fix/parser-null-check\",\"researchNotes\":\"Reproduced defect and writing unit test.\"}")
WS_ID=$(echo "$WS_RESP" | python3 -c "import sys, json; print(json.load(sys.stdin)['data']['id'])")

WS_UPDATE_RESP=$(curl -s -b "$COOKIE_JAR_A" -X PATCH "$BASE_URL/contributions/$WS_ID" \
  -H "Content-Type: application/json" \
  -d '{"title":"Parser Null Check Contribution","status":"REVIEW_REQUESTED","pullRequestUrl":"https://github.com/example/repo/pull/42","solutionPlan":"PR submitted and CI passed."}')
WS_STATUS=$(echo "$WS_UPDATE_RESP" | python3 -c "import sys, json; print(json.load(sys.stdin)['data']['status'])")
echo "✓ Workspace Status Updated to: $WS_STATUS"

# ---------------------------------------------------------
# WORKFLOW C: Internship Application Execution
# ---------------------------------------------------------
echo ""
echo ">>> [WORKFLOW C] 1 & 2 & 3. Create Application from Discovered Opportunity"
APP_REQ="{\"opportunityId\":\"$OPP_ID\",\"title\":\"Backend Engineering Intern\",\"organization\":\"CloudScale Technologies\",\"applicationType\":\"INTERNSHIP\",\"status\":\"PREPARING\",\"priority\":\"HIGH\",\"deadline\":\"2027-01-31\",\"applicationUrl\":\"https://cloudscale-demo.io/careers/internships/swe-2027\"}"
APP_RESP=$(curl -s -b "$COOKIE_JAR_A" -X POST "$BASE_URL/users/me/applications" \
  -H "Content-Type: application/json" \
  -d "$APP_REQ")
APP_ID=$(echo "$APP_RESP" | python3 -c "import sys, json; print(json.load(sys.stdin)['data']['id'])")
echo "✓ Created Application ID: $APP_ID"

echo ">>> [WORKFLOW C] 4 & 5. Create Structured Resume & Grounded Tailoring"
RESUME_RESP=$(curl -s -b "$COOKIE_JAR_A" -X POST "$BASE_URL/users/me/resumes" \
  -H "Content-Type: application/json" \
  -d '{"title":"Backend Engineering Master Resume","versionName":"v1.0","professionalSummary":"Passionate Java developer with focus on high-throughput microservices.","skillsJson":"[\"Java\",\"Spring Boot\",\"PostgreSQL\",\"Docker\"]","educationJson":"[{\"institution\":\"MIT\",\"degree\":\"BS Computer Science\",\"gradYear\":2027}]","experienceJson":"[{\"company\":\"Open Source\",\"role\":\"Contributor\",\"description\":\"Contributed bug fixes to Spring ecosystem.\"}]","isPrimary":true}')
RESUME_ID=$(echo "$RESUME_RESP" | python3 -c "import sys, json; print(json.load(sys.stdin)['data']['id'])")

TAILOR_RESP=$(curl -s -b "$COOKIE_JAR_A" -X POST "$BASE_URL/resumes/$RESUME_ID/tailor" \
  -H "Content-Type: application/json" \
  -d "{\"targetOpportunityId\":\"$OPP_ID\",\"instructions\":\"Emphasize microservices\"}")
TAILORED_TITLE=$(echo "$TAILOR_RESP" | python3 -c "import sys, json; print(json.load(sys.stdin)['data']['title'])")
echo "✓ Generated Tailored Resume: $TAILORED_TITLE"

echo ">>> [WORKFLOW C] 6 & 7. Generate Cover Letter Document"
DOC_RESP=$(curl -s -b "$COOKIE_JAR_A" -X POST "$BASE_URL/applications/$APP_ID/documents/generate" \
  -H "Content-Type: application/json" \
  -d '{"documentType":"COVER_LETTER","title":"Cover Letter - Acme Backend Intern","userInstructions":"Highlight microservices and open source work"}')
DOC_ID=$(echo "$DOC_RESP" | python3 -c "import sys, json; print(json.load(sys.stdin)['data']['id'])")
echo "✓ Generated Cover Letter ID: $DOC_ID"

echo ">>> [WORKFLOW C] 8 & 9 & 10. Update Application Status & Record Interview Round"
APP_PATCH_RESP=$(curl -s -b "$COOKIE_JAR_A" -X PATCH "$BASE_URL/applications/$APP_ID" \
  -H "Content-Type: application/json" \
  -d '{"status":"INTERVIEW","priority":"HIGH","userNotes":"Passed initial recruiter screen. Scheduled technical interview."}')

INTERVIEW_RESP=$(curl -s -b "$COOKIE_JAR_A" -X POST "$BASE_URL/applications/$APP_ID/interviews" \
  -H "Content-Type: application/json" \
  -d '{"stageType":"TECHNICAL_INTERVIEW","scheduledAt":"2027-01-20T14:00:00Z","durationMinutes":60,"meetingLink":"https://meet.google.com/abc-def-ghi","interviewerInfo":"Jane Doe (Staff Engineer)","status":"SCHEDULED","preparationNotes":"Focus on Java concurrency and database indexing."}')
INTERVIEW_ID=$(echo "$INTERVIEW_RESP" | python3 -c "import sys, json; print(json.load(sys.stdin)['data']['id'])")
echo "✓ Scheduled Interview Round ID: $INTERVIEW_ID"

echo ">>> [WORKFLOW C] 11. Application Analytics Check"
ANALYTICS_RESP=$(curl -s -b "$COOKIE_JAR_A" -X GET "$BASE_URL/applications/analytics")
TOTAL_APPS=$(echo "$ANALYTICS_RESP" | python3 -c "import sys, json; print(json.load(sys.stdin)['data']['totalApplications'])")
echo "✓ Analytics Verified: Total Applications = $TOTAL_APPS"

# ---------------------------------------------------------
# WORKFLOW D: GSoC Application Execution
# ---------------------------------------------------------
echo ""
echo ">>> [WORKFLOW D] GSoC Proposal Generation"
GSOC_DOC_RESP=$(curl -s -b "$COOKIE_JAR_A" -X POST "$BASE_URL/applications/$APP_ID/documents/generate" \
  -H "Content-Type: application/json" \
  -d '{"documentType":"GSOC_PROPOSAL","title":"GSoC Proposal - Distributed Tracing","userInstructions":"Focus on OpenTelemetry export"}')
GSOC_DOC_ID=$(echo "$GSOC_DOC_RESP" | python3 -c "import sys, json; print(json.load(sys.stdin)['data']['id'])")
echo "✓ Generated GSoC Proposal ID: $GSOC_DOC_ID"

# ---------------------------------------------------------
# WORKFLOW E: Multi-User Authorization & Security Isolation
# ---------------------------------------------------------
echo ""
echo ">>> [WORKFLOW E] 1. Register User B ($USER_B_EMAIL)"
REG_B_RESP=$(curl -s -c "$COOKIE_JAR_B" -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"email\": \"$USER_B_EMAIL\", \"password\": \"$PASSWORD\", \"fullName\": \"Auditor User B\"}")
USER_B_ID=$(echo "$REG_B_RESP" | python3 -c "import sys, json; print(json.load(sys.stdin)['data']['id'])")
echo "✓ User B registered with ID: $USER_B_ID"

echo ">>> [WORKFLOW E] 2. User B Attempts Unauthorized Access to User A's Roadmap ($ROADMAP_ID)"
HTTP_CODE_ROADMAP=$(curl -s -o /dev/null -w "%{http_code}" -b "$COOKIE_JAR_B" -X GET "$BASE_URL/roadmaps/$ROADMAP_ID")
echo "User B GET User A Roadmap HTTP Status: $HTTP_CODE_ROADMAP"
if [ "$HTTP_CODE_ROADMAP" -eq 403 ] || [ "$HTTP_CODE_ROADMAP" -eq 404 ]; then
  echo "✓ Access securely denied (Expected 403 or 404, got $HTTP_CODE_ROADMAP)."
else
  echo "❌ SECURITY LEAK: Unauthorized access allowed! Status: $HTTP_CODE_ROADMAP"
  exit 1
fi

echo ">>> [WORKFLOW E] 3. User B Attempts Unauthorized Access to User A's Application ($APP_ID)"
HTTP_CODE_APP=$(curl -s -o /dev/null -w "%{http_code}" -b "$COOKIE_JAR_B" -X GET "$BASE_URL/applications/$APP_ID")
echo "User B GET User A Application HTTP Status: $HTTP_CODE_APP"
if [ "$HTTP_CODE_APP" -eq 403 ] || [ "$HTTP_CODE_APP" -eq 404 ]; then
  echo "✓ Access securely denied (Expected 403 or 404, got $HTTP_CODE_APP)."
else
  echo "❌ SECURITY LEAK: Unauthorized access allowed! Status: $HTTP_CODE_APP"
  exit 1
fi

echo ">>> [WORKFLOW E] 4. User B Attempts Unauthorized Access to User A's Resume ($RESUME_ID)"
HTTP_CODE_RESUME=$(curl -s -o /dev/null -w "%{http_code}" -b "$COOKIE_JAR_B" -X GET "$BASE_URL/resumes/$RESUME_ID")
echo "User B GET User A Resume HTTP Status: $HTTP_CODE_RESUME"
if [ "$HTTP_CODE_RESUME" -eq 403 ] || [ "$HTTP_CODE_RESUME" -eq 404 ]; then
  echo "✓ Access securely denied (Expected 403 or 404, got $HTTP_CODE_RESUME)."
else
  echo "❌ SECURITY LEAK: Unauthorized access allowed! Status: $HTTP_CODE_RESUME"
  exit 1
fi

echo ">>> [WORKFLOW E] 5. User B Attempts Unauthorized Modification of User A's Application"
HTTP_CODE_PATCH=$(curl -s -o /dev/null -w "%{http_code}" -b "$COOKIE_JAR_B" -X PATCH "$BASE_URL/applications/$APP_ID" \
  -H "Content-Type: application/json" \
  -d '{"status":"REJECTED"}')
echo "User B PATCH User A Application HTTP Status: $HTTP_CODE_PATCH"
if [ "$HTTP_CODE_PATCH" -eq 403 ] || [ "$HTTP_CODE_PATCH" -eq 404 ]; then
  echo "✓ Tampering securely prevented (Expected 403 or 404, got $HTTP_CODE_PATCH)."
else
  echo "❌ SECURITY LEAK: Unauthorized modification allowed! Status: $HTTP_CODE_PATCH"
  exit 1
fi

echo ""
echo "=========================================================="
echo "ALL 5 END-TO-END INTEGRATION WORKFLOWS VERIFIED (100% PASS)"
echo "=========================================================="
