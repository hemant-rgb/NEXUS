export interface User {
  id: string;
  email: string;
  fullName: string;
  role: string;
  emailVerified: boolean;
  createdAt: string;
}

export interface UserProfile {
  id?: string;
  userId: string;
  fullName?: string;
  email?: string;
  educationLevel?: string;
  degree?: string;
  fieldOfStudy?: string;
  institution?: string;
  graduationYear?: number;
  countryCode?: string;
  timezone?: string;
  experienceLevel?: string;
  bio?: string;
  githubUsername?: string;
  availabilityHours?: number;
  onboardingCompleted: boolean;
  onboardingStep: number;
  updatedAt?: string;
}

export interface UserPreferences {
  id?: string;
  userId?: string;
  preferredCategories: string[];
  remotePreference: string;
  preferredCountries: string[];
  weeklyHours: number;
  interests: string[];
  updatedAt?: string;
}

export interface Skill {
  id: string;
  name: string;
  category: string;
}

export type SkillProficiency = "Beginner" | "Intermediate" | "Advanced";

export interface UserSkill {
  id: string;
  skillId: string;
  skillName: string;
  category: string;
  proficiency: SkillProficiency;
  yearsExperience: number;
  createdAt: string;
}

export interface Opportunity {
  id: string;
  title: string;
  organization: string;
  description: string;
  category: string;
  opportunityType: string;
  location?: string;
  countryCode?: string;
  remoteAvailable: boolean;
  eligibilityCriteria?: string;
  requiredSkills: string[];
  preferredSkills: string[];
  applicationUrl: string;
  officialWebsiteUrl?: string;
  deadline?: string;
  deadlineType: "STRICT" | "ROLLING" | "ESTIMATED" | "UNKNOWN";
  startDate?: string;
  endDate?: string;
  status: "OPEN" | "UPCOMING" | "CLOSED" | "ARCHIVED";
  createdAt: string;
  lastVerifiedAt: string;
  sourceName?: string;
  canonicalUrl?: string;
  isSaved?: boolean;
}

export interface SavedOpportunity {
  id: string;
  opportunityId: string;
  opportunity: Opportunity;
  savedAt: string;
  notes?: string;
}

export interface CategoryCount {
  category: string;
  count: number;
}

export interface OpportunitySourceItem {
  id: string;
  name: string;
  sourceKey: string;
  sourceType: string;
  baseUrl: string;
  enabled: boolean;
  lastSuccessfulSyncAt?: string;
  lastSyncStatus?: string;
  totalRecordsSynced?: number;
}

export interface SyncRunResponse {
  id: string;
  sourceKey: string;
  sourceName: string;
  syncType: string;
  status: "SUCCESS" | "FAILED" | "IN_PROGRESS";
  startedAt: string;
  completedAt?: string;
  recordsFetched: number;
  recordsCreated: number;
  recordsUpdated: number;
  recordsSkipped: number;
  duplicatesDetected: number;
  errorMessage?: string;
}

export interface DimensionScores {
  skills: number;
  eligibility: number;
  interest: number;
  experience: number;
  deadline: number;
  location: number;
}

export interface EligibilityAssessment {
  status: "ELIGIBLE" | "INELIGIBLE" | "NEEDS_REVIEW" | "UNKNOWN";
  reason: string;
  missingProfileFields: string[];
  criteriaChecked: string[];
}

export interface OpportunityMatch {
  id: string;
  opportunityId: string;
  opportunity: Opportunity;
  overallScore: number;
  dimensionScores: DimensionScores;
  eligibility: EligibilityAssessment;
  matchedSkills: string[];
  missingRequiredSkills: string[];
  missingPreferredSkills: string[];
  positiveFactors: string[];
  negativeFactors: string[];
  recommendationReason: string;
  algorithmVersion: string;
  calculatedAt: string;
}

export interface SkillItem {
  skillName: string;
  status: "MATCHED" | "PARTIALLY_MATCHED" | "MISSING" | "UNKNOWN";
  userProficiency?: string;
  priority: "HIGH" | "MEDIUM" | "LOW";
  isRequired: boolean;
}

