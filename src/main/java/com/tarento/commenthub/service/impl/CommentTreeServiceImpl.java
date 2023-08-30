package com.tarento.commenthub.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.uuid.Generators;
import com.tarento.commenthub.constant.Constants;
import com.tarento.commenthub.dto.CommentsRequestDTO;
import com.tarento.commenthub.entity.CommentTree;
import com.tarento.commenthub.service.CommentTreeService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

@Service
@Log4j2
public class CommentTreeServiceImpl implements CommentTreeService {

    @Value("${jwt.secret.key}")
    private String jwtSecretKey;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void createOrUpdateCommentTree(JsonNode commentData) {
        if(commentData.get("id") != null && !commentData.get("id").equals("")) {
            updateCommentTree(commentData);
        } else {
            createCommentTree(commentData);
        }
    }

    @Override
    public CommentTree getCommentTree(CommentsRequestDTO commentsRequestDTO) {
//        write implementation to get comment_tree from commentsRequestDTO
        return null;
    }

    public void createCommentTree(JsonNode commentData) {
        String entityId = objectMapper.convertValue(commentData.get("entityId"), String.class);
        String entityType = objectMapper.convertValue(commentData.get("entityType"), String.class);
        String workflow = objectMapper.convertValue(commentData.get("workflow"), String.class);

        String commentTreeId = generateJwtTokenKey(entityId, entityType, workflow);
        log.info("commentTreeId {}", commentTreeId);

    }

    public void updateCommentTree(JsonNode commentData) {

    }

    public String generateJwtTokenKey(String entityId, String entityType, String workflow) {
        log.info("generating JwtTokenKey");
        String jwtToken = "";
        if (StringUtils.isNotBlank(entityId)
                && StringUtils.isNotBlank(entityType)
                && StringUtils.isNotBlank(workflow)) {
            LinkedHashMap<String, String> payload = new LinkedHashMap<>();
            payload.put(Constants.ENTITY_ID, entityId);
            payload.put(Constants.ENTITY_TYPE, entityType);
            payload.put(Constants.WORKFLOW, workflow);

            jwtToken = JWT.create().withPayload(payload).sign(Algorithm.HMAC256(jwtSecretKey));
        }
        return jwtToken;
    }

}
