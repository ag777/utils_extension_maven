package github.ag777.util.network.mail;

import com.ag777.util.lang.collection.ListUtils;
import com.ag777.util.lang.collection.MapUtils;
import com.ag777.util.lang.exception.model.ValidateException;
import com.sun.mail.smtp.SMTPAddressFailedException;
import com.sun.mail.smtp.SMTPAddressSucceededException;
import com.sun.mail.smtp.SMTPSendFailedException;
import com.sun.mail.util.MailConnectException;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 邮件发送工具类，对jakarta.mail的二次封装
 * <p>
 *     SMTP是专门用于邮件发送的协议，而POP3和IMAP是用于接收邮件的协议<br/>
 *     IMAP协议相对于POP3（邮局协议版本3）协议具有更多的功能和灵活性，适用于需要在多个设备上同步邮件状态或进行更复杂操作的场景
 * </p>
 *
 * <p>
 *     IMAP协议的优点包括：
 *     <ul>
 *         <li>支持在多个设备上同步邮件，可以在不同设备上查看、删除、标记邮件的状态同步。</li>
 *         <li>可以在邮件服务器上创建、删除、重命名邮件夹，方便组织和管理邮件。</li>
 *         <li>支持通过搜索条件来查找邮件，可以根据日期、发件人、主题等进行高级搜索。</li>
 *         <li>可以部分下载邮件内容，节省带宽和存储空间。</li>
 *     </ul>
 * </p>
 *
 * @see <a href="https://eclipse-ee4j.github.io/mail/">Jakarta Mail官方文档</a>
 * @author ag777 <837915770@vip.qq.com>
 * @version 2023/9/26 14:46
 */
public class MailUtils {

    /** POP3（邮局协议版本3）默认端口号 */
    private static final int PORT_POP3 = 110;
    /** POP3（邮局协议版本3）加密连接端口号 */
    private static final int PORT_POP3_SSL = 995;
    /** IMAP（互联网消息访问协议）默认端口号 */
    private static final int PORT_IMAP = 143;
    /** IMAP（互联网消息访问协议）加密连接端口号 */
    private static final int PORT_IMAP_SSL = 993;
    /** SMTP（简单邮件传输协议）默认端口号 */
    private static final int PORT_SMTP = 25;
    /** SMTP（简单邮件传输协议）加密连接端口号 */
    private static final int PORT_SMTP_SSL = 465;

    public static void main(String[] args) throws ValidateException {
        String host = "smtp.qq.com";
        String password = "去qq邮箱设置里获取";
        String from = "a@qq.com";
        String to = "b@qq.com";
        sendMail(
                from,
                password,
                host,
                PORT_SMTP_SSL,
                ListUtils.of(to),
//                ListUtils.of(from),
                null,
                "smtp发送邮件测试",
                "我是内容1234",
                ListUtils.of(new File("D:\\表情.png")),
                true
        );
    }

    /**
     * 发送邮件方法
     *
     * @param account    登录账号，通常是发件人的邮箱地址
     * @param password    密码/授权码，用于登录邮箱服务器进行身份验证
     * @param smtpHost    SMTP服务器地址，例如："smtp.example.com"
     * @param smtpPort    SMTP服务器端口号，例如：587
     * @param toEmails  收件人邮箱地址列表，支持多个收件人
     * @param ccEmails      抄送人邮箱地址列表，支持多个抄送人
     * @param subject     邮件主题
     * @param content        邮件正文
     * @param attachments 附件文件路径列表，支持多个附件，可以为null或空列表表示无附件
     * @param useSSL 是否使用ssl连接邮件服务器
     * @throws ValidateException 当邮件发送失败时，抛出包含友好错误信息的异常
     */
    public static void sendMail(String account, String password, String smtpHost, int smtpPort, List<String> toEmails, List<String> ccEmails, String subject, String content, List<File> attachments, boolean useSSL) throws ValidateException {
        sendMail(account, password, smtpHost, smtpPort, toEmails, ccEmails, subject, content, attachments, null, useSSL, false);
    }

    /**
     * 发送邮件方法
     *
     * @param account    登录账号，通常是发件人的邮箱地址
     * @param password    密码/授权码，用于登录邮箱服务器进行身份验证
     * @param smtpHost    SMTP服务器地址，例如："smtp.example.com"
     * @param smtpPort    SMTP服务器端口号，例如：587
     * @param toEmails  收件人邮箱地址列表，支持多个收件人
     * @param ccEmails      抄送人邮箱地址列表，支持多个抄送人
     * @param subject     邮件主题
     * @param content        邮件正文
     * @param attachments 附件文件路径列表，支持多个附件，可以为null或空列表表示无附件
     * @param connectTimeoutMills 连接超时时间
     * @param useSSL 是否使用ssl连接邮件服务器
     * @param debug 是否开启debug模式
     * @throws ValidateException 当邮件发送失败时，抛出包含友好错误信息的异常
     */
    public static void sendMail(String account, String password, String smtpHost, int smtpPort, List<String> toEmails, List<String> ccEmails, String subject, String content, List<File> attachments, long connectTimeoutMills, boolean useSSL, boolean debug) throws ValidateException {
        sendMail(account, password, smtpHost, smtpPort, toEmails, ccEmails, subject, content, attachments, MapUtils.of(
                "mail.smtp.connectiontimeout", connectTimeoutMills
        ), useSSL, debug);
    }

