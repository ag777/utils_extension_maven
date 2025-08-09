package github.ag777.util.software.oof;

import com.google.gson.JsonObject;
import github.ag777.util.gson.JsonObjectUtils;
import github.ag777.util.http.HttpApiUtils;
import github.ag777.util.http.HttpHelper;
import github.ag777.util.http.HttpUtils;
import github.ag777.util.http.model.MyCall;
import github.ag777.util.lang.Console;
import github.ag777.util.lang.exception.model.GsonSyntaxException;
import github.ag777.util.lang.exception.model.ValidateException;
import lombok.SneakyThrows;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.Optional;

/**
 * 115网盘文件上传工具类
 * 
 * @author ag777 <837915770@vip.qq.com>
 * @version 2025/8/3 14:20
 */
public class OofFileUtil {
    private final HttpHelper http;
    private final String cookies;
    private final long userId;

    /**
     * 构造函数，从cookie中自动提取userId
     *
     * @param http http实例
     * @param cookies cookie字符串
     * @throws IllegalArgumentException 如果无法从cookie中提取userId
     */
    public OofFileUtil(HttpHelper http, String cookies) throws IllegalArgumentException {
        this.http = http;
        this.cookies = cookies;
        Optional<Long> userIdOpt = OofUtils.extractUserIdFromCookie(cookies);
        if (userIdOpt.isEmpty()) {
            throw new IllegalArgumentException("无法从cookie中提取userId，请检查cookie格式是否正确");
        }
        this.userId = userIdOpt.get();
    }

    /**
     * 上传文件到115
     * @param file 文件
     * @param cid 115对应路径的cid
     * @return 文件id
     * @throws ValidateException 上传文件异常
     * @throws FileNotFoundException 文件不存在异常
     * @throws GsonSyntaxException 解析json异常
     */
    public String upload(File file, String cid) throws ValidateException, FileNotFoundException, GsonSyntaxException {
        return upload(file, file.getName(), cid);
    }

    /**
     * 上传文件到115
     * @param file 文件
     * @param fileName 文件名
     * @param cid 115对应路径的cid
     * @return 文件id
     * @throws ValidateException 上传文件异常
     * @throws FileNotFoundException 文件不存在异常
     * @throws GsonSyntaxException 解析json异常
     */
    public String upload(File file, String fileName, String cid) throws ValidateException, FileNotFoundException, GsonSyntaxException {
        Map<String, Object> permission = getPermission(file, fileName, cid);
        return upload(file, permission);
    }

    /**
     * 获取文件上传权限
     * @param file 文件
     * @param cid 115对应路径的cid
     * @return 文件上传权限
     * @throws ValidateException 获取文件上传权限异常
     */
    public Map<String, Object> getPermission(File file, String cid) throws ValidateException {
        return getPermission(file, file.getName(), cid);
    }

    /**
     * 获取文件上传权限（支持自定义文件名）
     * @param file 文件
     * @param fileName 自定义文件名
     * @param cid 115对应路径的cid
     * @return 文件上传权限
     * @throws ValidateException 获取文件上传权限异常
     */
    private Map<String, Object> getPermission(File file, String fileName, String cid) throws ValidateException {
        if (!file.exists()) {
            throw new ValidateException("文件不存在:"+file.getAbsolutePath());
        }
        if (!file.isFile()) {
            throw new ValidateException("上传目标不是文件:"+file.getAbsolutePath());
        }
        MyCall call = http
            .post("https://uplb.115.com/3.0/sampleinitupload.php", Map.of(
                "userid", userId,
                "filename", fileName,
                "filesize", file.length(),
                "target", toDir(cid)
            ), Map.of(
                "Cookie", cookies
            ));
        try {
            /*
             {
                "object": "example_object_id_1234567890abcdef",
                "accessid": "example_access_id_1234567890",
                "host": "https://example-bucket.oss-cn-shenzhen.aliyuncs.com",
                "policy": "example_policy_base64_encoded_string",
                "signature": "example_signature_string",
                "expire": 1754190737,
                "callback": "example_callback_base64_encoded_string"
             }
             */

            JsonObject permission = HttpApiUtils.executeForJsonObject(
                    call,
                    "获取文件上传权限",
                    ValidateException::new,
                    null
            );
            return Map.of(
                "name", fileName,
                "key", JsonObjectUtils.getStr(permission, "object"),
                "policy",JsonObjectUtils.getStr(permission, "policy"),
                "OSSAccessKeyId",JsonObjectUtils.getStr(permission, "accessid"),
                "success_action_status",200,
                "callback", JsonObjectUtils.getStr(permission, "callback"),
                "signature",JsonObjectUtils.getStr(permission, "signature")
            );
        } catch (SocketTimeoutException e) {
            throw new ValidateException("获取授权接口调用超时",e);
        } catch (ConnectException e) {
            throw new ValidateException("获取授权接口连接超时",e);
        } catch (GsonSyntaxException e) {
            throw new ValidateException("解析获取授权接口返回异常",e);
        }
    }

