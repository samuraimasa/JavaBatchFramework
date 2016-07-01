package framework.mail;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import framework.util.UtilConfig;
import framework.util.UtilMisc;

/**
 * AWS SESで送ろう
 * メール送信
 *
 */
public class MailSender{
    public static final String SMTP_HOST        = "mail_smtp_host";
    public static final String SMTP_PORT        = "mail_smtp_port";
    public static final String SMTP_USER        = "mail_smtp_user";
    public static final String SMTP_PASS        = "mail_smtp_pass";
    public static final String MAIL_FROM        = "mail_from";
    public static final String MAIL_REPLY       = "mail_reply";
    public static final String MAIL_TO          = "mail_to";
    public static final String MAIL_ENCODE      = "iso-2022-jp";
    public static final String MAIL_ENCODE_SJIS = "MS932";
    public static final String MAIL_ENCODE_UTF8 = "UTF-8";
    
    private String smtpHost = null;
    private int smtpPort = 0;
    private String smtpUser = null;
    private String smtpPass = null;
    private String fromAddress = null;
    private String replyTo = null;
    private String toAddress = null;
    private String title = null;
    private String text = null;
    private String fromName = null;
    
    private boolean error = false;
    private String lastError = null;
    

    // テスト送信
    public static void main(String... args){
        HashMap<String,String> confMap = UtilConfig.getConfigMap();
        MailSender mailSender = new MailSender(confMap);
        mailSender.send("java test", "エラーしたときに送信します");
    }
    
    public MailSender(HashMap<String,String> confMap){
        this.setSmtpHost((String)confMap.get(SMTP_HOST));
        this.setSmtpPort(Integer.parseInt(confMap.get(SMTP_PORT)));
        this.setSmtpUser((String)confMap.get(SMTP_USER));
        this.setSmtpPass((String)confMap.get(SMTP_PASS));
        this.setFromAddress((String)confMap.get(MAIL_FROM));
        this.setReplyTo((String)confMap.get(MAIL_REPLY));
        this.setToAddress((String)confMap.get(MAIL_TO));
        this.setFromName((String)confMap.get(UtilConfig.APP_NAME));
    }
    
    /**
     * 件名と内容を指定して送信
     * @param title
     * @param text
     */
    public void send(String title, String text){
        this.setTitle(title);
        this.setText(text);
        this.send();
    }
    
    /**
     * 送信処理
     */
    public void send(){
        this.error = false;
        
        // 値のチェック
        if(UtilMisc.isEmpty(this.smtpHost)) return;
        if(this.smtpPort <= 0) return;
        if(UtilMisc.isEmpty(this.smtpUser)) return;
        if(UtilMisc.isEmpty(this.smtpPass)) return;
        if(UtilMisc.isEmpty(this.fromAddress)) return;
        if(UtilMisc.isEmpty(this.toAddress)) return;
        if(UtilMisc.isEmpty(this.title)) return;
        if(UtilMisc.isEmpty(this.text)) return;
        
        try{
            // SMTPサーバーのアドレスを指定
            Properties props = System.getProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.port", this.smtpPort);
            
            // aws用 options
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
            
            Session session = Session.getDefaultInstance(props);
            MimeMessage mimeMessage = new MimeMessage(session);
            // 送信元メールアドレスと送信者名を指定
            mimeMessage.setFrom(new InternetAddress(this.fromAddress, this.fromName, MAIL_ENCODE_UTF8));
            if(!UtilMisc.isEmpty(this.replyTo)) {
                mimeMessage.setReplyTo(getAddressArray(this.replyTo));
            }
            // 送信先メールアドレスを指定（宛先指定）
            Address[] arrAddress = getAddressArray(this.toAddress);
            if(arrAddress.length == 0) return;
            mimeMessage.setRecipients(Message.RecipientType.TO, arrAddress);
            
            // タイトルを指定
            mimeMessage.setSubject(this.title, MAIL_ENCODE_UTF8);
            // 内容を指定
            mimeMessage.setText(this.text + "\n", MAIL_ENCODE_UTF8);
            // 形式を指定
            mimeMessage.setHeader("Content-Type","text/plain");
            // 送信日付を指定
            mimeMessage.setSentDate(new Date());
            
            Transport transport = session.getTransport();
            transport.connect(this.smtpHost, this.smtpUser, this.smtpPass);
            transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
            
        }catch(Exception e){
            this.error = true;
            this.lastError = e.getMessage();
            System.out.println(this.lastError);
        }
    }
    
    public boolean isError() {
        return this.error;
    }
    public String getLastError() {
        return this.lastError;
    }
    
    /**
     * カンマ区切りの複数アドレス文字列をアドレス型の配列に変換
     * @param address
     * @return
     * @throws Exception
     */
    private Address[] getAddressArray(String address) throws Exception {
        String[] strArr = UtilMisc.splitter(address);
        ArrayList<InternetAddress> list = new ArrayList<>();
        for (int i = 0; i < strArr.length; i++) {
            if(strArr[i].length() == 0) continue;
            list.add(new InternetAddress(strArr[i]));
        }
        Address[] addr = new Address[list.size()];
        for(int i = 0; i < addr.length; i++) { addr[i] = (Address)list.get(i); }
        return addr;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }
    
    public void setSmtpPort(int port) {
        this.smtpPort = port;
    }
    
    public void setSmtpUser(String smtpUser) {
        this.smtpUser = smtpUser;
    }
    
    public void setSmtpPass(String smtpPass) {
        this.smtpPass = smtpPass;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }
}