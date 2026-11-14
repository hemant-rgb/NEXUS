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

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  validationErrors?: Record<string, string>;
  timestamp?: string;
}

export const OPPORTUNITY_CATEGORIES = [
  "Internships",
  "Jobs",
  "Google Summer of Code",
  "Open Source Programs",
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
