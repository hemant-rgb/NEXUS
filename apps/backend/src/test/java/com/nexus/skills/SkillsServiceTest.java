package com.nexus.skills;

import com.nexus.common.exception.DuplicateResourceException;
import com.nexus.skills.dto.CreateUserSkillRequest;
import com.nexus.skills.dto.SkillResponse;
import com.nexus.skills.dto.UserSkillResponse;
import com.nexus.skills.entity.Skill;
import com.nexus.skills.entity.UserSkill;
import com.nexus.skills.repository.SkillRepository;
import com.nexus.skills.repository.UserSkillRepository;
import com.nexus.skills.service.SkillsService;
import com.nexus.user.entity.User;
import com.nexus.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillsServiceTest {

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private UserSkillRepository userSkillRepository;

    @Mock
    private UserRepository userRepository;

    private SkillsService skillsService;

    @BeforeEach
    void setUp() {
        skillsService = new SkillsService(skillRepository, userSkillRepository, userRepository);
    }

    @Test
    @DisplayName("Should search catalog by query")
    void testSearchCatalog() {
        Skill skill = new Skill("TypeScript", "Programming Languages");
        skill.setId(UUID.randomUUID());
        when(skillRepository.findByNameContainingIgnoreCaseOrderByCategoryAscNameAsc("type"))
                .thenReturn(List.of(skill));

        List<SkillResponse> results = skillsService.searchCatalog("type");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).name()).isEqualTo("TypeScript");
    }

    @Test
    @DisplayName("Should add new skill for user")
    void testAddUserSkillSuccess() {
        UUID userId = UUID.randomUUID();
        User user = new User("dev@nexus.io", "hash", "Dev");
        user.setId(userId);

        UUID skillId = UUID.randomUUID();
        Skill skill = new Skill("Java", "Programming Languages");
        skill.setId(skillId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(skillRepository.findById(skillId)).thenReturn(Optional.of(skill));
        when(userSkillRepository.existsByUserIdAndSkillId(userId, skillId)).thenReturn(false);

        UserSkill saved = new UserSkill(user, skill, "Advanced", BigDecimal.valueOf(3.5));
        saved.setId(UUID.randomUUID());
        when(userSkillRepository.save(any(UserSkill.class))).thenReturn(saved);

        CreateUserSkillRequest request = new CreateUserSkillRequest(skillId, null, "Advanced", BigDecimal.valueOf(3.5));
        UserSkillResponse response = skillsService.addUserSkill(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.skillName()).isEqualTo("Java");
        assertThat(response.proficiency()).isEqualTo("Advanced");
        assertThat(response.yearsExperience()).isEqualByComparingTo(BigDecimal.valueOf(3.5));
    }

    @Test
    @DisplayName("Should reject duplicate skill addition for the same user")
    void testAddDuplicateSkill() {
        UUID userId = UUID.randomUUID();
        User user = new User("dev@nexus.io", "hash", "Dev");
        user.setId(userId);

        UUID skillId = UUID.randomUUID();
        Skill skill = new Skill("Rust", "Programming Languages");
        skill.setId(skillId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(skillRepository.findById(skillId)).thenReturn(Optional.of(skill));
        when(userSkillRepository.existsByUserIdAndSkillId(userId, skillId)).thenReturn(true);

        CreateUserSkillRequest request = new CreateUserSkillRequest(skillId, null, "Beginner", BigDecimal.valueOf(1.0));

        assertThatThrownBy(() -> skillsService.addUserSkill(userId, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already in your profile");

        verify(userSkillRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete skill owned by user")
    void testDeleteUserSkill() {
        UUID userId = UUID.randomUUID();
        UUID userSkillId = UUID.randomUUID();

        User user = new User("dev@nexus.io", "hash", "Dev");
        Skill skill = new Skill("Python", "Programming Languages");
        UserSkill userSkill = new UserSkill(user, skill, "Intermediate", BigDecimal.valueOf(2.0));
        userSkill.setId(userSkillId);

        when(userSkillRepository.findByIdAndUserId(userSkillId, userId)).thenReturn(Optional.of(userSkill));

        skillsService.deleteUserSkill(userId, userSkillId);

        verify(userSkillRepository).delete(userSkill);
    }
}
