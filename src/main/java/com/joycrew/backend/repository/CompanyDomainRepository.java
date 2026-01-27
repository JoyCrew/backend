package com.joycrew.backend.repository;

import java.util.Optional;

import com.joycrew.backend.entity.CompanyDomain;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface CompanyDomainRepository extends JpaRepository<com.joycrew.backend.entity.CompanyDomain, Long> {

    @Query("""
    select cd.company.companyId
    from CompanyDomain cd
    where cd.domain = :domain
  """)
    Optional<Long> findCompanyIdByDomain(@Param("domain") String domain);
    Optional<CompanyDomain> findFirstByCompanyCompanyIdAndPrimaryDomainTrueOrderByIdDesc(Long companyId);
}
