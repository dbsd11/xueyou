package group.bison.xueyou.controller;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.fasterxml.jackson.core.type.TypeReference;
import group.bison.xueyou.common.response.ResponseResult;
import group.bison.xueyou.entity.Student;
import group.bison.xueyou.entity.StudyPlan;
import group.bison.xueyou.interceptor.AuthInterceptor;
import group.bison.xueyou.service.StudentService;
import group.bison.xueyou.service.StudyPlanService;
import group.bison.xueyou.utils.ObjectMapperUtil;
import group.bison.xueyou.utils.PhoneUtil;
import io.reactivex.Flowable;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/studyplans")
public class StudyPlanController {
    private static final Logger logger = LoggerFactory.getLogger(StudyPlanController.class);

    @Autowired
    private StudyPlanService studyPlanService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${openai.apiKey}")
    String apiKey;

    @Value("${openai.apiBase}")
    String apiBase;

    @Value("${openai.reasoningModel}")
    String reasoningModel;

    private static final String CREATE_STUDY_PLAN_SYSTEM_PROMPT = "你是一名学习任务规划者，根据已存在的学习计划，理解输入的学习计划, 判断是添加(add)、修改(update)还是删除(delete)，解析成固定的json格式进行输出。格式：\n " +
            "```json\n[{\"id\": xxx, \"weekday\": xxx, \"eventStartTime\":xxx, \"duration\":xxx, \"eventContent\":xxx, \"type\":\"xxx\"}]``` \n " +
            "*案例1：\n " +
            "    输入：周一至周二上午10点看书1个小时, 停止周三上午8点30的晨跑 \n " +
            "    输出：```json\n [{\"weekday\":1, \"eventStartTime\":\"10:00\", \"duration\":60, \"eventContent\":\"看书\", \"type\":\"add\"}, {\"weekday\":2, \"eventStartTime\":\"10:00\", \"duration\":60, \"eventContent\":\"看书\", \"type\":\"add\"}, {\"id\": xxx, \"weekday\":3, \"eventStartTime\":\"08:30\", \"eventContent\":\"晨跑\", \"type\":\"delete\"}]``` \n " +
            "*限制：\n " +
            "   - 不要偷懒，尽量准确的理解学习计划； 需要严格正确生成指定的json格式，避免解析报错。\n " +
            "   - 给出解析结果的对应解释 \n ";

    private static final String SUGGEST_STUDY_PLAN_SYSTEM_PROMPT = "你是一名学习任务规划者，根据已存在的学习计划, 和学生的专业信息, 给出学生不少于2条的学习计划建议。按照固定的json格式进行输出。格式：\n " +
            "```json\n [xxxx, xxxx]``` \n " +
            "*案例1：\n " +
            "   输出：```json\n [\"周三下午5点看书到6点\", \"删除星期天的学习计划，放松一下\", \"修改周四晚上的跑步，改到周五早上晨跑\"]``` \n " +
            "*限制：\n " +
            "   - 不要偷懒，尽量准确的理解学习计划； 需要严格正确生成指定的json格式，避免解析报错。\n " +
            "   - 给出解析结果的对应解释 \n ";

