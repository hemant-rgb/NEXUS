package com.nexus.skills.service;

import com.nexus.common.exception.DuplicateResourceException;
import com.nexus.common.exception.ResourceNotFoundException;
import com.nexus.skills.dto.CreateUserSkillRequest;
import com.nexus.skills.dto.SkillResponse;
import com.nexus.skills.dto.UpdateUserSkillRequest;
import com.nexus.skills.dto.UserSkillResponse;
import com.nexus.skills.entity.Skill;
import com.nexus.skills.entity.UserSkill;
import com.nexus.skills.repository.SkillRepository;
import com.nexus.skills.repository.UserSkillRepository;
import com.nexus.user.entity.User;
import com.nexus.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SkillsService {

    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserRepository userRepository;

    public SkillsService(
            SkillRepository skillRepository,
            UserSkillRepository userSkillRepository,
            UserRepository userRepository) {
        this.skillRepository = skillRepository;
        this.userSkillRepository = userSkillRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<SkillResponse> searchCatalog(String query) {
        List<Skill> skills;
        if (query != null && !query.trim().isEmpty()) {
            skills = skillRepository.findByNameContainingIgnoreCaseOrderByCategoryAscNameAsc(query.trim());
        } else {
            skills = skillRepository.findAllByOrderByCategoryAscNameAsc();
        }
        return skills.stream()
                .map(s -> new SkillResponse(s.getId(), s.getName(), s.getCategory()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserSkillResponse> getUserSkills(UUID userId) {
        return userSkillRepository.findByUserIdWithSkill(userId).stream()
                .map(this::mapToUserSkillResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserSkillResponse addUserSkill(UUID userId, CreateUserSkillRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Skill skill;
        if (request.skillId() != null) {
            skill = skillRepository.findById(request.skillId())
                    .orElseThrow(() -> new ResourceNotFoundException("Skill not found with ID: " + request.skillId()));
        } else if (request.skillName() != null && !request.skillName().trim().isEmpty()) {
            String rawName = request.skillName().trim();
            String normalized = rawName.toLowerCase();
            skill = skillRepository.findByNormalizedName(normalized)
                    .orElseGet(() -> skillRepository.save(new Skill(rawName, "Custom / Other")));
        } else {
            throw new IllegalArgumentException("Either skillId or skillName must be provided");
        }

        if (userSkillRepository.existsByUserIdAndSkillId(userId, skill.getId())) {
            throw new DuplicateResourceException("Skill '" + skill.getName() + "' is already in your profile");
        }

        BigDecimal experience = request.yearsExperience() != null ? request.yearsExperience() : BigDecimal.ZERO;
        UserSkill userSkill = new UserSkill(user, skill, request.proficiency(), experience);
        UserSkill saved = userSkillRepository.save(userSkill);

        return mapToUserSkillResponse(saved);
    }

    @Transactional
    public UserSkillResponse updateUserSkill(UUID userId, UUID userSkillId, UpdateUserSkillRequest request) {
        UserSkill userSkill = userSkillRepository.findByIdAndUserId(userSkillId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User skill not found"));

        if (request.proficiency() != null) {
            userSkill.setProficiency(request.proficiency());
        }
        if (request.yearsExperience() != null) {
            userSkill.setYearsExperience(request.yearsExperience());
        }

        UserSkill updated = userSkillRepository.save(userSkill);
        return mapToUserSkillResponse(updated);
    }

    @Transactional
    public void deleteUserSkill(UUID userId, UUID userSkillId) {
        UserSkill userSkill = userSkillRepository.findByIdAndUserId(userSkillId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User skill not found"));
        userSkillRepository.delete(userSkill);
    }

    private UserSkillResponse mapToUserSkillResponse(UserSkill userSkill) {
        return new UserSkillResponse(
                userSkill.getId(),
                userSkill.getSkill().getId(),
                userSkill.getSkill().getName(),
                userSkill.getSkill().getCategory(),
                userSkill.getProficiency(),
                userSkill.getYearsExperience(),
                userSkill.getCreatedAt()
        );
    }
}
