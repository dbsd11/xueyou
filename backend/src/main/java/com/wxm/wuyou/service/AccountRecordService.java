package com.wxm.wuyou.service;

import com.wxm.wuyou.entity.AccountRecord;
import com.wxm.wuyou.repository.AccountRecordRepository;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AccountRecordService {

    private static final Logger logger = LoggerFactory.getLogger(AccountRecordService.class);

    @Autowired
    private AccountRecordRepository accountRecordRepository;

    /**
     * 获取所有记账记录
     */
    public List<AccountRecord> getAllRecords() {
        logger.info("Fetching all account records");
        return accountRecordRepository.findAll();
    }

    /**
     * 根据ID获取记账记录
     */
    public Optional<AccountRecord> getRecordById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid record ID: " + id);
        }
        logger.info("Fetching account record with ID: {}", id);
        return accountRecordRepository.findById(id);
    }

    /**
     * 保存记账记录
     */
    @Transactional
    public AccountRecord saveRecord(AccountRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Account record cannot be null");
        }
        if (record.getCreateTime() == null) {
            record.setCreateTime(LocalDateTime.now());
        }

        logger.info("Saving new account record: {}", record);
        return accountRecordRepository.save(record);
    }

    /**
     * 更新记账记录
     */
    @Transactional
    public AccountRecord updateRecord(Long id, AccountRecord recordDetails) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid record ID: " + id);
        }
        if (recordDetails == null) {
            throw new IllegalArgumentException("Account record details cannot be null");
        }

        logger.info("Updating account record with ID: {}", id);
        AccountRecord record = findRecordById(id);

        record.setType(recordDetails.getType());
        record.setDetails(recordDetails.getDetails());
        record.setAmount(recordDetails.getAmount());
        record.setAiSuggestion(recordDetails.getAiSuggestion());

        return accountRecordRepository.save(record);
    }

    /**
     * 删除记账记录
     */
    @Transactional
    public void deleteRecord(String createBy, Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid record ID: " + id);
        }

        AccountRecord accountRecord = findRecordById(id);
        if (accountRecord == null) {
            return;
        }

        if (ObjectUtils.compare(accountRecord.getCreateBy(), createBy) != 0) {
            throw new IllegalArgumentException("Invalid record ID: " + id);
        }

        logger.info("Deleting account record with ID: {}", id);
        accountRecordRepository.deleteById(id);
    }

    /**
     * 根据时间范围查询记录
     */
    public Page<AccountRecord> getRecordsByTimeRange(String createBy, LocalDateTime startTime, LocalDateTime endTime, Integer page, Integer pageSize) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Time range cannot be null");
        }
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
        logger.info("Fetching account records between {} and {}", startTime, endTime);
        return accountRecordRepository.findByTimeRange(createBy, startTime, endTime, PageRequest.of(page, pageSize));
    }

    /**
     * 计算指定时间范围内的月均消费额
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 月均消费额
     */
    public BigDecimal getAverageMonthlyExpense(String createBy, LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Time range cannot be null");
        }
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        logger.info("Calculating average monthly expense between {} and {}", startTime, endTime);
        
        // 获取支出类型的记录
        BigDecimal expenses = accountRecordRepository.getMonthlyAverageSpend(createBy, startTime, endTime);

        // 计算月均消费额，四舍五入保留2位小数
        return expenses == null ? BigDecimal.ZERO : expenses.setScale(2, RoundingMode.HALF_UP);
    }

    // 私有方法：根据ID查找记录，若不存在则抛出异常
    private AccountRecord findRecordById(Long id) {
        return accountRecordRepository.findById(id)
                .orElse(null);
    }

    /**
     * 获取未来时间的记账记录
     */
    public List<AccountRecord> getFutureRecords(String createBy) {
        logger.info("Fetching future account records");
        return accountRecordRepository.findFutureRecords(createBy);
    }
}

// 自定义异常类
class AccountRecordNotFoundException extends RuntimeException {
    public AccountRecordNotFoundException(String message) {
        super(message);
    }
}