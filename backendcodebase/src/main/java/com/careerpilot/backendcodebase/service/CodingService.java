package com.careerpilot.backendcodebase.service;

import com.careerpilot.backendcodebase.dtos.CodingRegisterDTO;
import com.careerpilot.backendcodebase.dtos.CodingResponseDTO;
import com.careerpilot.backendcodebase.entity.CodingQuestion;
import com.careerpilot.backendcodebase.repository.CodingRepository;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CodingService {
    private final CodingRepository codingRepository;

    public CodingService(CodingRepository codingRepository) {
        this.codingRepository = codingRepository;
    }

    public CodingResponseDTO registerCodingQuestion(CodingRegisterDTO codingRegisterDTO) {

       CodingQuestion codingQuestion=mapToEntity(codingRegisterDTO);
       CodingQuestion savedCodingQuestion= codingRepository.save(codingQuestion);
       return mapToDTO(savedCodingQuestion);


    }

    public Page<CodingResponseDTO> getAllCodingQuestion(Pageable pageable) {
        Page<CodingQuestion> coding=codingRepository.findAll(pageable);
        return coding.map(this::mapToDTO);


    }

    private CodingResponseDTO mapToDTO(CodingQuestion codingQuestion) {
        CodingResponseDTO codingResponseDTO = new CodingResponseDTO();
        codingResponseDTO.setId(codingQuestion.getId());
        codingResponseDTO.setQuestionName(codingQuestion.getQuestionName());
        codingResponseDTO.setDescription(codingQuestion.getDescription());
        codingResponseDTO.setNumber(codingQuestion.getNumber());
        codingResponseDTO.setDifficultyLevel(codingQuestion.getDifficultyLevel());
        codingResponseDTO.setUrl(codingQuestion.getUrl());
        return  codingResponseDTO;
    }
    private CodingQuestion mapToEntity(CodingRegisterDTO codingRegisterDTO) {
        CodingQuestion codingQuestion=  new CodingQuestion();
        codingQuestion.setQuestionName(codingRegisterDTO.getQuestionName());
        codingQuestion.setNumber(codingRegisterDTO.getNumber());
        codingQuestion.setDescription(codingRegisterDTO.getDescription());
        codingQuestion.setDifficultyLevel(codingRegisterDTO.getDifficultyLevel());
        codingQuestion.setUrl(codingRegisterDTO.getUrl());
        return  codingQuestion;
    }
}
