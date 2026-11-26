package com.careerpilot.backendcodebase.controller;

import com.careerpilot.backendcodebase.dtos.CodingRegisterDTO;
import com.careerpilot.backendcodebase.dtos.CodingResponseDTO;
import com.careerpilot.backendcodebase.service.CodingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Pageable;
import java.util.List;

@RestController
@RequestMapping("api/coding")

public class CodingController {

    private final CodingService codingService;
     @Autowired
    public CodingController(CodingService codingService) {
        this.codingService = codingService;
    }

    @GetMapping("/get-all-codingquestions")
    public ResponseEntity<Page<CodingResponseDTO>> getAllCodingQuestions(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(codingService.getAllCodingQuestion(PageRequest.of(page,pageSize)));
    }
    @PostMapping("/register-codingquestion")
    public ResponseEntity<CodingResponseDTO> registerCodingQuestion(@RequestBody CodingRegisterDTO dto) {
         return ResponseEntity.status(HttpStatus.CREATED).body(codingService.registerCodingQuestion(dto));
    }
}
