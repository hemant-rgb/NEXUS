package com.nexus.matching.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "nexus.matching")
public class MatchingProperties {

    private String algorithmVersion = "v1.0";
    private Weights weights = new Weights();

    public static class Weights {
        private double skills = 30.0;
        private double eligibility = 25.0;
        private double interest = 15.0;
        private double experience = 15.0;
        private double deadline = 10.0;
        private double location = 5.0;

        public double getSkills() { return skills; }
        public void setSkills(double skills) { this.skills = skills; }

        public double getEligibility() { return eligibility; }
        public void setEligibility(double eligibility) { this.eligibility = eligibility; }

        public double getInterest() { return interest; }
        public void setInterest(double interest) { this.interest = interest; }

        public double getExperience() { return experience; }
        public void setExperience(double experience) { this.experience = experience; }

        public double getDeadline() { return deadline; }
        public void setDeadline(double deadline) { this.deadline = deadline; }

        public double getLocation() { return location; }
        public void setLocation(double location) { this.location = location; }

        public double getTotalWeight() {
            return skills + eligibility + interest + experience + deadline + location;
        }
    }

    public String getAlgorithmVersion() { return algorithmVersion; }
    public void setAlgorithmVersion(String algorithmVersion) { this.algorithmVersion = algorithmVersion; }

    public Weights getWeights() { return weights; }
    public void setWeights(Weights weights) { this.weights = weights; }
}
