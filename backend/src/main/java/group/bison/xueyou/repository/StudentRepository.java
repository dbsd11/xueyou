package group.bison.xueyou.repository;

import group.bison.xueyou.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 学生信息存储库接口，用于管理学生实体的数据操作。
 * <p>
 * 注意事项：
 * 1. 所有方法均可能抛出 DataAccessException 或其子类异常，请在调用时进行适当的异常处理。
 * 2. 查询结果可能为空，请在调用后检查 Optional 或 List 是否为空。
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    /**
     * 根据学生姓名查找学生信息。
     * 如果存在多个同名学生，则返回所有匹配的学生列表。
     *
     * @param name 学生姓名
     * @return 匹配的学生列表
     */
    List<Student> findByName(String name);

    /**
     * 根据学生 ID 查找学生信息。
     * 如果未找到对应的学生，则返回 Optional.empty()。
     *
     * @param id 学生 ID
     * @return 包含学生信息的 Optional 对象
     */
    Optional<Student> findById(Long id);

    /**
     * 自定义查询：根据学生的年龄范围查找学生信息。
     *
     * @param minAge 最小年龄
     * @param maxAge 最大年龄
     * @return 符合条件的学生列表
     */
    @Query("SELECT s FROM Student s WHERE s.age BETWEEN :minAge AND :maxAge")
    List<Student> findByAgeRange(@Param("minAge") int minAge, @Param("maxAge") int maxAge);

}
