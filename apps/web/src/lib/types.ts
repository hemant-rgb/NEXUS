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
