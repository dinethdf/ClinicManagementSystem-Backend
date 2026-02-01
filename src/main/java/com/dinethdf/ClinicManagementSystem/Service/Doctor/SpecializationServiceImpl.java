package com.dinethdf.ClinicManagementSystem.Service.Doctor;

import com.dinethdf.ClinicManagementSystem.Model.Doctor.Specialization;
import com.dinethdf.ClinicManagementSystem.Repo.Doctor.SpecializationRepo;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SpecializationServiceImpl implements SpecializationService {

    @Autowired
    SpecializationRepo specializationRepo;

    @Autowired
    private MessageSourceUtil messageSourceUtil;

    @Override
    @Transactional(readOnly = true)
    public List<Specialization> getAllSpecializations() {
        return specializationRepo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Specialization> searchSpecializations(String query) {
        // This handles your requirement: "if I send name Gene Need ful object"
        if (query == null || query.trim().isEmpty()) {
            return specializationRepo.findAll();
        }
        return specializationRepo.findByNameContainingIgnoreCase(query.trim());
    }

    @Override
    @Transactional
    public Specialization createSpecialization(Specialization specialization) {
        // 1. Validate: Check if name already exists
        if (specializationRepo.existsByNameIgnoreCase(specialization.getName())) {
            throw new ValidationException(
                    messageSourceUtil.getMessage("specialization.exists"),
                    "DUPLICATE_NAME",
                    null,
                    HttpStatus.BAD_REQUEST
            );
        }

        try {
            return specializationRepo.save(specialization);
        } catch (Exception e) {
            throw new ValidationException(
                    messageSourceUtil.getMessage("server.error"),
                    "ERROR",
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    @Transactional
    public Specialization updateSpecialization(Integer id, Specialization specialization) {
        // 1. Check if ID exists
        Specialization existingSpec = specializationRepo.findById(id)
                .orElseThrow(() -> new ValidationException(
                        messageSourceUtil.getMessage("specialization.not.found"),
                        "NOT_FOUND",
                        null,
                        HttpStatus.NOT_FOUND
                ));

        // 2. Validate: Check if new name exists for a DIFFERENT ID
        if (specializationRepo.existsByNameIgnoreCaseAndIdNot(specialization.getName(), id)) {
            throw new ValidationException(
                    messageSourceUtil.getMessage("specialization.exists"),
                    "DUPLICATE_NAME",
                    null,
                    HttpStatus.BAD_REQUEST
            );
        }

        // 3. Update fields
        existingSpec.setName(specialization.getName());
        existingSpec.setNote(specialization.getNote());

        return specializationRepo.save(existingSpec);
    }

    @Override
    @Transactional
    public void deleteSpecialization(Integer id) {
        if (!specializationRepo.existsById(id)) {
            throw new ValidationException(
                    messageSourceUtil.getMessage("specialization.not.found"),
                    "NOT_FOUND",
                    null,
                    HttpStatus.NOT_FOUND
            );
        }

        try {
            specializationRepo.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            // This handles the case where a Doctor is already assigned this specialization
            throw new ValidationException(
                    messageSourceUtil.getMessage("specialization.in.use"),
                    "CONSTRAINT_VIOLATION",
                    null,
                    HttpStatus.CONFLICT
            );
        }
    }
}