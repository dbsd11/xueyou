package com.wxm.wuyou.controller;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.fasterxml.jackson.core.type.TypeReference;
import com.wxm.wuyou.common.response.ResponseResult;
import com.wxm.wuyou.entity.AccountRecord;
import com.wxm.wuyou.entity.Student;
import com.wxm.wuyou.interceptor.AuthInterceptor;
import com.wxm.wuyou.service.AccountRecordService;
import com.wxm.wuyou.service.StudentService;
import com.wxm.wuyou.utils.ObjectMapperUtil;
import com.wxm.wuyou.utils.PhoneUtil;
import io.reactivex.Flowable;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 记账记录控制器
 * 提供记账相关的RESTful API接口
 */
@RestController
@RequestMapping("/api/account-records")
@Validated
public class AccountRecordController {

    private static final Logger logger = LoggerFactory.getLogger(AccountRecordController.class);

    @Autowired
    private AccountRecordService accountRecordService;

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

    public static final String PARSE_EXPENSE_FILE_SYSTEM_PROMPT = "你是一名交易明细图片解析识别工具，根据上传的交易明细截图，理解交易类型和交易明细，交易类型为：收入(INCOME)、支出(EXPENSE)、转账(TRANSFER)。解析成固定的json格式进行输出。格式：\n " +
            "```json\n[{\"createTime\": xxx, \"details\": xxx, \"amount\":xxx, \"type\":xxx}]``` \n " +
            "*案例1：\n" +
            "    输出：```json\n [{\"createTime\":\"2025-01-01 00:01:02\", \"details\":\"理发\", \"amount\":10, \"type\":\"EXPENSE\"}, {\"createTime\":\"2025-01-02 02:02:02\", \"details\":\"转账给小李20.5元\", \"amount\":20.5, \"type\":\"TRANSFER\"}]``` \n " +
            "*限制：\n " +
            "   - 不要偷懒，尽量准确的理解费用支出截图； 需要严格正确生成指定的json格式，避免解析报错。\n " +
            "   - 如果没有交易年份信息，则年份统一用1970 \n " +
            "   - 给出解析结果的对应解释 \n ";

    public static final String PARSE_SUBMIT_EXPENSE_SYSTEM_PROMPT = "你是一名交易信息解析员，根据提交的交易信息和当前年份，理解交易类型和交易明细，交易类型为：收入(INCOME)、支出(EXPENSE)、转账(TRANSFER)。解析成固定的json格式进行输出。格式：\n " +
            "```json\n[{\"createTime\": xxx, \"details\": xxx, \"amount\":xxx, \"type\":xxx}]``` \n " +
            "*案例1：\n" +
            "    输入：2025年3月25日上午10点购买了一瓶眼药水，花费5.6元, 并在11点给小张转账8元\n" +
            "    输出：```json\n [{\"createTime\":\"2025-03-25 10:00:00\", \"details\":\"购买了一瓶眼药水\", \"amount\":5.6, \"type\":\"EXPENSE\"}, {\"createTime\":\"2025-03-25 11:00:00\", \"details\":\"转账给小张8元\", \"amount\":8, \"type\":\"TRANSFER\"}]``` \n " +
            "*限制：\n " +
            "   - 不要偷懒，尽量准确的理解交易信息； 需要严格正确生成指定的json格式，避免解析报错。\n " +
            "   - 如果没有交易年份信息，则年份统一用1970 \n " +
            "   - 给出解析结果的对应解释 \n ";

    private static final String SUGGEST_ACCOUNT_RECORD_SYSTEM_PROMPT = "你是一名生活费用支出规划者，根据所设置的期望月均消费额，结合已产生的当月消费交易，对未来的消费支出给出建议, 以帮助学生达到合理开支费用的目标。按照固定的json格式进行输出。格式：\n " +
            "```json\n {\"action\":xxx, \"reason\": xxx}, {\"action\":xxx, \"reason\": xxx}``` \n " +
            "*案例1：\n " +
            "   输出：```json\n {\"action\":\"建议取消该笔不重要的支出\", \"reason\":\"非必要小额消费, 建议取消该笔支出以强化预算控制意识\"}``` \n " +
            "*限制：\n " +
            "   - 不要偷懒，尽量准确的理解消费交易； 需要严格正确生成指定的json格式，避免解析报错。\n " +
            "   - 给出解析结果的对应解释 \n ";

