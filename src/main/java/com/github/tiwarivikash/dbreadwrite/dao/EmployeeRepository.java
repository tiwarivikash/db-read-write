package com.github.tiwarivikash.dbreadwrite.dao;

import com.github.tiwarivikash.dbreadwrite.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;


@Transactional(readOnly = true)
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

}
