package com.nexus.roadmap.ai;

import com.nexus.roadmap.entity.RoadmapType;
import com.nexus.roadmap.entity.TaskPriority;
import com.nexus.roadmap.entity.TaskType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LocalFallbackRoadmapPlanner implements AiRoadmapPlanner {

    public static final String PROVIDER_NAME = "nexus-local-roadmap-planner";
    public static final String MODEL_NAME = "nexus-grounded-planner-v1";

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public AiRoadmapPlanResult planRoadmap(AiRoadmapContext context) {
        if (context.roadmapType() == RoadmapType.OPPORTUNITY_PREPARATION) {
            return planOpportunityRoadmap(context);
        } else {
            return planGoalRoadmap(context);
        }
    }

    private AiRoadmapPlanResult planOpportunityRoadmap(AiRoadmapContext context) {
        String oppTitle = context.opportunity() != null ? context.opportunity().getTitle() : "Target Opportunity";
        String org = context.opportunity() != null ? context.opportunity().getOrganization() : "Organization";
        String title = "Preparation Roadmap: " + oppTitle;
        String description = "Personalized step-by-step preparation for " + oppTitle + " at " + org +
                ", targeting critical prerequisite competencies and practical project demonstration.";
        String targetGoal = "Secure " + oppTitle + " (" + org + ")";

        List<String> missingReq = context.missingRequiredSkillsSafe();
        List<String> missingPref = context.missingPreferredSkillsSafe();
        List<String> existing = context.existingSkillsSafe();

        List<AiMilestonePlan> milestones = new ArrayList<>();

        // Milestone 1: Prerequisite Skills & Environment Setup
        List<AiTaskPlan> m1Tasks = new ArrayList<>();
        int taskSeq = 1;

        if (!missingReq.isEmpty()) {
            for (String skill : missingReq) {
                m1Tasks.add(new AiTaskPlan(
                        "Master Core Concepts & Syntax of " + skill,
                        "Deep-dive into official documentation and complete structured exercises for " + skill + ".",
                        "Complete official tutorials, build a functional sample prototype demonstrating idiomatic syntax, and verify zero compiler/lint errors.",
                        TaskType.LEARNING,
                        TaskPriority.HIGH,
                        4.0,
                        taskSeq++,
                        skill,
                        skill + " Documentation & Reference",
                        "https://docs.nexus.dev/skills/" + skill.toLowerCase()
                ));
            }
        } else {
            m1Tasks.add(new AiTaskPlan(
                    "Review Technical Foundations & Specification Alignment",
                    "Audit existing competencies against technical expectations of " + oppTitle + ".",
                    "Produce an architecture checklist and document repository structure matching organizational conventions.",
                    TaskType.LEARNING,
                    TaskPriority.MEDIUM,
                    3.0,
                    taskSeq++,
                    "Architecture",
                    "Official Documentation & Architecture Guides",
                    "https://docs.nexus.dev/architecture"
            ));
        }

        m1Tasks.add(new AiTaskPlan(
                "Configure Local Developer Toolchain & Sandbox",
                "Set up local runtime, Docker container sandbox, version control remotes, and dependency managers.",
                "Environment script completes with exit code 0; unit test runner passes on local machine.",
                TaskType.PRACTICE,
                TaskPriority.HIGH,
                2.5,
                taskSeq++,
                "DevOps",
                "Developer Environment Setup Checklist",
                "https://docs.docker.com/get-started/"
        ));

        milestones.add(new AiMilestonePlan(
                "Milestone 1: Core Prerequisites & Foundation Setup",
                "Establish essential technical competencies and verify developer toolchains.",
                1,
                1,
                m1Tasks
        ));

        // Milestone 2: Practical Architecture & Hands-On Exercises
        List<AiTaskPlan> m2Tasks = new ArrayList<>();
        taskSeq = 1;

        String primarySkill = !missingReq.isEmpty() ? missingReq.get(0) : (!existing.isEmpty() ? existing.get(0) : "Software Architecture");

        m2Tasks.add(new AiTaskPlan(
                "Implement Core Architecture with " + primarySkill,
                "Build modular service layers, data access repositories, and input validation handling.",
                "All business logic validation rules pass; integration tests verify database transactions and error handling.",
                TaskType.PRACTICE,
                TaskPriority.HIGH,
                5.0,
                taskSeq++,
                primarySkill,
                primarySkill + " Practical Guide",
                "https://spring.io/guides"
        ));

        if (!missingPref.isEmpty()) {
            String bonusSkill = missingPref.get(0);
            m2Tasks.add(new AiTaskPlan(
                    "Integrate Preferred Competency: " + bonusSkill,
                    "Incorporate " + bonusSkill + " into the practical service architecture as an enhancement.",
                    "Demonstrate working integration with clean logs and passing automated smoke tests.",
                    TaskType.PRACTICE,
                    TaskPriority.MEDIUM,
                    3.5,
                    taskSeq++,
                    bonusSkill,
                    bonusSkill + " Integration Reference",
                    "https://docs.docker.com"
            ));
        }

        m2Tasks.add(new AiTaskPlan(
                "Write Automated Test Suite & Coverage Reports",
                "Implement unit tests and end-to-end integration tests for all primary workflows.",
                "Automated test suite achieves >= 80% branch coverage with zero flaky tests.",
                TaskType.ASSESSMENT,
                TaskPriority.HIGH,
                3.0,
                taskSeq++,
                "Testing",
                "Unit & Integration Testing Best Practices",
                "https://git-scm.com/book/en/v2"
        ));

        milestones.add(new AiMilestonePlan(
                "Milestone 2: Practical Architecture & Integration",
                "Construct functional prototypes demonstrating required technical capabilities.",
                2,
                2,
                m2Tasks
        ));

        // Milestone 3: Real-World Demonstration & Project Deliverable
        List<AiTaskPlan> m3Tasks = new ArrayList<>();
        taskSeq = 1;

        m3Tasks.add(new AiTaskPlan(
                "Deliver Capstone Demonstration Project",
                "Assemble an end-to-end production-grade project addressing typical workflows of " + org + ".",
                "Live demo or clean GitHub repository with architectural documentation, Dockerfile, and clear setup instructions.",
                TaskType.PROJECT,
                TaskPriority.HIGH,
                6.0,
                taskSeq++,
                "FullStack",
                "GitHub Repository Guidelines & Clean Architecture",
                "https://github.com/donnemartin/system-design-primer"
        ));

        m3Tasks.add(new AiTaskPlan(
                "Conduct Peer Review & Code Quality Refactoring",
                "Perform static analysis, sanitize credentials, and optimize algorithmic complexity.",
                "SonarQube / linter reports zero high-severity warnings; all code meets clean-code conventions.",
                TaskType.REVISION,
                TaskPriority.MEDIUM,
                2.5,
                taskSeq++,
                "CodeQuality",
                "Clean Code & Refactoring Patterns",
                "https://docs.python.org/3/tutorial/"
        ));

        milestones.add(new AiMilestonePlan(
                "Milestone 3: Capstone Demonstration & Verification",
                "Create verifiable engineering artifacts directly applicable to the opportunity.",
                3,
                2,
                m3Tasks
        ));

        // Milestone 4: Final Proposal & Application Submission
        List<AiTaskPlan> m4Tasks = new ArrayList<>();
        taskSeq = 1;

        m4Tasks.add(new AiTaskPlan(
                "Draft Technical Application Proposal & Portfolio Narrative",
                "Synthesize engineering contributions and portfolio links into a focused application narrative.",
                "Proposal draft clearly articulates technical rationale, past achievements, and specific value for " + org + ".",
                TaskType.APPLICATION_PREPARATION,
                TaskPriority.HIGH,
                3.0,
                taskSeq++,
                "Communication",
                "Technical Proposal Guidelines",
                "https://summerofcode.withgoogle.com"
        ));

        m4Tasks.add(new AiTaskPlan(
                "Submit Application on Official Portal",
                "Review all eligibility documents, links, and contact information before final submission.",
                "Submission confirmation received on official portal before deadline.",
                TaskType.APPLICATION_PREPARATION,
                TaskPriority.HIGH,
                1.5,
                taskSeq++,
                "Execution",
                "Official Portal",
                context.opportunity() != null ? context.opportunity().getApplicationUrl() : "https://nexus.io"
        ));

        milestones.add(new AiMilestonePlan(
                "Milestone 4: Application Execution & Review",
                "Finalize technical proposals and submit application with verified artifacts.",
                4,
                1,
                m4Tasks
        ));

        return new AiRoadmapPlanResult(
                title,
                description,
                targetGoal,
                6,
                milestones,
                250,
                480,
                730,
                MODEL_NAME
        );
    }

    private AiRoadmapPlanResult planGoalRoadmap(AiRoadmapContext context) {
        String goal = context.targetGoal() != null && !context.targetGoal().isBlank()
                ? context.targetGoal()
                : "Full-Stack Software Engineering";
        String domain = context.targetDomain() != null && !context.targetDomain().isBlank()
                ? context.targetDomain()
                : "Software Engineering";
        String title = "Career Mastery Roadmap: " + goal;
        String description = "Comprehensive, outcome-driven learning roadmap designed to achieve " + goal +
                " in the " + domain + " domain through structured projects and verified milestones.";

        List<String> targetSkills = context.targetSkillsSafe();
        List<AiMilestonePlan> milestones = new ArrayList<>();

        // Milestone 1: Core Technologies & Foundational Tooling
        List<AiTaskPlan> m1Tasks = new ArrayList<>();
        int taskSeq = 1;

        if (!targetSkills.isEmpty()) {
            for (int i = 0; i < Math.min(2, targetSkills.size()); i++) {
                String skill = targetSkills.get(i);
                m1Tasks.add(new AiTaskPlan(
                        "Foundations & Syntactic Idioms of " + skill,
                        "Read official guides, establish mental models, and solve introductory practice problems in " + skill + ".",
                        "Pass basic competency tests and complete 5 fundamental programming exercises without tutorial assistance.",
                        TaskType.LEARNING,
                        TaskPriority.HIGH,
                        4.0,
                        taskSeq++,
                        skill,
                        skill + " Documentation",
                        "https://docs.nexus.dev/skills/" + skill.toLowerCase()
                ));
            }
        } else {
            m1Tasks.add(new AiTaskPlan(
                    "Programming Language Fundamentals & Memory Models",
                    "Study data structures, algorithm complexities, and memory management paradigms.",
                    "Solve 10 algorithm problems covering arrays, trees, and hash tables with verified linear time complexity.",
                    TaskType.LEARNING,
                    TaskPriority.HIGH,
                    5.0,
                    taskSeq++,
                    "Algorithms",
                    "Algorithms & Data Structures Reference",
                    "https://github.com/donnemartin/system-design-primer"
            ));
        }

        m1Tasks.add(new AiTaskPlan(
                "Setup Professional Git & Terminal Workflow",
                "Configure command line tooling, git hooks, SSH keys, and semantic commit standards.",
                "Git repository configured with clean commit history, branch protection rules, and README.",
                TaskType.PRACTICE,
                TaskPriority.MEDIUM,
                2.0,
                taskSeq++,
                "Git",
                "Pro Git Guide",
                "https://git-scm.com/book/en/v2"
        ));

        milestones.add(new AiMilestonePlan(
                "Milestone 1: Core Tooling & Syntactic Foundations",
                "Master primary programming syntax and build professional development environments.",
                1,
                2,
                m1Tasks
        ));

        // Milestone 2: Backend & System Architecture
        List<AiTaskPlan> m2Tasks = new ArrayList<>();
        taskSeq = 1;

        m2Tasks.add(new AiTaskPlan(
                "Build RESTful Backend with Relational Persistence",
                "Create a scalable backend service with transactional data persistence and connection pooling.",
                "Service successfully handles concurrent requests, validates payloads, and passes automated integration tests.",
                TaskType.PRACTICE,
                TaskPriority.HIGH,
                6.0,
                taskSeq++,
                "Backend",
                "Spring Boot REST Service Guide",
                "https://spring.io/guides/gs/rest-service/"
        ));

        m2Tasks.add(new AiTaskPlan(
                "Implement Relational Indexing & Query Optimization",
                "Design normalized SQL schemas with primary/foreign keys and analyze execution plans with EXPLAIN.",
                "Queries execute under 50ms with appropriate index scans; zero sequential table scans on large datasets.",
                TaskType.PRACTICE,
                TaskPriority.MEDIUM,
                3.5,
                taskSeq++,
                "PostgreSQL",
                "PostgreSQL SQL Tutorial",
                "https://www.postgresql.org/docs/current/tutorial.html"
        ));

        milestones.add(new AiMilestonePlan(
                "Milestone 2: Distributed Systems & Backend Architecture",
                "Develop enterprise-ready services with resilient database interactions.",
                2,
                2,
                m2Tasks
        ));

        // Milestone 3: Full-Stack Capstone Project
        List<AiTaskPlan> m3Tasks = new ArrayList<>();
        taskSeq = 1;

        m3Tasks.add(new AiTaskPlan(
                "Build Full-Stack Capstone Web Application",
                "Develop end-to-end web platform integrating interactive frontend, REST APIs, and automated test pipelines.",
                "Deployed application functions reliably with responsive UI, authenticated user sessions, and continuous deployment.",
                TaskType.PROJECT,
                TaskPriority.HIGH,
                8.0,
                taskSeq++,
                "FullStack",
                "React Documentation & Next.js Guide",
                "https://react.dev/learn"
        ));

        m3Tasks.add(new AiTaskPlan(
                "Containerize Application with Docker Compose",
                "Write multi-stage Dockerfiles for frontend and backend, with a docker-compose orchestrator.",
                "'docker compose up' boots all services, seeds initial database tables, and reaches healthy state.",
                TaskType.PRACTICE,
                TaskPriority.HIGH,
                3.0,
                taskSeq++,
                "Docker",
                "Docker Getting Started Guide",
                "https://docs.docker.com/get-started/"
        ));

        milestones.add(new AiMilestonePlan(
                "Milestone 3: Production Deployment & Capstone",
                "Deliver production-grade full-stack project demonstrating domain mastery.",
                3,
                3,
                m3Tasks
        ));

        return new AiRoadmapPlanResult(
                title,
                description,
                goal,
                7,
                milestones,
                220,
                450,
                670,
                MODEL_NAME
        );
    }
}
