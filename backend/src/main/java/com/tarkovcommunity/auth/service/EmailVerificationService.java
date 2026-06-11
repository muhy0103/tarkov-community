package com.tarkovcommunity.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tarkovcommunity.auth.config.EmailVerificationProperties;
import com.tarkovcommunity.auth.dto.EmailVerificationResponse;
import com.tarkovcommunity.auth.dto.EmailVerificationResult;
import com.tarkovcommunity.auth.entity.EmailVerificationToken;
import com.tarkovcommunity.auth.mapper.EmailVerificationTokenMapper;
import com.tarkovcommunity.user.entity.SysUser;
import com.tarkovcommunity.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_USED = "USED";
    private static final String STATUS_EXPIRED = "EXPIRED";

    private final EmailVerificationTokenMapper tokenMapper;
    private final SysUserMapper userMapper;
    private final JavaMailSender mailSender;
    private final EmailVerificationProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${spring.mail.password:}")
    private String mailPassword;

    public EmailVerificationResult createAndSendVerification(SysUser user) {
        String token = newToken();
        String verificationUrl = verificationUrl(token);

        EmailVerificationToken record = new EmailVerificationToken();
        record.setUserId(user.getId());
        record.setTokenHash(hash(token));
        record.setStatus(STATUS_PENDING);
        record.setExpiresAt(LocalDateTime.now().plusMinutes(Math.max(5, properties.getTokenMinutes())));
        tokenMapper.insert(record);

        if (!properties.isEnabled() || !StringUtils.hasText(mailPassword)) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "邮箱验证服务未配置，请联系管理员");
        }

        try {
            sendVerificationMail(user, verificationUrl);
        } catch (MailException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "验证邮件暂时无法发送，请稍后再试", exception);
        }

        return new EmailVerificationResult(true);
    }

    @Transactional
    public EmailVerificationResponse verifyEmail(String token) {
        if (!StringUtils.hasText(token)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "\u9a8c\u8bc1\u94fe\u63a5\u65e0\u6548");
        }

        EmailVerificationToken record = tokenMapper.selectOne(new LambdaQueryWrapper<EmailVerificationToken>()
                .eq(EmailVerificationToken::getTokenHash, hash(token.trim()))
                .eq(EmailVerificationToken::getStatus, STATUS_PENDING));

        if (record == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "\u9a8c\u8bc1\u94fe\u63a5\u65e0\u6548\u6216\u5df2\u4f7f\u7528");
        }

        if (record.getExpiresAt() != null && record.getExpiresAt().isBefore(LocalDateTime.now())) {
            record.setStatus(STATUS_EXPIRED);
            tokenMapper.updateById(record);
            throw new ResponseStatusException(HttpStatus.GONE, "\u9a8c\u8bc1\u94fe\u63a5\u5df2\u8fc7\u671f\uff0c\u8bf7\u91cd\u65b0\u6ce8\u518c\u6216\u8054\u7cfb\u7ba1\u7406\u5458");
        }

        SysUser user = userMapper.selectById(record.getUserId());
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "\u7528\u6237\u4e0d\u5b58\u5728");
        }

        if (!"NORMAL".equals(user.getStatus())) {
            user.setStatus("NORMAL");
            userMapper.updateById(user);
        }

        record.setStatus(STATUS_USED);
        record.setVerifiedAt(LocalDateTime.now());
        tokenMapper.updateById(record);

        return new EmailVerificationResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getStatus(),
                "\u90ae\u7bb1\u9a8c\u8bc1\u6210\u529f\uff0c\u73b0\u5728\u53ef\u4ee5\u767b\u5f55"
        );
    }

    private void sendVerificationMail(SysUser user, String verificationUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.getFrom());
        message.setTo(user.getEmail());
        message.setSubject("\u9003\u79bb\u5854\u79d1\u592b\u73a9\u5bb6\u60c5\u62a5\u793e\u533a - \u786e\u8ba4\u4f60\u7684\u90ae\u7bb1");
        message.setText("""
                %s，你好：

                欢迎注册逃离塔科夫玩家情报社区。请在 %d 分钟内点击下面的链接完成邮箱确认：

                %s

                如果不是你本人操作，可以忽略这封邮件。
                """.formatted(user.getNickname(), Math.max(5, properties.getTokenMinutes()), verificationUrl));
        mailSender.send(message);
    }

    private String verificationUrl(String token) {
        String baseUrl = properties.getFrontendUrl();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + "/verify-email?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }

    private String newToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