export interface SkillGapReport {
  opportunityId: string;
  opportunityTitle: string;
  matchedSkills: SkillItem[];
  partiallyMatchedSkills: SkillItem[];
  missingRequiredSkills: SkillItem[];
  missingPreferredSkills: SkillItem[];
  totalSkillsEvaluated: number;
  matchPercentage: number;
}

export interface OpportunityAnalysis {
  id: string;
  opportunityId: string;
  opportunityTitle: string;
  organization: string;
  summary: string;
  organizationOverview?: string;
  expectations?: string;
  keyDates?: string;
  applicationProcess?: string;
  requirementsAnalysis?: string;
  personalizedAssessment?: string;
  strengths: string[];
  weaknesses: string[];
  missingInformation: string[];
  eligibilityConcerns?: string;
  recommendation: "STRONG_FIT" | "POTENTIAL_FIT" | "WEAK_FIT" | "NEEDS_INFORMATION";
  sourceReferences?: string;
  modelName: string;
  promptVersion: string;
  generatedAt: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  empty: boolean;
  first: boolean;
  last: boolean;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  validationErrors?: Record<string, string>;
  timestamp?: string;
}

export const OPPORTUNITY_CATEGORIES = [
  "Internships",
  "Full-time jobs",
  "Google Summer of Code",
  "Open-source programs",
  "Hackathons",
  "Scholarships",
  "Fellowships",
  "Competitions",
] as const;

export const DEFAULT_INTERESTS = [
  "Software Engineering",
  "Artificial Intelligence",
  "Machine Learning",
  "Web Development",
  "Backend Development",
  "Data Science",
  "Open Source",
  "Competitive Programming",
  "Cybersecurity",
  "Cloud Computing",
] as const;

// --- Phase 5: Roadmaps & Task Management Types ---

export type RoadmapType = "OPPORTUNITY_PREPARATION" | "GOAL_BASED";
export type RoadmapStatus = "NOT_STARTED" | "ACTIVE" | "PAUSED" | "COMPLETED" | "ARCHIVED";
export type MilestoneStatus = "NOT_STARTED" | "IN_PROGRESS" | "COMPLETED" | "SKIPPED";
export type TaskType = "LEARNING" | "PRACTICE" | "PROJECT" | "RESEARCH" | "REVISION" | "APPLICATION_PREPARATION" | "ASSESSMENT";
export type TaskPriority = "HIGH" | "MEDIUM" | "LOW";
export type TaskStatus = "NOT_STARTED" | "IN_PROGRESS" | "COMPLETED" | "SKIPPED" | "BLOCKED";

export interface TaskResource {
  id: string;
  taskId: string;
  resourceId?: string;
  title: string;
  url: string;
  provider: string;
  resourceType: string;
  verificationStatus: string;
}

export interface LearningResource {
  id: string;
  title: string;
  description: string;
  resourceUrl: string;
  resourceType: string;
  provider: string;
  skillName?: string;
  difficultyLevel: string;
  estimatedDuration?: string;
  verificationStatus: "VERIFIED" | "COMMUNITY" | "UNVERIFIED";
}

export interface RoadmapTask {
  id: string;
  milestoneId: string;
  roadmapId: string;
  title: string;
  description?: string;
  completionCriteria?: string;
  taskType: TaskType;
  priority: TaskPriority;
  estimatedHours: number;
  sequenceOrder: number;
  scheduledDate?: string;
  status: TaskStatus;
  completedAt?: string;
  userNotes?: string;
  dependencies?: string;
  resources: TaskResource[];
}

export interface RoadmapMilestone {
  id: string;
  roadmapId: string;
  title: string;
  description?: string;
  sequenceOrder: number;
  estimatedDurationWeeks: number;
  targetDate?: string;
  status: MilestoneStatus;
  completionPercentage: number;
  tasks: RoadmapTask[];
}

export interface RoadmapSummary {
  id: string;
  title: string;
  description?: string;
  roadmapType: RoadmapType;
  targetOpportunityId?: string;
  targetOpportunityTitle?: string;
  targetGoal?: string;
  status: RoadmapStatus;
  startDate: string;
  targetCompletionDate?: string;
  estimatedDurationWeeks: number;
  availableHoursPerWeek: number;
  overallProgressPercentage: number;
  totalMilestones: number;
  completedMilestones: number;
  totalTasks: number;
  completedTasks: number;
  createdAt: string;
  updatedAt: string;
}

