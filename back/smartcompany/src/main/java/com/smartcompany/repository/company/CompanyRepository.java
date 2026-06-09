package com.smartcompany.repository.company;

import com.smartcompany.model.company.Company;
import com.smartcompany.model.company.CompanyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long>,
        JpaSpecificationExecutor<Company> {

    List<Company> findByOwnerUsernameAndStatus(String username, CompanyStatus status);
}