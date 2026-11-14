import {
  ApiResponse,
  CategoryCount,
  Opportunity,
  OpportunityAnalysis,
  OpportunityMatch,
  OpportunitySourceItem,
  PageResponse,
  SavedOpportunity,
  Skill,
  SkillGapReport,
  SyncRunResponse,
  User,
  UserPreferences,
  UserProfile,
  UserSkill,
  Roadmap,
  RoadmapSummary,
  RoadmapTask,
  RoadmapProgress,
  TaskResource,
  LearningResource,
  CreateRoadmapPayload,
  UpdateTaskPayload,
} from "./types";

const BASE_URL = process.env.NEXT_PUBLIC_API_URL || "/api/v1";

class ApiError extends Error {
  status: number;
  validationErrors?: Record<string, string>;

  constructor(message: string, status: number, validationErrors?: Record<string, string>) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.validationErrors = validationErrors;
  }
}

async function request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const url = endpoint.startsWith("http") ? endpoint : `${BASE_URL}${endpoint}`;
  const config: RequestInit = {
    ...options,
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
      Accept: "application/json",
      ...(options.headers || {}),
    },
  };

  const response = await fetch(url, config);

  let data: ApiResponse<T>;
  try {
    data = await response.json();
  } catch {
    if (!response.ok) {
      throw new ApiError(`Request failed with status ${response.status}`, response.status);
    }
    return null as unknown as T;
  }

  if (!response.ok || !data.success) {
    const errorMsg = data.message || `Request failed with status ${response.status}`;
    throw new ApiError(errorMsg, response.status, data.validationErrors);
  }

  return data.data;
}

