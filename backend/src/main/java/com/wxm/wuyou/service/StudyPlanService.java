package com.wxm.wuyou.service;

import com.wxm.wuyou.entity.StudyPlan;
import com.wxm.wuyou.repository.StudyPlanRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StudyPlanService {

    @Autowired
    private StudyPlanRepository studyPlanRepository;

    @Transactional(readOnly = true)
    public List<StudyPlan> findByTimeRange(String createBy, LocalDateTime startTime, LocalDateTime endTime) {
        // 参数验证
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("开始时间和结束时间不能为空");
        }
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("开始时间不能晚于结束时间");
        }

        return studyPlanRepository.findByCreateByAndTimeRange(createBy, startTime, endTime);
    }

    @Transactional
    public StudyPlan save(StudyPlan studyPlan) {
        if (studyPlan == null) {
            throw new IllegalArgumentException("学习计划不能为空");
        }
        if (studyPlan.getCreateTime() == null) {
            studyPlan.setCreateTime(LocalDateTime.now());
        }
        return studyPlanRepository.save(studyPlan);
    }

    @Transactional
    public StudyPlan update(StudyPlan studyPlan) {
        if (studyPlan == null || studyPlan.getId() == null) {
            throw new IllegalArgumentException("学习计划及ID不能为空");
        }
        StudyPlan existing = studyPlanRepository.findById(studyPlan.getId())
            .orElseThrow(() -> new IllegalArgumentException("未找到该学习计划"));
        existing.setWeekday(studyPlan.getWeekday());
        existing.setEventContent(studyPlan.getEventContent());
        existing.setEventStartTime(studyPlan.getEventStartTime());
        existing.setDuration(studyPlan.getDuration());
        return studyPlanRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("无效的ID");
        }
        studyPlanRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public StudyPlan findById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("无效的ID");
        }
        return studyPlanRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("未找到该学习计划"));
    }
}