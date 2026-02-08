package com.dinethdf.ClinicManagementSystem.Service.Doctor;

import com.dinethdf.ClinicManagementSystem.Model.Doctor.Specialization;
import java.util.List;

public interface SpecializationService {
    List<Specialization> getAllSpecializations();
    List<Specialization> searchSpecializations(String query);
    Specialization createSpecialization(Specialization specialization);
    Specialization updateSpecialization(Integer id, Specialization specialization);
    void deleteSpecialization(Integer id);
}