export const api = {
  auth: {
    register: (body: { email: string; password: string; fullName: string }) =>
      request<User>("/auth/register", { method: "POST", body: JSON.stringify(body) }),
    login: (body: { email: string; password: string }) =>
      request<User>("/auth/login", { method: "POST", body: JSON.stringify(body) }),
    logout: () =>
      request<void>("/auth/logout", { method: "POST" }),
    me: () =>
      request<User>("/auth/me", { method: "GET" }),
  },

  profile: {
    get: () =>
      request<UserProfile>("/users/me/profile", { method: "GET" }),
    update: (data: Partial<UserProfile>) =>
      request<UserProfile>("/users/me/profile", { method: "PUT", body: JSON.stringify(data) }),
  },

  preferences: {
    get: () =>
      request<UserPreferences>("/users/me/preferences", { method: "GET" }),
    update: (data: Partial<UserPreferences>) =>
      request<UserPreferences>("/users/me/preferences", { method: "PUT", body: JSON.stringify(data) }),
  },

  skills: {
    catalog: (q?: string) =>
      request<Skill[]>(`/skills/catalog${q ? `?q=${encodeURIComponent(q)}` : ""}`, { method: "GET" }),
    getUserSkills: () =>
      request<UserSkill[]>("/users/me/skills", { method: "GET" }),
    addUserSkill: (data: { skillId?: string; skillName?: string; proficiency: string; yearsExperience?: number }) =>
      request<UserSkill>("/users/me/skills", { method: "POST", body: JSON.stringify(data) }),
    updateUserSkill: (id: string, data: { proficiency?: string; yearsExperience?: number }) =>
      request<UserSkill>(`/users/me/skills/${id}`, { method: "PATCH", body: JSON.stringify(data) }),
    deleteUserSkill: (id: string) =>
      request<void>(`/users/me/skills/${id}`, { method: "DELETE" }),
  },

  opportunities: {
    list: (params: {
      query?: string;
      category?: string;
      location?: string;
      remoteOnly?: boolean;
      status?: string;
      organization?: string;
      deadlineBefore?: string;
      deadlineAfter?: string;
      sortBy?: string;
      page?: number;
      size?: number;
    } = {}) => {
      const searchParams = new URLSearchParams();
      if (params.query) searchParams.set("query", params.query);
      if (params.category) searchParams.set("category", params.category);
      if (params.location) searchParams.set("location", params.location);
      if (params.remoteOnly !== undefined) searchParams.set("remoteOnly", String(params.remoteOnly));
      if (params.status) searchParams.set("status", params.status);
      if (params.organization) searchParams.set("organization", params.organization);
      if (params.deadlineBefore) searchParams.set("deadlineBefore", params.deadlineBefore);
      if (params.deadlineAfter) searchParams.set("deadlineAfter", params.deadlineAfter);
      if (params.sortBy) searchParams.set("sortBy", params.sortBy);
      if (params.page !== undefined) searchParams.set("page", String(params.page));
      if (params.size !== undefined) searchParams.set("size", String(params.size));
      const queryString = searchParams.toString();
      return request<PageResponse<Opportunity>>(`/opportunities${queryString ? `?${queryString}` : ""}`, {
        method: "GET",
      });
    },

    get: (id: string) =>
      request<Opportunity>(`/opportunities/${id}`, { method: "GET" }),

    categories: () =>
      request<CategoryCount[]>("/opportunities/categories", { method: "GET" }),

    featured: () =>
      request<Opportunity[]>("/opportunities/featured", { method: "GET" }),

    saved: () =>
      request<SavedOpportunity[]>("/users/me/saved-opportunities", { method: "GET" }),

    save: (opportunityId: string, notes?: string) =>
      request<SavedOpportunity>("/users/me/saved-opportunities", {
        method: "POST",
        body: JSON.stringify({ opportunityId, notes }),
      }),

    updateNotes: (opportunityId: string, notes: string) =>
      request<SavedOpportunity>(`/users/me/saved-opportunities/${opportunityId}`, {
        method: "PATCH",
        body: JSON.stringify({ notes }),
      }),

    removeSaved: (opportunityId: string) =>
      request<void>(`/users/me/saved-opportunities/${opportunityId}`, {
        method: "DELETE",
      }),
  },

  sources: {
    list: () =>
      request<OpportunitySourceItem[]>("/sources", { method: "GET" }),
    getRuns: (sourceKey: string) =>
      request<SyncRunResponse[]>(`/sources/${sourceKey}/runs`, { method: "GET" }),
    triggerSync: (sourceKey: string) =>
      request<any>(`/sources/${sourceKey}/sync`, { method: "POST" }),
  },

  matching: {
    getRecommendations: (params: {
      minScore?: number;
      category?: string;
      eligibilityStatus?: string;
      sortBy?: string;
      page?: number;
      size?: number;
    } = {}) => {
      const searchParams = new URLSearchParams();
      if (params.minScore !== undefined) searchParams.set("minScore", String(params.minScore));
      if (params.category) searchParams.set("category", params.category);
      if (params.eligibilityStatus) searchParams.set("eligibilityStatus", params.eligibilityStatus);
      if (params.sortBy) searchParams.set("sortBy", params.sortBy);
      if (params.page !== undefined) searchParams.set("page", String(params.page));
      if (params.size !== undefined) searchParams.set("size", String(params.size));
      const queryString = searchParams.toString();
      return request<PageResponse<OpportunityMatch>>(`/users/me/recommendations${queryString ? `?${queryString}` : ""}`, {
        method: "GET",
      });
    },

    getMatch: (opportunityId: string) =>
      request<OpportunityMatch>(`/opportunities/${opportunityId}/match`, { method: "GET" }),

    refreshMatches: () =>
      request<{ refreshedCount: number; userId: string }>("/users/me/matches/refresh", { method: "POST" }),

    getSkillGap: (opportunityId: string) =>
      request<SkillGapReport>(`/opportunities/${opportunityId}/skill-gap`, { method: "GET" }),
  },

  intelligence: {
    generateAnalysis: (opportunityId: string) =>
      request<OpportunityAnalysis>(`/opportunities/${opportunityId}/analysis`, { method: "POST" }),

    getAnalysis: (opportunityId: string) =>
      request<OpportunityAnalysis>(`/opportunities/${opportunityId}/analysis`, { method: "GET" }),
  },

  roadmaps: {
    list: () =>
      request<RoadmapSummary[]>("/users/me/roadmaps", { method: "GET" }),

    get: (id: string) =>
      request<Roadmap>(`/roadmaps/${id}`, { method: "GET" }),

    generate: (payload: CreateRoadmapPayload) =>
      request<Roadmap>("/roadmaps/generate", {
        method: "POST",
        body: JSON.stringify(payload),
      }),

    update: (id: string, payload: Partial<Roadmap>) =>
      request<Roadmap>(`/roadmaps/${id}`, {
        method: "PATCH",
        body: JSON.stringify(payload),
      }),

    delete: (id: string) =>
      request<void>(`/roadmaps/${id}`, { method: "DELETE" }),

    regenerate: (id: string) =>
      request<Roadmap>(`/roadmaps/${id}/regenerate`, { method: "POST" }),

    progress: (id: string) =>
      request<RoadmapProgress>(`/roadmaps/${id}/progress`, { method: "GET" }),

    reschedule: (id: string) =>
      request<RoadmapProgress>(`/roadmaps/${id}/reschedule`, { method: "POST" }),

    createTask: (roadmapId: string, payload: Partial<RoadmapTask>) =>
      request<RoadmapTask>(`/roadmaps/${roadmapId}/tasks`, {
        method: "POST",
        body: JSON.stringify(payload),
      }),

    updateTask: (roadmapId: string, taskId: string, payload: UpdateTaskPayload) =>
      request<RoadmapTask>(`/roadmaps/${roadmapId}/tasks/${taskId}`, {
        method: "PATCH",
        body: JSON.stringify(payload),
      }),

    deleteTask: (roadmapId: string, taskId: string) =>
      request<void>(`/roadmaps/${roadmapId}/tasks/${taskId}`, { method: "DELETE" }),

    addTaskResource: (taskId: string, payload: { resourceId?: string; customTitle?: string; customUrl?: string; customProvider?: string }) =>
      request<TaskResource>(`/tasks/${taskId}/resources`, {
        method: "POST",
        body: JSON.stringify(payload),
      }),

    removeTaskResource: (taskId: string, resourceId: string) =>
      request<void>(`/tasks/${taskId}/resources/${resourceId}`, { method: "DELETE" }),

    searchResources: (query?: string) =>
      request<LearningResource[]>(`/resources/search${query ? `?query=${encodeURIComponent(query)}` : ""}`, { method: "GET" }),
  },
};

export { ApiError };
