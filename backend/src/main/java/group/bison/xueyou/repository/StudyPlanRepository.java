package group.bison.xueyou.repository;

import group.bison.xueyou.entity.StudyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {
    // 查询指定时间范围内的所有学习计划
    @Query("SELECT sp FROM StudyPlan sp WHERE sp.createBy = :createBy AND sp.createTime >= :startTime AND sp.createTime <= :endTime")
    List<StudyPlan> findByCreateByAndTimeRange(@Param("createBy") String createBy, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}