export interface Roadmap {
  id: string;
  userId: string;
  title: string;
  description?: string;
  roadmapType: RoadmapType;
  targetOpportunityId?: string;
  targetOpportunityTitle?: string;
  targetGoal?: string;
  status: RoadmapStatus;
  startDate: string;
  targetCompletionDate?: string;
  estimatedDurationWeeks: number;
  availableHoursPerWeek: number;
  generationVersion: string;
  overallProgressPercentage: number;
  totalMilestones: number;
  completedMilestones: number;
  totalTasks: number;
  completedTasks: number;
  remainingEstimatedHours: number;
  milestones: RoadmapMilestone[];
  createdAt: string;
  updatedAt: string;
}

export interface RoadmapProgress {
  roadmapId: string;
  status: RoadmapStatus;
  overallProgressPercentage: number;
  totalMilestones: number;
  completedMilestones: number;
  totalTasks: number;
  completedTasks: number;
  inProgressTasks: number;
  blockedTasks: number;
  totalEstimatedHours: number;
  remainingEstimatedHours: number;
  projectedCompletionDate?: string;
  overdueTasks: RoadmapTask[];
  upcomingTasks: RoadmapTask[];
}

export interface CreateRoadmapPayload {
  roadmapType: RoadmapType;
  opportunityId?: string;
  targetGoal?: string;
  targetDomain?: string;
  currentSkillLevel?: string;
  targetSkills?: string[];
  availableHoursPerWeek?: number;
  targetCompletionDate?: string;
  customTitle?: string;
}

export interface UpdateTaskPayload {
  title?: string;
  description?: string;
  completionCriteria?: string;
  taskType?: TaskType;
  priority?: TaskPriority;
  estimatedHours?: number;
  sequenceOrder?: number;
  scheduledDate?: string;
  status?: TaskStatus;
  userNotes?: string;
}

// -------------------------------------------------------------
// Phase 6: GitHub Intelligence & Open-Source Contributions
// -------------------------------------------------------------

export interface GitHubConnection {
  id: string;
  githubUsername: string;
  avatarUrl?: string;
  profileUrl?: string;
  publicReposCount: number;
  followersCount: number;
  followingCount: number;
  status: string;
  connectedAt: string;
  lastSyncedAt?: string;
}

export interface GitHubRepository {
  id: string;
  githubRepoId: number;
  ownerLogin: string;
  name: string;
  fullName: string;
  description?: string;
  htmlUrl: string;
  primaryLanguage?: string;
  topics?: string;
  starsCount: number;
  forksCount: number;
  openIssuesCount: number;
  defaultBranch: string;
  licenseSpdx?: string;
  hasContributingGuide: boolean;
  contributingUrl?: string;
  lastPushedAt?: string;
  isSaved?: boolean;
}

export interface GitHubIssue {
  id: string;
  githubIssueId: number;
  repositoryId?: string;
  repositoryFullName?: string;
  primaryLanguage?: string;
  issueNumber: number;
  title: string;
  body?: string;
  htmlUrl: string;
  state: string;
  labels: string[];
  commentsCount: number;
  isGoodFirstIssue: boolean;
  isHelpWanted: boolean;
  githubCreatedAt?: string;
  githubUpdatedAt?: string;
  isSaved?: boolean;
  suitabilityScore?: number;
}

export interface IssueSuitability {
  issueId: string;
  repositoryFullName: string;
  suitabilityScore: number;
  matchingSkills: string[];
  missingSkills: string[];
  experienceLevelSuitability: string;
  difficultyAssessment: string;
  reasoning: string[];
}

export type ContributionStatus =
  | "INTERESTED"
  | "RESEARCHING"
  | "PLANNED"
  | "IN_PROGRESS"
  | "REVIEW_REQUESTED"
  | "CHANGES_REQUESTED"
  | "MERGED"
  | "CLOSED"
  | "ABANDONED";

