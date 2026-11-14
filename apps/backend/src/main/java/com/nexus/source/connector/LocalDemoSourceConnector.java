package com.nexus.source.connector;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class LocalDemoSourceConnector implements OpportunitySourceConnector {

    public static final String SOURCE_KEY = "nexus-local-demo";

    @Override
    public String getSourceKey() {
        return SOURCE_KEY;
    }

    @Override
    public SourceMetadata getMetadata() {
        return new SourceMetadata(
                SOURCE_KEY,
                "NEXUS Verified Local Catalog",
                "DEMO",
                "Reliable offline catalog containing verified opportunities across all categories",
                "https://nexus.dev/catalog"
        );
    }

    @Override
    public SyncResult fetchOpportunities(SyncRequest request) {
        List<NormalizedOpportunity> list = new ArrayList<>();

        list.add(new NormalizedOpportunity(
                "DEMO-GSOC-2027",
                "Google Summer of Code 2027 Contributor",
                "Google Open Source",
                "Participate as a contributor in GSoC with open-source mentor organizations. Write code, learn with industry mentors, and contribute to production open-source software.",
                "Google Summer of Code",
                "OPEN_SOURCE",
                "Global Remote",
                "GLOBAL",
                true,
                "Enrolled student or beginner in open-source software development. 18+ years of age.",
                List.of("Git & GitHub", "Python", "Java", "C++"),
                List.of("Open Source Contribution", "Docker", "Linux / Bash"),
                "https://summerofcode.withgoogle.com/get-started",
                "https://summerofcode.withgoogle.com",
                LocalDate.of(2027, 4, 15),
                "STRICT",
                LocalDate.of(2027, 5, 20),
                LocalDate.of(2027, 8, 30),
                "OPEN",
                "{\"source\":\"demo-catalog\",\"track\":\"open-source\"}"
        ));

        list.add(new NormalizedOpportunity(
                "DEMO-CLOUDSCALE-SWE",
                "Distributed Systems Software Engineering Intern",
                "CloudScale Technologies",
                "Join our core infrastructure engineering group to build low-latency distributed storage services. Work directly on cluster scheduling, telemetry pipelines, and consensus implementations.",
                "Internships",
                "INTERNSHIP",
                "San Francisco, CA",
                "US",
                true,
                "Currently pursuing a B.S. or M.S. in Computer Science or related STEM discipline graduating within 1-2 years.",
                List.of("Go", "Data Structures & Algorithms", "Linux / Bash"),
                List.of("Kubernetes", "Docker", "Distributed Systems", "SQL"),
                "https://cloudscale-demo.io/careers/internships/swe-2027",
                "https://cloudscale-demo.io",
                LocalDate.of(2027, 3, 1),
                "ROLLING",
                LocalDate.of(2027, 6, 1),
                LocalDate.of(2027, 8, 25),
                "OPEN",
                "{\"source\":\"demo-catalog\",\"track\":\"internship\"}"
        ));

        list.add(new NormalizedOpportunity(
                "DEMO-VECTORSTREAM-JR",
                "Junior Backend Platform Engineer",
                "VectorStream",
                "Design and maintain microservice APIs, message streaming pipelines, and high-throughput PostgreSQL databases powering developer collaboration tools.",
                "Full-time jobs",
                "FULL_TIME",
                "New York, NY",
                "US",
                false,
                "Recent graduates or early-career developers with up to 2 years of backend engineering experience.",
                List.of("Java", "Spring Boot", "PostgreSQL", "REST API"),
                List.of("Docker", "Redis", "System Design", "AWS"),
                "https://vectorstream-demo.io/jobs/junior-backend",
                "https://vectorstream-demo.io",
                LocalDate.of(2027, 2, 28),
                "ROLLING",
                LocalDate.of(2027, 4, 1),
                null,
                "OPEN",
                "{\"source\":\"demo-catalog\",\"track\":\"full-time\"}"
        ));

        list.add(new NormalizedOpportunity(
                "DEMO-MLH-2027",
                "MLH Fellowship — Open Source Track",
                "Major League Hacking",
                "A 12-week intensive remote internship alternative. Fellows work with maintainers on major open-source projects used by millions worldwide while receiving an educational stipend.",
                "Open-source programs",
                "OPEN_SOURCE",
                "Global Remote",
                "GLOBAL",
                true,
                "Developers with proficient programming fundamentals looking to contribute professionally to open-source.",
                List.of("Git & GitHub", "Python", "JavaScript", "TypeScript"),
                List.of("Open Source Contribution", "FastAPI", "React"),
                "https://fellowship.mlh.io/programs/open-source",
                "https://fellowship.mlh.io",
                LocalDate.of(2027, 3, 31),
                "STRICT",
                LocalDate.of(2027, 6, 1),
                LocalDate.of(2027, 8, 20),
                "OPEN",
                "{\"source\":\"demo-catalog\",\"track\":\"open-source\"}"
        ));

        list.add(new NormalizedOpportunity(
                "DEMO-AI-HACK-2027",
                "Global Autonomous AI Hackathon 2027",
                "AI Alliance Foundation",
                "A 48-hour virtual worldwide hackathon challenging developer teams to construct privacy-preserving agentic software and developer tooling.",
                "Hackathons",
                "HACKATHON",
                "Virtual / Online",
                "GLOBAL",
                true,
                "Open to university students, engineers, and researchers worldwide. Teams of 1 to 4 individuals.",
                List.of("Python", "TypeScript", "React", "Next.js"),
                List.of("Machine Learning", "FastAPI", "Docker"),
                "https://ai-alliance-hackathon-demo.org/register",
                "https://ai-alliance-hackathon-demo.org",
                LocalDate.of(2027, 5, 10),
                "STRICT",
                LocalDate.of(2027, 5, 15),
                LocalDate.of(2027, 5, 17),
                "UPCOMING",
                "{\"source\":\"demo-catalog\",\"track\":\"hackathon\"}"
        ));

        list.add(new NormalizedOpportunity(
                "DEMO-GHC-SCHOLAR-2027",
                "Grace Hopper Women in Computing Tech Scholarship",
                "AnitaB.org",
                "Full sponsorship scholarship covering registration, mentorship, and travel to the annual technical computing conference and career fair.",
                "Scholarships",
                "SCHOLARSHIP",
                "Orlando, FL",
                "US",
                false,
                "Undergraduate or graduate female/non-binary students studying computer science or related engineering field.",
                List.of("Computer Science"),
                List.of("Data Structures & Algorithms", "Software Engineering"),
                "https://anitab-demo.org/ghc-scholarships-2027",
                "https://anitab-demo.org",
                LocalDate.of(2027, 4, 30),
                "STRICT",
                LocalDate.of(2027, 9, 22),
                LocalDate.of(2027, 9, 25),
                "OPEN",
                "{\"source\":\"demo-catalog\",\"track\":\"scholarship\"}"
        ));

        list.add(new NormalizedOpportunity(
                "DEMO-THIEL-2027",
                "Thiel Fellowship Program",
                "The Thiel Foundation",
                "A two-year, $100,000 fellowship grant given to young entrepreneurs and technical builders who want to build new things instead of sitting in a classroom.",
                "Fellowships",
                "FELLOWSHIP",
                "San Francisco, CA / Remote",
                "US",
                true,
                "Must be 22 or younger at time of application. Building impactful technical projects or commercial research.",
                List.of("System Design", "Software Engineering"),
                List.of("Machine Learning", "Open Source Contribution"),
                "https://thielfellowship.org/apply",
                "https://thielfellowship.org",
                LocalDate.of(2027, 12, 31),
                "ROLLING",
                null,
                null,
                "OPEN",
                "{\"source\":\"demo-catalog\",\"track\":\"fellowship\"}"
        ));

        list.add(new NormalizedOpportunity(
                "DEMO-CODEMATRIX-2027",
                "International Competitive Programming Challenge",
                "CodeMatrix Contest Arena",
                "Premier individual algorithmic contest featuring problems spanning advanced graph theory, dynamic programming, and computational geometry.",
                "Competitions",
                "COMPETITION",
                "Online",
                "GLOBAL",
                true,
                "Open to university students and competitive programmers worldwide.",
                List.of("C++", "Java", "Data Structures & Algorithms"),
                List.of("Competitive Programming", "Python"),
                "https://codematrix-arena-demo.org/contests/2027-championship",
                "https://codematrix-arena-demo.org",
                LocalDate.of(2027, 6, 15),
                "STRICT",
                LocalDate.of(2027, 6, 20),
                LocalDate.of(2027, 6, 21),
                "OPEN",
                "{\"source\":\"demo-catalog\",\"track\":\"competition\"}"
        ));

        return SyncResult.success(SOURCE_KEY, list);
    }
}
