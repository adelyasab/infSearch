package ru.itis.spingbootdemo.services;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassRelativeResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.SpringTemplateLoader;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Component
public class MailsServiceImpl implements MailsService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String mailFrom;

    private final Template confirmMailTemplate;

    public MailsServiceImpl() {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_30);

        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateLoader(
                new SpringTemplateLoader(new ClassRelativeResourceLoader(this.getClass()),
                        "/")
        );
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        try {
            confirmMailTemplate = configuration.getTemplate("mail/confirm_mail.ftlh");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void sendConfirmEmail(String email, String confirmCode) {
        String mailText = getEmailText(confirmCode);

        MimeMessagePreparator messagePreparator = getEmail(email, mailText);

        javaMailSender.send(messagePreparator);
    }

    private MimeMessagePreparator getEmail(String email, String mailText) {
        return mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom(mailFrom);
            messageHelper.setTo(email);
            messageHelper.setSubject("Подтверждение почты");
            messageHelper.setText(mailText, true);
        };
    }

    private String getEmailText(String confirmCode) {
        //подготовка данных
        Map<String, String> attributes = new HashMap<>();
        attributes.put("confirm_code", confirmCode);

        StringWriter writer = new StringWriter();
        try {
            //записали текст сообщения
            confirmMailTemplate.process(attributes, writer);
        } catch (TemplateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        //получили текст сообщения из шаблона
        String mailText = writer.toString();
        return mailText;
    }
}
