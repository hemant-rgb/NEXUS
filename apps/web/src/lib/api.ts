import { ApiResponse, Skill, User, UserPreferences, UserProfile, UserSkill } from "./types";

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
};

export { ApiError };
