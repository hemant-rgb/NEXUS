package com.nexus.matching.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.matching.dto.SkillGapReportResponse;
import com.nexus.matching.dto.SkillItemResponse;
import com.nexus.opportunity.entity.Opportunity;
import com.nexus.skills.entity.UserSkill;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SkillGapAnalyzer {

    private final ObjectMapper objectMapper;

    public SkillGapAnalyzer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SkillGapReportResponse analyze(List<UserSkill> userSkills, Opportunity opportunity) {
        List<String> requiredSkills = parseJsonList(opportunity.getRequiredSkills());
        List<String> preferredSkills = parseJsonList(opportunity.getPreferredSkills());

        Map<String, UserSkill> userSkillMap = new HashMap<>();
        if (userSkills != null) {
            for (UserSkill us : userSkills) {
                if (us.getSkill() != null && us.getSkill().getName() != null) {
                    userSkillMap.put(normalizeSkill(us.getSkill().getName()), us);
                }
            }
        }

        List<SkillItemResponse> matched = new ArrayList<>();
        List<SkillItemResponse> partiallyMatched = new ArrayList<>();
        List<SkillItemResponse> missingRequired = new ArrayList<>();
        List<SkillItemResponse> missingPreferred = new ArrayList<>();

        int totalEvaluated = 0;
        double maxPossibleWeight = 0.0;
        double scoreAccumulator = 0.0;

        // Evaluate Required Skills (Weight = 1.0 each)
        for (String req : requiredSkills) {
            if (req.isBlank()) continue;
            totalEvaluated++;
            maxPossibleWeight += 1.0;
            String norm = normalizeSkill(req);

            if (userSkillMap.containsKey(norm)) {
                UserSkill us = userSkillMap.get(norm);
                String prof = us.getProficiency() != null ? us.getProficiency() : "Intermediate";
                if ("Beginner".equalsIgnoreCase(prof)) {
                    partiallyMatched.add(new SkillItemResponse(req, "PARTIALLY_MATCHED", prof, "HIGH", true));
                    scoreAccumulator += 0.6;
                } else {
                    matched.add(new SkillItemResponse(req, "MATCHED", prof, "HIGH", true));
                    scoreAccumulator += 1.0;
                }
            } else {
                // Check for substring partial match
                boolean foundPartial = false;
                for (Map.Entry<String, UserSkill> entry : userSkillMap.entrySet()) {
                    if (entry.getKey().contains(norm) || norm.contains(entry.getKey())) {
                        partiallyMatched.add(new SkillItemResponse(req, "PARTIALLY_MATCHED", entry.getValue().getProficiency(), "HIGH", true));
                        scoreAccumulator += 0.5;
                        foundPartial = true;
                        break;
                    }
                }
                if (!foundPartial) {
                    missingRequired.add(new SkillItemResponse(req, "MISSING", null, "HIGH", true));
                }
            }
        }

        // Evaluate Preferred Skills (Weight = 0.5 each)
        for (String pref : preferredSkills) {
            if (pref.isBlank()) continue;
            totalEvaluated++;
            maxPossibleWeight += 0.5;
            String norm = normalizeSkill(pref);

            if (userSkillMap.containsKey(norm)) {
                UserSkill us = userSkillMap.get(norm);
                String prof = us.getProficiency() != null ? us.getProficiency() : "Intermediate";
                matched.add(new SkillItemResponse(pref, "MATCHED", prof, "MEDIUM", false));
                scoreAccumulator += 0.5;
            } else {
                missingPreferred.add(new SkillItemResponse(pref, "MISSING", null, "LOW", false));
            }
        }

        double matchPercentage = maxPossibleWeight > 0
                ? Math.min(100.0, Math.round((scoreAccumulator / maxPossibleWeight) * 100.0 * 10.0) / 10.0)
                : 100.0; // If no skills specified, 100% compatibility

        return new SkillGapReportResponse(
                opportunity.getId(),
                opportunity.getTitle(),
                matched,
                partiallyMatched,
                missingRequired,
                missingPreferred,
                totalEvaluated,
                matchPercentage
        );
    }

    public String normalizeSkill(String skill) {
        if (skill == null) return "";
        String s = skill.toLowerCase(Locale.ROOT).trim();
        s = s.replace(".", "").replace("-", " ").replace("_", " ");
        if (s.equals("nodejs") || s.equals("node js")) return "node";
        if (s.equals("reactjs") || s.equals("react js")) return "react";
        if (s.equals("postgresql") || s.equals("postgres sql")) return "postgres";
        if (s.equals("golang")) return "go";
        if (s.equals("cpp") || s.equals("c plus plus")) return "c++";
        return s;
    }

    private List<String> parseJsonList(String json) {
        if (json == null || json.isBlank() || json.equals("[]")) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Arrays.stream(json.replace("[", "").replace("]", "").replace("\"", "").split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }
    }
}
