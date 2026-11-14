package com.nexus.skills.entity;

import com.nexus.user.entity.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "user_skills",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_skill", columnNames = {"user_id", "skill_id"})
    }
)
public class UserSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(nullable = false, length = 50)
    private String proficiency; // Beginner, Intermediate, Advanced

    @Column(name = "years_experience", nullable = false, precision = 4, scale = 1)
    private BigDecimal yearsExperience = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public UserSkill() {
    }

    public UserSkill(User user, Skill skill, String proficiency, BigDecimal yearsExperience) {
        this.user = user;
        this.skill = skill;
        this.proficiency = proficiency;
        this.yearsExperience = yearsExperience != null ? yearsExperience : BigDecimal.ZERO;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    public String getProficiency() {
        return proficiency;
    }

    public void setProficiency(String proficiency) {
        this.proficiency = proficiency;
    }

    public BigDecimal getYearsExperience() {
        return yearsExperience;
    }

    public void setYearsExperience(BigDecimal yearsExperience) {
        this.yearsExperience = yearsExperience;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
