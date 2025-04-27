package group.bison.xueyou.repository;

import group.bison.xueyou.entity.AccountRecord;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 记账记录存储库接口，用于管理记账记录的数据操作。
 * <p>
 * 注意事项：
 * 1. 所有方法均可能抛出 DataAccessException 或其子类异常，请在调用时进行适当的异常处理。
 * 2. 查询结果可能为空，请在调用后检查 List 是否为空。
 */
@Repository
public interface AccountRecordRepository extends JpaRepository<AccountRecord, Long> {

    /**
     * 根据创建时间范围查找记账记录。
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 指定时间范围内的记账记录列表
     */
    @Query("SELECT a FROM AccountRecord a WHERE a.createBy=:createBy AND a.createTime BETWEEN :startTime AND :endTime ORDER BY a.createTime DESC")
    Page<AccountRecord> findByTimeRange(@Param("createBy") String createBy, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime, Pageable pageable);

    /**
     * 根据记账类型和时间范围查找记账记录。
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 符合条件的记账记录列表
     */
    @Query(value = "SELECT AVG(monthly_spend) " +
            "FROM (" +
            "    SELECT SUM(COALESCE(a.amount, 0)) as monthly_spend " +
            "    FROM account_records a " +
            "    WHERE a.create_by = :createBy AND a.type = 'EXPENSE' AND a.create_time BETWEEN :startTime AND :endTime " +
            "    GROUP BY DATE_FORMAT(a.create_time, '%Y-%m') " +
            ") as monthly_spends", nativeQuery = true)
    BigDecimal getMonthlyAverageSpend(
            @Param("createBy") String createBy,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 查询创建时间大于当前时间的记账记录。
     *
     * @return 未来时间的记账记录列表
     */
    @Query("SELECT a FROM AccountRecord a WHERE a.createBy=:createBy AND a.createTime > CURRENT_TIMESTAMP ORDER BY a.createTime ASC")
    List<AccountRecord> findFutureRecords(@Param("createBy") String createBy);
}