    /**
     * 创建新的记账记录
     */
    @PostMapping("/parse-submit")
    public ResponseEntity parseSubmitRecord(@RequestAttribute(AuthInterceptor.ATTRIBUTE_PHONE) String phone, @RequestBody AccountRecord submitAccountRecord) throws Exception {
        String createBy = PhoneUtil.encrypt(phone);
        LocalDateTime currentTime = LocalDateTime.now();

        Generation gen = new Generation();
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content(PARSE_SUBMIT_EXPENSE_SYSTEM_PROMPT)
                .build();
        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content("解析交易信息: " + submitAccountRecord.getDetails() + "\n 当前时间是: " + currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
        GenerationParam param = GenerationParam.builder()
                // 若没有配置环境变量，请用百炼API Key将下行替换为：.apiKey("sk-xxx")
                .apiKey(apiKey)
                .model(reasoningModel)
                .messages(Arrays.asList(systemMsg, userMsg))
                // 不可以设置为"text"
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();
        Flowable<GenerationResult> result = gen.streamCall(param);
        StringBuilder parsedExpenseResultSB = new StringBuilder();
        result.blockingForEach(message -> parsedExpenseResultSB.append(message.getOutput().getChoices().get(0).getMessage().getContent()));
        String parsedExpenseResult = parsedExpenseResultSB.toString();
        logger.info("解析结果 {}", parsedExpenseResult);

        String parsedExpenseJSON = parsedExpenseResult.split("```")[1].trim();
        if (parsedExpenseJSON.startsWith("json")) {
            parsedExpenseJSON = parsedExpenseJSON.substring("json".length()).trim();
        }
        List<AccountRecord> parsedAccountRecordList = ObjectMapperUtil.deserialize(parsedExpenseJSON, new TypeReference<List<AccountRecord>>() {
        });

        if (CollectionUtils.isEmpty(parsedAccountRecordList)) {
            return ResponseEntity.ok(new ResponseResult());
        }

        parsedAccountRecordList.forEach(accountRecord -> {
            LocalDateTime originalCreateTime = accountRecord.getCreateTime();
            if (originalCreateTime.getYear() == 1970) {
                LocalDateTime updatedCreateTime = originalCreateTime.withYear(currentTime.getYear());
                accountRecord.setCreateTime(updatedCreateTime);
            }
            accountRecord.setCreateBy(createBy);
            accountRecordService.saveRecord(accountRecord);
        });
        return ResponseEntity.ok(new ResponseResult<>());
    }

    /**
     * 创建新的记账记录
     */
    @PostMapping
    public ResponseEntity createRecord(@RequestAttribute(AuthInterceptor.ATTRIBUTE_PHONE) String phone, @RequestBody List<AccountRecord> accountRecordList) {
        String createBy = PhoneUtil.encrypt(phone);
        LocalDateTime currentTime = LocalDateTime.now();
        accountRecordList.forEach(accountRecord -> {
            LocalDateTime originalCreateTime = accountRecord.getCreateTime();
            if (originalCreateTime.getYear() == 1970) {
                LocalDateTime updatedCreateTime = originalCreateTime.withYear(currentTime.getYear());
                accountRecord.setCreateTime(updatedCreateTime);
            }
            accountRecord.setCreateBy(createBy);
            accountRecordService.saveRecord(accountRecord);
        });
        return ResponseEntity.ok(new ResponseResult<>());
    }

    /**
     * 删除记账记录
     */
    @PostMapping("/delete")
    public ResponseEntity deleteRecords(@RequestAttribute(AuthInterceptor.ATTRIBUTE_PHONE) String phone, @RequestBody List<Long> idList) {
        String createBy = PhoneUtil.encrypt(phone);
        idList.forEach(id -> {
            accountRecordService.deleteRecord(createBy, id);
        });
        return ResponseEntity.ok(new ResponseResult<>());
    }

    /**
     * 根据时间范围查询记录
     */
    @GetMapping("/time-range")
    public ResponseEntity getRecordsByTimeRange(
            @RequestAttribute(AuthInterceptor.ATTRIBUTE_PHONE) String phone,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        String createBy = PhoneUtil.encrypt(phone);
        Page<AccountRecord> accountRecordResponsePage = accountRecordService.getRecordsByTimeRange(createBy, startTime, endTime, page - 1, pageSize);
        return ResponseEntity.ok(new ResponseResult<>(accountRecordResponsePage));
    }

    /**
     * 计算指定时间范围内的月均消费额
     */
    @GetMapping("/average-monthly-expense")
    public ResponseEntity getAverageMonthlyExpense(
            @RequestAttribute(AuthInterceptor.ATTRIBUTE_PHONE) String phone,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        String createBy = PhoneUtil.encrypt(phone);
        BigDecimal averageMonthlyExpense = accountRecordService.getAverageMonthlyExpense(createBy, startTime, endTime);
        Map monthlyExpenseMap = new HashMap<>();
        monthlyExpenseMap.put("averageMonthlyExpense", averageMonthlyExpense);

        List<Student> studentList = studentService.findStudentByName(createBy);
        monthlyExpenseMap.put("expectMonthlySpend", studentList.get(0).getExpectMonthlySpend());

        return ResponseEntity.ok(new ResponseResult<>(monthlyExpenseMap));
    }

    /**
     * 获取未来时间的记账记录
     */
    @GetMapping("/future-records")
    public ResponseEntity getFutureRecords(@RequestAttribute(AuthInterceptor.ATTRIBUTE_PHONE) String phone) {
        String createBy = PhoneUtil.encrypt(phone);
        List<AccountRecord> accountRecordList = accountRecordService.getFutureRecords(createBy);

        accountRecordList.forEach(accountRecord -> {
            if (StringUtils.isEmpty(accountRecord.getAiSuggestion())) {
                CompletableFuture.runAsync(() -> {
                    String handleKey = "suggestion_accountrecord#" + accountRecord.getCreateBy() + accountRecord.getId();
                    try {
                        if (!redisTemplate.opsForValue().setIfAbsent(handleKey, "", 10, TimeUnit.MINUTES)) {
                            return;
                        }
                        doAiSuggestion(accountRecord);
                    } catch (Exception e) {
                        logger.error("doAiSuggestion failed", e);
                    }
                });
            } else {
                try {
                    List<Map> suggestionMapList = null;
                    if (accountRecord.getAiSuggestion().startsWith("[") && accountRecord.getAiSuggestion().endsWith("]")) {
                        suggestionMapList = ObjectMapperUtil.deserialize(accountRecord.getAiSuggestion(), new TypeReference<List<Map>>() {
                        });
                    } else {
                        Map suggestionMap = ObjectMapperUtil.deserialize(accountRecord.getAiSuggestion(), HashMap.class);
                        suggestionMapList.add(suggestionMap);
                    }
                    StringBuilder sb = new StringBuilder();
                    AtomicInteger i = new AtomicInteger(1);
                    suggestionMapList.forEach(suggestionMap -> {
                        sb.append(i.getAndIncrement());
                        sb.append(" ");
                        if (suggestionMap.containsKey("action")) {
                            sb.append(suggestionMap.get("action"));
                            sb.append("\n");
                        }
                        if (suggestionMap.containsKey("reason")) {
                            sb.append(suggestionMap.get("reason"));
                            sb.append("\n");
                        }
                    });
                    accountRecord.setAiSuggestion(sb.toString());
                } catch (Exception e) {
                    logger.warn("解析建议内容失败", e);
                }
            }
        });

        return ResponseEntity.ok(new ResponseResult<>(accountRecordList));
    }

    void doAiSuggestion(AccountRecord accountRecord) throws Exception {
        if (StringUtils.isNotEmpty(accountRecord.getAiSuggestion())) {
            return;
        }

        LocalDateTime monthEndTime = LocalDateTime.now().plusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        if (accountRecord.getCreateTime().isAfter(monthEndTime)) {
            return;
        }

        List<Student> studentList = studentService.findStudentByName(accountRecord.getCreateBy());
        Student student = studentList.get(0);
        if (student.getExpectMonthlySpend() == null || student.getExpectMonthlySpend().compareTo(BigDecimal.ZERO) == 0) {
            logger.warn("can not do ai suggestion for {} . expectMonthlySpend not configured", accountRecord.getCreateBy());
            return;
        }

        Page<AccountRecord> currentMonthAccountRecordPage = accountRecordService.getRecordsByTimeRange(accountRecord.getCreateBy(), LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0), LocalDateTime.now(), 0, 100);
        List<AccountRecord> currentMonthLargeAccountRecordList = currentMonthAccountRecordPage.getContent().stream().sorted((accountRecord1, accountRecord2) -> ObjectUtils.compare(accountRecord2.getAmount(), accountRecord1.getAmount())).limit(20).collect(Collectors.toList());
        Generation gen = new Generation();
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content(SUGGEST_ACCOUNT_RECORD_SYSTEM_PROMPT)
                .build();
        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content("已产生的当月消费交易: " + ObjectMapperUtil.serialize(currentMonthLargeAccountRecordList))
                .build();
        Message userMsg2 = Message.builder()
                .role(Role.USER.getValue())
                .content("期望月均消费额: " + student.getExpectMonthlySpend().toString())
                .build();
        Message userMsg3 = Message.builder()
                .role(Role.USER.getValue())
                .content("帮我建议未来的费用支出: " + accountRecord.getDetails() + " 费用：" + accountRecord.getAmount() + " 时间：" + accountRecord.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
        GenerationParam param = GenerationParam.builder()
                // 若没有配置环境变量，请用百炼API Key将下行替换为：.apiKey("sk-xxx")
                .apiKey(apiKey)
                .model(reasoningModel)
                .messages(Arrays.asList(systemMsg, userMsg, userMsg2, userMsg3))
                // 不可以设置为"text"
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();
        Flowable<GenerationResult> result = gen.streamCall(param);
        StringBuilder parsedSuggestionResultSB = new StringBuilder();
        result.blockingForEach(message -> parsedSuggestionResultSB.append(message.getOutput().getChoices().get(0).getMessage().getContent()));
        String parsedSuggestionResult = parsedSuggestionResultSB.toString();
        logger.info("解析结果：{}", parsedSuggestionResult);

        String parsedSuggestionJSON = parsedSuggestionResult.split("```")[1].trim();
        if (parsedSuggestionJSON.startsWith("json")) {
            parsedSuggestionJSON = parsedSuggestionJSON.substring("json".length()).trim();
        }

        accountRecord.setAiSuggestion(parsedSuggestionJSON);
        accountRecordService.saveRecord(accountRecord);
    }
}