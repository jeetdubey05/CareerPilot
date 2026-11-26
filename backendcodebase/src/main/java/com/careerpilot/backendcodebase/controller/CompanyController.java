package com.careerpilot.backendcodebase.controller;

import com.careerpilot.backendcodebase.dtos.CompanyResponseDTO;
import com.careerpilot.backendcodebase.entity.Company;
import com.careerpilot.backendcodebase.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/company")
public class CompanyController {
    @Autowired
    private CompanyService companyService;

    @PostMapping("/addcompany")
    public ResponseEntity<Company> addCompany(@RequestBody Company company) {
        Company company1=companyService.addCompany(company);
        return  ResponseEntity.status(HttpStatus.CREATED).body(company1);
    }
    @GetMapping("/getallcompany")
    public ResponseEntity<Page<CompanyResponseDTO>> getAllCompany(@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.status(HttpStatus.OK).body(companyService.getCompany(PageRequest.of(page,size)));

    }

}
