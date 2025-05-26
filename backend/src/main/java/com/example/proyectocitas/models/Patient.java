package com.example.proyectocitas.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "patients")
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    private String medicalHistory;
    private String allergies;
    private String contactInformation;
    
    public Long getId() {
        return this.id;
    }
    
    public User getUser() {
        return this.user;
    }
    
    public String getMedicalHistory() {
        return this.medicalHistory;
    }
    
    public String getAllergies() {
        return this.allergies;
    }
    
    public String getContactInformation() {
        return this.contactInformation;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }
    
    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }
    
    public void setContactInformation(String contactInformation) {
        this.contactInformation = contactInformation;
    }
}
