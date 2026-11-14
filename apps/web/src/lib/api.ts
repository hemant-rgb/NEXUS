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
  GitHubConnection,
  GitHubRepository,
  GitHubIssue,
  IssueSuitability,
  ContributionWorkspace,
  GitHubAiAnalysis,
  Application,
  ApplicationActivity,
  Resume,
  ApplicationDocument,
  ApplicationInterview,
  ApplicationAnalytics,
  ApplicationQuestionResponse,
  Notification,
  NotificationPreference,
  ActivityItem,
  AgentMetadata,
  AgentRun,
  AgentStep,
  AgentApproval,
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

  github: {
    getConnection: () =>
      request<GitHubConnection | null>("/github/connection", { method: "GET" }),

    connect: (payload: { accessToken: string; username?: string; scopes?: string }) =>
      request<GitHubConnection>("/github/connect", {
        method: "POST",
        body: JSON.stringify(payload),
      }),

    disconnect: () =>
      request<void>("/github/disconnect", { method: "DELETE" }),

    getProfile: () =>
      request<GitHubConnection>("/github/profile", { method: "GET" }),

    searchRepositories: (query?: string, language?: string) => {
      const params = new URLSearchParams();
      if (query) params.set("query", query);
      if (language && language !== "ALL") params.set("language", language);
      const qs = params.toString();
      return request<GitHubRepository[]>(`/github/repositories${qs ? `?${qs}` : ""}`, { method: "GET" });
    },

    getSavedRepositories: () =>
      request<GitHubRepository[]>("/github/repositories/saved", { method: "GET" }),

    getRepository: (id: string) =>
      request<GitHubRepository>(`/github/repositories/${id}`, { method: "GET" }),

    saveRepository: (id: string) =>
      request<void>(`/github/repositories/${id}/save`, { method: "POST" }),

    unsaveRepository: (id: string) =>
      request<void>(`/github/repositories/${id}/save`, { method: "DELETE" }),

    analyzeRepository: (id: string) =>
      request<GitHubAiAnalysis>(`/github/repositories/${id}/analysis`, { method: "POST" }),

    searchIssues: (params?: { query?: string; language?: string; goodFirstOnly?: boolean; helpWantedOnly?: boolean; state?: string }) => {
      const q = new URLSearchParams();
      if (params?.query) q.set("query", params.query);
      if (params?.language && params.language !== "ALL") q.set("language", params.language);
      if (params?.goodFirstOnly) q.set("goodFirstOnly", "true");
      if (params?.helpWantedOnly) q.set("helpWantedOnly", "true");
      if (params?.state && params.state !== "ALL") q.set("state", params.state);
      const qs = q.toString();
      return request<GitHubIssue[]>(`/github/issues${qs ? `?${qs}` : ""}`, { method: "GET" });
    },

    getSavedIssues: () =>
      request<GitHubIssue[]>("/github/issues/saved", { method: "GET" }),

    getIssue: (id: string) =>
      request<GitHubIssue>(`/github/issues/${id}`, { method: "GET" }),

    saveIssue: (id: string) =>
      request<void>(`/github/issues/${id}/save`, { method: "POST" }),

    unsaveIssue: (id: string) =>
      request<void>(`/github/issues/${id}/save`, { method: "DELETE" }),

    getIssueSuitability: (id: string) =>
      request<IssueSuitability>(`/github/issues/${id}/suitability`, { method: "GET" }),

    analyzeIssue: (id: string) =>
      request<GitHubAiAnalysis>(`/github/issues/${id}/analysis`, { method: "POST" }),

    getContributions: (status?: string) =>
      request<ContributionWorkspace[]>(`/users/me/contributions${status ? `?status=${status}` : ""}`, { method: "GET" }),

    getContribution: (id: string) =>
      request<ContributionWorkspace>(`/contributions/${id}`, { method: "GET" }),

    createContribution: (payload: { repositoryId?: string; issueId?: string; roadmapTaskId?: string; title: string; status?: string; targetBranch?: string; pullRequestUrl?: string; researchNotes?: string; solutionPlan?: string }) =>
      request<ContributionWorkspace>("/users/me/contributions", {
        method: "POST",
        body: JSON.stringify(payload),
      }),

    updateContribution: (id: string, payload: Partial<ContributionWorkspace>) =>
      request<ContributionWorkspace>(`/contributions/${id}`, {
        method: "PATCH",
        body: JSON.stringify(payload),
      }),

    deleteContribution: (id: string) =>
      request<void>(`/contributions/${id}`, { method: "DELETE" }),
  },

  applications: {
    list: (status?: string) =>
      request<Application[]>(`/users/me/applications${status && status !== "ALL" ? `?status=${status}` : ""}`, { method: "GET" }),

    get: (id: string) =>
      request<Application>(`/applications/${id}`, { method: "GET" }),

    create: (payload: {
      opportunityId?: string;
      roadmapId?: string;
      title: string;
      organization: string;
      applicationType?: string;
      applicationUrl?: string;
      status?: string;
      priority?: string;
      deadline?: string;
      expectedResponseDate?: string;
      userNotes?: string;
    }) =>
      request<Application>("/users/me/applications", {
        method: "POST",
        body: JSON.stringify(payload),
      }),

    update: (id: string, payload: Partial<Application>) =>
      request<Application>(`/applications/${id}`, {
        method: "PATCH",
        body: JSON.stringify(payload),
      }),

    delete: (id: string) =>
      request<void>(`/applications/${id}`, { method: "DELETE" }),

    getActivities: (id: string) =>
      request<ApplicationActivity[]>(`/applications/${id}/activities`, { method: "GET" }),

    createActivity: (id: string, payload: { activityType: string; fromStatus?: string; toStatus?: string; description: string }) =>
      request<ApplicationActivity>(`/applications/${id}/activities`, {
        method: "POST",
        body: JSON.stringify(payload),
      }),

    getAnalytics: () =>
      request<ApplicationAnalytics>("/applications/analytics", { method: "GET" }),

    getDocuments: (applicationId: string) =>
      request<ApplicationDocument[]>(`/applications/${applicationId}/documents`, { method: "GET" }),

    generateDocument: (applicationId: string, payload: { documentType: string; title?: string; userInstructions?: string }) =>
      request<ApplicationDocument>(`/applications/${applicationId}/documents/generate`, {
        method: "POST",
        body: JSON.stringify(payload),
      }),

    updateDocument: (applicationId: string, documentId: string, payload: { title?: string; userInstructions?: string }) =>
      request<ApplicationDocument>(`/applications/${applicationId}/documents/${documentId}`, {
        method: "PATCH",
        body: JSON.stringify(payload),
      }),

    deleteDocument: (applicationId: string, documentId: string) =>
      request<void>(`/applications/${applicationId}/documents/${documentId}`, { method: "DELETE" }),

    answerQuestion: (payload: { question: string; context?: string }) =>
      request<ApplicationQuestionResponse>("/applications/questions/answer", {
        method: "POST",
        body: JSON.stringify(payload),
      }),

    getInterviews: (applicationId: string) =>
      request<ApplicationInterview[]>(`/applications/${applicationId}/interviews`, { method: "GET" }),

    scheduleInterview: (applicationId: string, payload: {
      stageType: string;
      scheduledAt?: string;
      durationMinutes?: number;
      interviewerInfo?: string;
      meetingLink?: string;
      preparationNotes?: string;
      status?: string;
      outcomeNotes?: string;
    }) =>
      request<ApplicationInterview>(`/applications/${applicationId}/interviews`, {
        method: "POST",
        body: JSON.stringify(payload),
      }),

    updateInterview: (applicationId: string, interviewId: string, payload: Partial<ApplicationInterview>) =>
      request<ApplicationInterview>(`/applications/${applicationId}/interviews/${interviewId}`, {
        method: "PATCH",
        body: JSON.stringify(payload),
      }),

    deleteInterview: (applicationId: string, interviewId: string) =>
      request<void>(`/applications/${applicationId}/interviews/${interviewId}`, { method: "DELETE" }),

    listResumes: () =>
      request<Resume[]>("/users/me/resumes", { method: "GET" }),

    getResume: (id: string) =>
      request<Resume>(`/resumes/${id}`, { method: "GET" }),

    createResume: (payload: Partial<Resume>) =>
      request<Resume>("/users/me/resumes", {
        method: "POST",
        body: JSON.stringify(payload),
      }),

    updateResume: (id: string, payload: Partial<Resume>) =>
      request<Resume>(`/resumes/${id}`, {
        method: "PATCH",
        body: JSON.stringify(payload),
      }),

    deleteResume: (id: string) =>
      request<void>(`/resumes/${id}`, { method: "DELETE" }),

    tailorResume: (id: string, payload: { targetOpportunityId: string; instructions?: string }) =>
      request<Resume>(`/resumes/${id}/tailor`, {
        method: "POST",
        body: JSON.stringify(payload),
      }),
  },

  pulse: {
    getNotifications: (params: { status?: string; category?: string; page?: number; size?: number } = {}) => {
      const searchParams = new URLSearchParams();
      if (params.status && params.status !== "ALL") searchParams.set("status", params.status);
      if (params.category && params.category !== "ALL") searchParams.set("category", params.category);
      if (params.page !== undefined) searchParams.set("page", String(params.page));
      if (params.size !== undefined) searchParams.set("size", String(params.size));
      const qs = searchParams.toString();
      return request<PageResponse<Notification>>(`/users/me/notifications${qs ? `?${qs}` : ""}`, { method: "GET" });
    },

    getUnreadCount: () =>
      request<{ unreadCount: number }>("/users/me/notifications/unread-count", { method: "GET" }),

    markAsRead: (id: string) =>
      request<Notification>(`/notifications/${id}/read`, { method: "PATCH" }),

    archive: (id: string) =>
      request<Notification>(`/notifications/${id}/archive`, { method: "PATCH" }),

    markAllAsRead: () =>
      request<{ markedRead: number; status: string }>("/users/me/notifications/read-all", { method: "POST" }),

    getPreferences: () =>
      request<NotificationPreference>("/users/me/notification-preferences", { method: "GET" }),

    updatePreferences: (payload: Partial<NotificationPreference>) =>
      request<NotificationPreference>("/users/me/notification-preferences", {
        method: "PUT",
        body: JSON.stringify(payload),
      }),

    getActivity: (limit = 20) =>
      request<ActivityItem[]>(`/users/me/activity?limit=${limit}`, { method: "GET" }),

    triggerMonitoring: () =>
      request<Record<string, unknown>>("/monitoring/trigger", { method: "POST" }),
  },

  agents: {
    list: () =>
      request<AgentMetadata[]>("/agents", { method: "GET" }),

    get: (agentId: string) =>
      request<AgentMetadata>(`/agents/${agentId}`, { method: "GET" }),

    startRun: (agentId: string, payload: { goal: string; parameters?: Record<string, unknown> }) =>
      request<AgentRun>(`/agents/${agentId}/runs`, {
        method: "POST",
        body: JSON.stringify(payload),
      }),

    getUserRuns: (page = 0, size = 20) =>
      request<PageResponse<AgentRun>>(`/users/me/agent-runs?page=${page}&size=${size}`, { method: "GET" }),

    getRun: (runId: string) =>
      request<AgentRun>(`/agent-runs/${runId}`, { method: "GET" }),

    cancelRun: (runId: string) =>
      request<AgentRun>(`/agent-runs/${runId}/cancel`, { method: "POST" }),

    getRunSteps: (runId: string) =>
      request<AgentStep[]>(`/agent-runs/${runId}/steps`, { method: "GET" }),

    getApprovals: (status?: string) =>
      request<AgentApproval[]>(`/users/me/agent-approvals${status && status !== "ALL" ? `?status=${status}` : ""}`, { method: "GET" }),

    approve: (approvalId: string, reason?: string) =>
      request<AgentApproval>(`/agent-approvals/${approvalId}/approve`, {
        method: "POST",
        body: JSON.stringify({ reason }),
      }),

    reject: (approvalId: string, reason?: string) =>
      request<AgentApproval>(`/agent-approvals/${approvalId}/reject`, {
        method: "POST",
        body: JSON.stringify({ reason }),
      }),
  },

  ai: {
    getStatus: () =>
      request<Record<string, unknown>>("/ai/status", { method: "GET" }),
  },
};

export { ApiError };
