package com.wxm.wuyou.controller;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.aliyun.oss.OSS;
import com.fasterxml.jackson.core.type.TypeReference;
import com.wxm.wuyou.common.response.ResponseResult;
import com.wxm.wuyou.entity.AccountRecord;
import com.wxm.wuyou.utils.ObjectMapperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.time.Instant;
import java.util.*;

/**
 * 文件操作控制器
 * 提供文件操作相关的RESTful API接口
 */
@RestController
@RequestMapping("/api/file")
@Validated
public class FileController {
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);


    @Autowired
    private OSS ossClient;

    @Value("${openai.apiKey}")
    String apiKey;

    @Value("${openai.apiBase}")
    String apiBase;

    @PostMapping("/parse-expense-file")
    public ResponseEntity parseExpenseFile(MultipartFile[] files) throws Exception {
        MultiModalConversation conv = new MultiModalConversation();
        MultiModalMessage systemMsg = MultiModalMessage.builder().role(Role.SYSTEM.getValue())
                .content(Arrays.asList(Collections.singletonMap("text", AccountRecordController.PARSE_EXPENSE_FILE_SYSTEM_PROMPT))).build();

        List<Map<String, Object>> userMsgMapList = new LinkedList<>();
        for (MultipartFile file : files) {
            String bucket = "wuyou-study";
            String object = "expense-files/" + file.getOriginalFilename();
            ossClient.putObject(bucket, object, file.getInputStream());
            URL presignedUrl = ossClient.generatePresignedUrl(bucket, object, new Date(Instant.now().plusSeconds(3600).toEpochMilli()));
            userMsgMapList.add(Collections.singletonMap("image", presignedUrl.toString()));
        }
        userMsgMapList.add(Collections.singletonMap("text", "请解析费用支出截图"));
        MultiModalMessage userMsg = MultiModalMessage.builder().role(Role.USER.getValue())
                .content(userMsgMapList).build();
        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .apiKey(apiKey)
                .model("qwen-vl-max")
                .messages(Arrays.asList(systemMsg, userMsg))
                .build();
        MultiModalConversationResult result = conv.call(param);

        List<Map<String, Object>> parsedResult = result.getOutput().getChoices().get(0).getMessage().getContent();
        logger.info("解析结果：", parsedResult);

        String parsedExpenseJSON = ((String)parsedResult.get(0).get("text")).split("```")[1].trim();
        if (parsedExpenseJSON.startsWith("json")) {
            parsedExpenseJSON = parsedExpenseJSON.substring("json".length()).trim();
        }
        List<AccountRecord> parsedAccountRecordList = ObjectMapperUtil.deserialize(parsedExpenseJSON, new TypeReference<List<AccountRecord>>() {
        });

        return ResponseEntity.ok(new ResponseResult<>(parsedAccountRecordList));
    }
}