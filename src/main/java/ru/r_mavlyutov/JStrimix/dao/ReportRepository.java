package ru.r_mavlyutov.JStrimix.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.r_mavlyutov.JStrimix.entity.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {
}