    /**
     * 发送邮件方法
     *
     * @param account    登录账号，通常是发件人的邮箱地址
     * @param password    密码/授权码，用于登录邮箱服务器进行身份验证
     * @param smtpHost    SMTP服务器地址，例如："smtp.example.com"
     * @param smtpPort    SMTP服务器端口号，例如：587
     * @param toEmails  收件人邮箱地址列表，支持多个收件人
     * @param ccEmails      抄送人邮箱地址列表，支持多个抄送人
     * @param subject     邮件主题
     * @param content        邮件正文
     * @param attachments 附件文件路径列表，支持多个附件，可以为null或空列表表示无附件
     * @param otherProps 其它属性，可以为空
     * @param useSSL 是否使用ssl连接邮件服务器
     * @param debug 是否开启debug模式
     * @throws ValidateException 当邮件发送失败时，抛出包含友好错误信息的异常
     */
    public static void sendMail(String account, String password, String smtpHost, int smtpPort, List<String> toEmails, List<String> ccEmails, String subject, String content, List<File> attachments, Map<String, Object> otherProps, boolean useSSL, boolean debug) throws ValidateException {
        // 设置邮件属性
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.auth", "true");

        /*
         * bug:邮件附件名字过长时，收件会得到一个名字奇怪的附件,通过修改系统属性能够解决这个问题
         * <p>
         * 参考资料:https://blog.csdn.net/baidu_35962462/article/details/81062629
         * </p>
         */
        props.setProperty("mail.mime.splitlongparameters", "false");

        if (!MapUtils.isEmpty(otherProps)) {
            props.putAll(otherProps);
        }

        // 根据useSSL参数设置SSL连接
        if (useSSL) {
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.port", smtpPort);
        }

        // 创建认证对象
        Authenticator auth = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(account, password);
            }
        };

        try {
            // 创建邮件会话
            Session session = Session.getInstance(props, auth);
            // debug模式
            session.setDebug(debug);

            // 创建邮件对象
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(account));
            message.setSubject(subject);

            // 添加收件人
            for (String toEmail : toEmails) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            }

            // 添加抄送人
            if (!ListUtils.isEmpty(ccEmails)) {
                for (String ccEmail : ccEmails) {
                    message.addRecipient(Message.RecipientType.CC, new InternetAddress(ccEmail));
                }
            }

            // 添加附件和正文
            if (!ListUtils.isEmpty(attachments)) {
                // 有附件的时候，附件正文需要一起添加
                Multipart multipart = getMultipart(content, attachments);
                message.setContent(multipart);
            } else {
                // 没附件的时间直接添加正文
                message.setContent(content, "text/html;charset=UTF-8");
            }

            // 发送邮件
            Transport.send(message);
        } catch (IllegalArgumentException e) {
            /*
            1.当你提供的邮件参数不正确时，例如：无效的邮件地址、无效的邮件格式等。
            2.当你尝试使用不支持的或不正确的协议发送邮件时，例如：尝试使用SMTP协议发送IMAP邮件。
            3.当你的邮件服务器设置不正确，或者邮件服务器URL不正确时。
             */
            throw new ValidateException("邮件发送失败：请检查配置是否正确", e);
        } catch (MailConnectException e) {
            throw new ValidateException("邮件发送失败：连接邮箱服务失败", e);
        } catch (SMTPAddressFailedException e) {
            // 表示SMTP服务器拒绝了一个或多个收件人地址。这可能是因为地址格式错误、收件人不存在或其他原因。
            throw new ValidateException("邮件发送失败：请检查收件人邮箱地址有误", e);
        } catch (SMTPAddressSucceededException e) {
            // 表示SMTP服务器接受了一个或多个收件人地址，但在发送过程中出现了问题。这种情况下，邮件可能只发送给部分收件人。
            throw new ValidateException("邮件发送失败：发送邮件到目标邮箱出现异常", e);
        } catch (SMTPSendFailedException e) {
            // 表示SMTP服务器在处理邮件发送时遇到了问题。这可能是因为服务器错误、客户端配置错误或其他原因。
            throw new ValidateException("邮件发送失败：发送邮件出现异常，可能是因为服务器错误、客户端配置错误导致", e);
        } catch(UnsupportedEncodingException e) {
            throw new ValidateException("邮件发送失败：请检查邮箱地址拼写有误", e);
        } catch (SendFailedException e) {
            throw new ValidateException("邮件发送失败：收件人或抄送人邮箱地址有误", e);
        } catch (AuthenticationFailedException e) {
            throw new ValidateException("邮件发送失败：登录账号或密码/授权码不正确", e);
        } catch (NoSuchProviderException e) {
            throw new ValidateException("邮件发送失败：SMTP服务器设置有误", e);
        } catch (MessagingException e) {
            throw new ValidateException("邮件发送失败：消息处理出现问题，请检查邮件内容和附件", e);
        } catch (IOException e) {
            throw new ValidateException("邮件发送失败：附件读取出错，请检查附件路径和文件", e);
        } catch (Exception e) {
            throw new ValidateException("邮件发送失败：出现未知错误，请检查代码和配置", e);
        }
    }

    /**
     * @param content 正文
     * @param attachments 附件
     * @return 邮件主体
     * @throws MessagingException 邮件异常
     * @throws IOException IO异常
     */
    private static Multipart getMultipart(String content, List<File> attachments) throws MessagingException, IOException {
        Multipart multipart = new MimeMultipart();

        // 添加正文内容
        MimeBodyPart contentPart = new MimeBodyPart();
        contentPart.setContent(content, "text/html;charset=UTF-8");
        multipart.addBodyPart(contentPart);

        // 添加附件
        for (File attachment : attachments) {
            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.attachFile(attachment);
            multipart.addBodyPart(attachmentPart);
        }
        return multipart;
    }
}