export interface ContributionWorkspace {
  id: string;
  repositoryId?: string;
  repositoryFullName?: string;
  issueId?: string;
  issueNumber?: number;
  issueTitle?: string;
  issueHtmlUrl?: string;
  roadmapTaskId?: string;
  title: string;
  status: ContributionStatus;
  targetBranch: string;
  pullRequestUrl?: string;
  researchNotes?: string;
  solutionPlan?: string;
  createdAt: string;
  updatedAt: string;
}

export interface GitHubAiAnalysis {
  targetType: "REPOSITORY" | "ISSUE";
  targetName: string;
  summary: string;
  technicalOverview: string;
  keyComponentsOrPrerequisites: string[];
  suggestedSteps: string[];
  testingGuidance: string;
  warningsOrTips: string[];
}

// -------------------------------------------------------------
// Phase 7: Application Management & Execution
// -------------------------------------------------------------

export type ApplicationStatus =
  | "INTERESTED"
  | "PREPARING"
  | "READY_TO_APPLY"
  | "APPLIED"
  | "ASSESSMENT"
  | "INTERVIEW"
  | "OFFER_RECEIVED"
  | "REJECTED"
  | "WITHDRAWN"
  | "ACCEPTED";

export type ApplicationPriority = "HIGH" | "MEDIUM" | "LOW";

export type ActivityType =
  | "STATUS_CHANGED"
  | "NOTE_ADDED"
  | "DOCUMENT_ATTACHED"
  | "INTERVIEW_SCHEDULED"
  | "SUBMITTED"
  | "DEADLINE_UPDATED";

export type DocumentType =
  | "COVER_LETTER"
  | "GSOC_PROPOSAL"
  | "MOTIVATION_STATEMENT"
  | "APPLICATION_ESSAY"
  | "APPLICATION_ANSWERS";

export type InterviewStageType =
  | "ONLINE_ASSESSMENT"
  | "CODING_ROUND"
  | "TECHNICAL_INTERVIEW"
  | "HR_INTERVIEW"
  | "PROJECT_DISCUSSION"
  | "TAKE_HOME";

export type InterviewStatus =
  | "SCHEDULED"
  | "COMPLETED"
  | "CANCELLED"
  | "PASSED"
  | "FAILED";

export interface Application {
  id: string;
  userId: string;
  opportunityId?: string;
  opportunityTitle?: string;
  roadmapId?: string;
  roadmapTitle?: string;
  title: string;
  organization: string;
  applicationType: string;
  applicationUrl?: string;
  status: ApplicationStatus;
  priority: ApplicationPriority;
  deadline?: string;
  appliedAt?: string;
  expectedResponseDate?: string;
  userNotes?: string;
  activitiesCount: number;
  documentsCount: number;
  interviewsCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface ApplicationActivity {
  id: string;
  applicationId: string;
  activityType: ActivityType;
  fromStatus?: string;
  toStatus?: string;
  description: string;
  createdAt: string;
}

export interface Resume {
  id: string;
  userId: string;
  title: string;
  versionName: string;
  targetOpportunityId?: string;
  targetOpportunityTitle?: string;
  professionalSummary?: string;
  educationJson?: string;
  skillsJson?: string;
  experienceJson?: string;
  projectsJson?: string;
  contributionsJson?: string;
  isPrimary: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ApplicationDocument {
  id: string;
  applicationId: string;
  documentType: DocumentType;
  title: string;
  content: string;
  structuredSections?: string;
  version: string;
  createdAt: string;
  updatedAt: string;
}

export interface ApplicationInterview {
  id: string;
  applicationId: string;
  stageType: InterviewStageType;
  scheduledAt?: string;
  durationMinutes: number;
  interviewerInfo?: string;
  meetingLink?: string;
  preparationNotes?: string;
  status: InterviewStatus;
  outcomeNotes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ApplicationAnalytics {
  totalApplications: number;
  preparingCount: number;
  appliedCount: number;
  inProgressCount: number;
  interviewCount: number;
  offerCount: number;
  rejectedCount: number;
  interviewRate: number;
  offerRate: number;
  upcomingDeadlinesCount: number;
  statusBreakdown: Record<string, number>;
}

export interface ApplicationQuestionResponse {
  question: string;
  suggestedAnswer: string;
  keyTalkingPoints: string[];
  advisoryNotice: string;
}

