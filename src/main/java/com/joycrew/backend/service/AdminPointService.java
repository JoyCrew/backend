package com.joycrew.backend.service;

import com.joycrew.backend.dto.AdminPointDistributionRequest;
import com.joycrew.backend.dto.PointDistributionDetail;
import com.joycrew.backend.entity.Company;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.RewardPointTransaction;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.entity.enums.TransactionType;
import com.joycrew.backend.exception.UserNotFoundException;
import com.joycrew.backend.repository.CompanyRepository;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.RewardPointTransactionRepository;
import com.joycrew.backend.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminPointService {

  private final EmployeeRepository employeeRepository;
  private final WalletRepository walletRepository;
  private final RewardPointTransactionRepository transactionRepository;
  private final CompanyRepository companyRepository;

  public void distributePoints(AdminPointDistributionRequest request, Employee admin) {
    // Admin 다시 조회 (Company까지 join fetch)
    Employee managedAdmin = employeeRepository.findByIdWithCompany(admin.getEmployeeId())
            .orElseThrow(() -> new UserNotFoundException("Admin not found"));

    Company company = managedAdmin.getCompany();

    // 총 변화량 계산
    int netPointsChange = request.distributions().stream()
            .mapToInt(PointDistributionDetail::points)
            .sum();

    // 회사 예산 반영
    if (netPointsChange > 0) {
      company.spendBudget(netPointsChange);
    } else if (netPointsChange < 0) {
      company.addBudget(Math.abs(netPointsChange));
    }
    companyRepository.save(company);

    // 지급 대상 직원 ID 목록
    List<Long> employeeIds = request.distributions().stream()
            .map(PointDistributionDetail::employeeId)
            .toList();

    // 직원 목록 조회
    Map<Long, Employee> employeeMap = employeeRepository.findAllById(employeeIds).stream()
            .collect(Collectors.toMap(Employee::getEmployeeId, Function.identity()));

    // 일부 직원이 없으면 예외
    if (employeeMap.size() != employeeIds.size()) {
      throw new UserNotFoundException("Could not find some of the requested employees. Please verify the IDs.");
    }

    // 각 직원별 지급/차감 실행
    for (PointDistributionDetail detail : request.distributions()) {
      Employee employee = employeeMap.get(detail.employeeId());
      Wallet wallet = walletRepository.findByEmployee_EmployeeId(employee.getEmployeeId())
              .orElseThrow(() -> new IllegalStateException("Wallet not found for employee: " + employee.getEmployeeName()));

      int pointsToProcess = detail.points();

      if (pointsToProcess > 0) {
        wallet.addPoints(pointsToProcess);
      } else if (pointsToProcess < 0) {
        wallet.revokePoints(Math.abs(pointsToProcess));
      }

      // 트랜잭션 기록 저장
      if (pointsToProcess != 0) {
        RewardPointTransaction transaction = RewardPointTransaction.builder()
                .sender(managedAdmin)
                .receiver(employee)
                .pointAmount(pointsToProcess)
                .message(request.message())
                .type(TransactionType.AWARD_MANAGER_SPOT)
                .build();
        transactionRepository.save(transaction);
      }
    }
  }
}