    /**
     * 上传文件到115
     * @param file 文件
     * @param permission 文件上传权限
     * @return 文件id
     * @throws FileNotFoundException 文件不存在异常
     * @throws ValidateException 上传文件异常
     */
    private String upload(File file, Map<String, Object> permission) throws FileNotFoundException, ValidateException {
        MyCall call = http.postMultiFiles(
                "https://fhnfile.oss-cn-shenzhen.aliyuncs.com",
                "file",
                new File[]{file},
                permission,
                null
        );

        try {
            /*
             {
                "state": true,
                "message": "",
                "code": 0,
                "data": {
                    "aid": 1,
                    "cid": "example_cid_1234567890",
                    "file_name": "example_file.txt",
                    "file_ptime": 1754189761,
                    "file_status": 1,
                    "file_id": "example_file_id_1234567890",
                    "file_size": "84",
                    "pick_code": "example_pick_code_1234567890",
                    "sha1": "EXAMPLE_SHA1_HASH_1234567890ABCDEF",
                    "sp": 0,
                    "file_type": 103,
                    "is_video": 0
                }
             */
            JsonObject jo = HttpApiUtils.executeForJsonObject(
                call,
                "上传文件",
                ValidateException::new,
                response -> {
                    try {
                        Optional<String> res = HttpUtils.responseStrForce(response);
                        if (res.isPresent()) {
                            String xmlStr = res.get();
                            if (xmlStr.contains("<Error>")) {
                                try {
                                    DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
                                    DocumentBuilder builder2 = factory.newDocumentBuilder();
                                    Document doc = builder2.parse(new java.io.ByteArrayInputStream(xmlStr.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
                                    NodeList messageNodes = doc.getElementsByTagName("Message");
                                    if (messageNodes.getLength() > 0) {
                                        String errorMsg = messageNodes.item(0).getTextContent();
                                        return new ValidateException("上传文件接口返回异常:"+errorMsg);
                                    }
                                } catch (Exception e) {
                                    // 解析异常时忽略，继续后续处理
                                }
                            }
                        }
                    } catch (IOException ignored) {
                    }
                    return null;
                }
            );
            if (JsonObjectUtils.getInt(jo, "code", -99) != 0) {
                throw new ValidateException(JsonObjectUtils.getStr(jo, "message", "上传失败"));
            }
            JsonObject data = JsonObjectUtils.getJsonObject(jo, "data");
            return JsonObjectUtils.getStr(data, "file_id");
        } catch (SocketTimeoutException e) {
            throw new ValidateException("上传文件接口调用超时",e);
        } catch (ConnectException e) {
            throw new ValidateException("上传文件接口连接超时",e);
        } catch (GsonSyntaxException e) {
            throw new ValidateException("解析上传文件接口返回异常",e);
        }
    }

    /**
     * 将cid转换为115对应路径
     * @param cid 115对应路径的cid
     * @return 115对应路径
     */
    private static String toDir(String cid) {
        return "U_1_"+cid;
    }

    /**
     * 测试
     * @param args 参数
     */
    @SneakyThrows
    public static void main(String[] args) {
        OofFileUtil uploader = new OofFileUtil(
                new HttpHelper(null, "115"), "xxx"
        );
        Console.prettyLog(uploader.upload(
                new File("example_file.txt"),
                "example_cid_1234567890"));
    }
}