    @PostMapping
    public ResponseEntity createPlan(@RequestAttribute(AuthInterceptor.ATTRIBUTE_PHONE) String phone, @RequestBody StudyPlan studyPlan) throws Exception {
        String createBy = PhoneUtil.encrypt(phone);
        List<StudyPlan> studyPlanList = studyPlanService.findByTimeRange(createBy, ObjectUtils.defaultIfNull(studyPlan.getStartTime(), LocalDateTime.now().plusYears(-1)), ObjectUtils.defaultIfNull(studyPlan.getEndTime(), LocalDateTime.now().plusYears(1)));

        Generation gen = new Generation();
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content(CREATE_STUDY_PLAN_SYSTEM_PROMPT)
                .build();
        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content("解析学习计划: " + studyPlan.getEventContent())
                .build();
        GenerationParam param = GenerationParam.builder()
                // 若没有配置环境变量，请用百炼API Key将下行替换为：.apiKey("sk-xxx")
                .apiKey(apiKey)
                .model(reasoningModel)
                .prompt("已经存在的学习计划: " + ObjectMapperUtil.serialize(studyPlanList))
                .messages(Arrays.asList(systemMsg, userMsg))
                // 不可以设置为"text"
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();
        Flowable<GenerationResult> result = gen.streamCall(param);
        StringBuilder parsedStudyPlanResultSB = new StringBuilder();
        result.blockingForEach(message -> parsedStudyPlanResultSB.append(message.getOutput().getChoices().get(0).getMessage().getContent()));
        String parsedStudyPlanResult = parsedStudyPlanResultSB.toString();
        logger.info("解析结果 {}", parsedStudyPlanResult);

        String parsedStudyPlanJSON = parsedStudyPlanResult.split("```")[1].trim();
        if (parsedStudyPlanJSON.startsWith("json")) {
            parsedStudyPlanJSON = parsedStudyPlanJSON.substring("json".length()).trim();
        }
        List<StudyPlan> parsedStudyPlanList = ObjectMapperUtil.deserialize(parsedStudyPlanJSON, new TypeReference<List<StudyPlan>>() {
        });

        if (CollectionUtils.isEmpty(parsedStudyPlanList)) {
            return ResponseEntity.ok(new ResponseResult());
        }

        for (StudyPlan parsedStudyPlan : parsedStudyPlanList) {
            if (StringUtils.equalsIgnoreCase(parsedStudyPlan.getType(), "add")) {
                parsedStudyPlan.setCreateTime(LocalDateTime.now());
                parsedStudyPlan.setCreateBy(createBy);
                if (parsedStudyPlan.getDuration() == null) {
                    parsedStudyPlan.setDuration(1);
                }
                studyPlanService.save(parsedStudyPlan);
            } else if (StringUtils.equalsIgnoreCase(parsedStudyPlan.getType(), "update") && parsedStudyPlan.getId() != null) {
                parsedStudyPlan.setCreateBy(createBy);
                studyPlanService.save(parsedStudyPlan);
            } else if (StringUtils.equalsIgnoreCase(parsedStudyPlan.getType(), "delete") && parsedStudyPlan.getId() != null) {
                studyPlanService.delete(parsedStudyPlan.getId());
            }

        }

        CompletableFuture.runAsync(() -> {
            try {
                getStudySuggestion(phone, studyPlan.getStartTime(), studyPlan.getEndTime(), false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return ResponseEntity.ok(new ResponseResult<>());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseResult> updatePlan(@PathVariable Long id, @RequestBody StudyPlan studyPlan) {
        studyPlan.setId(id);
        StudyPlan updatedPlan = studyPlanService.update(studyPlan);
        return ResponseEntity.ok(new ResponseResult(updatedPlan));
    }

    @GetMapping
    public ResponseEntity<ResponseResult> getPlansByTimeRange(
            @RequestAttribute(AuthInterceptor.ATTRIBUTE_PHONE) String phone,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime) {
        String createBy = PhoneUtil.encrypt(phone);
        if (startTime == null) {
            startTime = LocalDateTime.now().plusYears(-1);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now().plusYears(1);
        }
        List<StudyPlan> plans = studyPlanService.findByTimeRange(createBy, startTime, endTime);
        return ResponseEntity.ok(new ResponseResult<>(plans));
    }

    @GetMapping("/suggestion")
    public ResponseEntity getStudySuggestion(@RequestAttribute(AuthInterceptor.ATTRIBUTE_PHONE) String phone,
                                             @RequestParam(required = false) LocalDateTime startTime,
                                             @RequestParam(required = false) LocalDateTime endTime,
                                             @RequestParam(required = false, defaultValue = "true") Boolean useCache) throws Exception {
        String createBy = PhoneUtil.encrypt(phone);

        String cacheKey = "suggestion_study#" + createBy + (startTime != null ? startTime.toInstant(ZoneOffset.ofHours(8)).getEpochSecond() : null) + (endTime != null ? endTime.toInstant(ZoneOffset.ofHours(8)).getEpochSecond() : null);
        String suggestionStr = (String) redisTemplate.opsForValue().get(cacheKey);
        if (BooleanUtils.isTrue(useCache) && StringUtils.isNotEmpty(suggestionStr)) {
            List<String> studyPlanSuggestionList = ObjectMapperUtil.deserialize(suggestionStr, List.class);
            return ResponseEntity.ok(new ResponseResult<>(studyPlanSuggestionList));
        }

        List<StudyPlan> studyPlanList = studyPlanService.findByTimeRange(createBy, ObjectUtils.defaultIfNull(startTime, LocalDateTime.now().plusYears(-1)), ObjectUtils.defaultIfNull(endTime, LocalDateTime.now().plusYears(1)));

        List<Student> studentList = studentService.findStudentByName(createBy);
        if (CollectionUtils.isEmpty(studentList)) {
            logger.warn("请完善信息，然后才能生成学习计划建议");
            return ResponseEntity.ok(new ResponseResult<>());
        }

        Generation gen = new Generation();
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content(SUGGEST_STUDY_PLAN_SYSTEM_PROMPT)
                .build();
        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content("请给出我的学习计划建议。我的信息:" + ObjectMapperUtil.serialize(studentList.get(0)))
                .build();
        GenerationParam param = GenerationParam.builder()
                // 若没有配置环境变量，请用百炼API Key将下行替换为：.apiKey("sk-xxx")
                .apiKey(apiKey)
                .model(reasoningModel)
                .prompt("已经存在的学习计划: " + ObjectMapperUtil.serialize(studyPlanList))
                .messages(Arrays.asList(systemMsg, userMsg))
                // 不可以设置为"text"
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();
        Flowable<GenerationResult> result = gen.streamCall(param);
        StringBuilder parsedStudyPlanResultSB = new StringBuilder();
        result.blockingForEach(message -> parsedStudyPlanResultSB.append(message.getOutput().getChoices().get(0).getMessage().getContent()));
        String parsedStudyPlanResult = parsedStudyPlanResultSB.toString();
        logger.info("解析结果 {}", parsedStudyPlanResult);

        String studyPlanSuggestionJSON = parsedStudyPlanResult.split("```")[1].trim();
        if (studyPlanSuggestionJSON.startsWith("json")) {
            studyPlanSuggestionJSON = studyPlanSuggestionJSON.substring("json".length()).trim();
        }
        List<String> studyPlanSuggestionList = ObjectMapperUtil.deserialize(studyPlanSuggestionJSON, List.class);
        if (CollectionUtils.isEmpty(studyPlanSuggestionList)) {
            studyPlanSuggestionList = Collections.emptyList();
        }

        redisTemplate.opsForValue().set(cacheKey, ObjectMapperUtil.serialize(studyPlanSuggestionList), Duration.ofMinutes(3));
        return ResponseEntity.ok(new ResponseResult<>(studyPlanSuggestionList));
    }

}