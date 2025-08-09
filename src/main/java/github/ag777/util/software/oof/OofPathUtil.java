package github.ag777.util.software.oof;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import github.ag777.util.gson.JsonObjectUtils;
import github.ag777.util.http.HttpApiUtils;
import github.ag777.util.http.HttpHelper;
import github.ag777.util.http.model.MyCall;
import github.ag777.util.lang.StringUtils;
import github.ag777.util.lang.exception.model.GsonSyntaxException;
import github.ag777.util.lang.exception.model.ValidateException;
import github.ag777.util.software.oof.model.OofEntry;
import github.ag777.util.software.oof.model.OofFileInfo;
import github.ag777.util.software.oof.model.OofFolder;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.function.Function;

/**
 * 115网盘目录路径查找工具类
 * 根据目录路径(如 a\b\c)查找对应的cid
 * 
 * @author ag777 <837915770@vip.qq.com>
 * @version 2025/8/3 14:20
 */
public class OofPathUtil {

    private static final String BASE_URL = "https://webapi.115.com/files";
    private static final String FALLBACK_URL = "https://aps.115.com/natsort/files.php";

    private final String cookie;

    private final HttpHelper http;

    public OofPathUtil(HttpHelper http, String cookie) {
        this.cookie = cookie;
        this.http = http;
    }
    
    /**
     * 根据目录路径查找对应的cid
     * 
     * @param path 目录路径，支持多种分隔符: \ / 
     * @return 找到的cid，如果没找到返回Optional.empty()
     * @throws ValidateException 获取目录列表接口返回异常
     * @throws GsonSyntaxException 解析接口返回异常
     */
    public Optional<String> findCidByPath(String path) throws ValidateException, GsonSyntaxException {
        if (StringUtils.isEmpty(path)) {
            return Optional.of("0"); // 根目录
        }
        
        // 分割路径
        String[] pathParts = path.split("[\\\\/]");
        List<String> pathList = Arrays.stream(pathParts)
                .filter(part -> !StringUtils.isEmpty(part))
                .toList();
        
        if (pathList.isEmpty()) {
            return Optional.of("0"); // 根目录
        }
        
        // 从根目录开始逐级查找
        String currentCid = "0";
        for (int i = 0; i < pathList.size(); i++) {
            String folderName = pathList.get(i);
            Optional<String> nextCid = findFolderCid(currentCid, folderName);
            if (nextCid.isEmpty()) {
                // 构建已找到的路径用于错误提示
                StringBuilder foundPath = new StringBuilder();
                for (int j = 0; j < i; j++) {
                    if (j > 0) foundPath.append("\\");
                    foundPath.append(pathList.get(j));
                }
                System.err.println("未找到目录: " + folderName + 
                    (!foundPath.isEmpty() ? " 在路径: " + foundPath.toString() : ""));
                return Optional.empty();
            }
            currentCid = nextCid.get();
        }
        
        return Optional.of(currentCid);
    }
    
    /**
     * 在指定目录下查找文件夹的cid
     * 
     * @param parentCid 父目录cid
     * @param folderName 文件夹名称
     * @return 找到的cid，如果没找到返回Optional.empty()
     * @throws ValidateException 获取目录列表接口返回异常
     * @throws GsonSyntaxException 解析接口返回异常
     */
    private Optional<String> findFolderCid(String parentCid, String folderName) throws ValidateException, GsonSyntaxException {
        final String[] found = new String[1];
        fetchFileList(parentCid, entry -> {
            // 原逻辑：遇到文件即停止遍历
            if (entry instanceof OofFileInfo) {
                return false;
            }
            if (folderName.equals(entry.getN())) {
                found[0] = entry.getCid();
                return false; // 找到后终止
            }
            return true; // 继续
        });
        return Optional.ofNullable(found[0]);
    }
    
    /**
     * 获取指定目录下的所有文件夹列表
     * 
     * @param parentCid 父目录cid，使用"0"表示根目录
     * @return 文件夹名称列表
     * @throws ValidateException 解析获取目录列表返回异常
     * @throws GsonSyntaxException 解析接口返回异常
     */
    public List<String> listFolderNames(String parentCid) throws ValidateException, GsonSyntaxException {
        List<String> folders = new java.util.ArrayList<>();
        fetchFileList(parentCid, entry -> {
            if (entry instanceof OofFileInfo) {
                return false; // 一旦遇到文件，按旧逻辑终止
            }
            folders.add(entry.getN());
            return true;
        });
        return folders;
    }

    /**
     * 逐页面获取文件(夹)列表
     * @param cid 目录对应cid
     * @param onFileList 文件(夹列表)，返回是否继续遍历
     */
    public void fetchFileList(String cid, Function<OofEntry, Boolean> onFileList) throws GsonSyntaxException {
        int offset = 0;
        int limit = 32;

        while (true) {
            JsonObject response;
            try {
                response = fetchEntryPage(cid, offset, limit);
            } catch (ValidateException e) {
                // 统一失败即终止
                return;
            }
            if (response == null) {
                return; // 请求失败
            }

            JsonArray data;
            try {
                data = JsonObjectUtils.getJsonArray(response, "data");
            } catch (GsonSyntaxException e) {
                return; // 数据异常，终止
            }
            if (data == null || data.isEmpty()) {
                return; // 无更多数据
            }

            for (JsonElement element : data) {
                JsonObject item = element.getAsJsonObject();
                OofEntry entry = toEntry(item);
                Boolean cont = onFileList.apply(entry);
                if (cont == null || !cont) {
                    return; // 上层要求终止
                }
            }

            int count;
            try {
                count = JsonObjectUtils.getInt(response, "count", 0);
            } catch (Exception e) {
                break;
            }

            if (offset + limit >= count) {
                break; // 没有更多数据
            }
            offset += limit;
        }
    }

    /**
     * 将接口返回的一条记录转换为 OofEntry（OofFolder 或 OofFileInfo）
     */
    private OofEntry toEntry(JsonObject item) throws GsonSyntaxException {
        int d = getIntFlexible(item, "d", 0); // d=1 文件；否则视为目录
        OofEntry base;
        if (d == 1) {
            OofFileInfo file = new OofFileInfo();
            // 文件专有字段
            file.setFid(getString(item, "fid"));
            file.setUid(getIntFlexible(item, "uid", null));
            file.setS(getIntFlexible(item, "s", null));
            file.setSta(getIntFlexible(item, "sta", null));
            file.setPt(getString(item, "pt"));
            file.setIco(getString(item, "ico"));
            file.setClassX(getString(item, "class"));
            file.setFatr(getString(item, "fatr"));
            file.setSha(getString(item, "sha"));
            file.setQ(getIntFlexible(item, "q", null));
            file.setD(getIntFlexible(item, "d", null));
            file.setC(getIntFlexible(item, "c", null));
            base = file;
        } else {
            OofFolder folder = new OofFolder();
            folder.setPid(getString(item, "pid"));
            folder.setCc(getString(item, "cc"));
            folder.setNs(getString(item, "ns"));
            folder.setIspl(getIntFlexible(item, "ispl", null));
            base = folder;
        }

        // 公共字段填充
        base.setAid(getIntFlexible(item, "aid", null));
        base.setCid(getString(item, "cid"));
        base.setN(getString(item, "n"));
        base.setM(getIntFlexible(item, "m", null));
        base.setPc(getString(item, "pc"));
        base.setT(getString(item, "t"));
        base.setTe(getString(item, "te"));
        base.setTp(getString(item, "tp"));
        base.setTu(getString(item, "tu"));
        base.setTo(getString(item, "to"));
        base.setP(getIntFlexible(item, "p", null));
        base.setFc(getIntFlexible(item, "fc", null));
        base.setSh(getIntFlexible(item, "sh", null));
        base.setE(getString(item, "e"));
        base.setFdes(getIntFlexible(item, "fdes", null));
        base.setHdf(getIntFlexible(item, "hdf", null));
        base.setEt(getIntFlexible(item, "et", null));
        base.setEpos(getString(item, "epos"));
        base.setFvs(getIntFlexible(item, "fvs", null));
        base.setCheckCode(getIntFlexible(item, "check_code", null));
        base.setCheckMsg(getString(item, "check_msg"));
        base.setFuuid(getIntFlexible(item, "fuuid", null));
        base.setU(getString(item, "u"));
        base.setIssct(getIntFlexible(item, "issct", null));
        base.setScore(getIntFlexible(item, "score", null));
        base.setIsTop(getIntFlexible(item, "is_top", null));

        // 标签列表
        JsonElement flElem = item.get("fl");
        if (flElem != null && flElem.isJsonArray()) {
            base.setFl(parseTags(flElem.getAsJsonArray()));
        }

        return base;
    }

    /**
     * 解析标签数组，转换为 OofEntry.FlDTO 列表
     *
     * @param arr JsonArray，标签数组（每个元素为标签对象）
     * @return 标签DTO列表，若无有效标签则返回空列表
     */
    private List<OofEntry.FlDTO> parseTags(JsonArray arr) throws GsonSyntaxException {
        List<OofEntry.FlDTO> list = new ArrayList<>(arr.size());
        for (JsonElement e : arr) {
            if (!e.isJsonObject()) continue;
            JsonObject o = e.getAsJsonObject();
            OofEntry.FlDTO dto = new OofEntry.FlDTO();
            dto.setId(getString(o, "id"));
            dto.setName(getString(o, "name"));
            dto.setSort(getString(o, "sort"));
            dto.setColor(getString(o, "color"));
            dto.setUpdateTime(getIntFlexible(o, "update_time", null));
            dto.setCreateTime(getIntFlexible(o, "create_time", null));
            list.add(dto);
        }
        return list;
    }

    /**
     * 从JsonObject中获取整型字段，支持null和异常容错
     *
     * @param obj JsonObject对象
     * @param name 字段名
     * @param defaultVal 默认值（当字段不存在或异常时返回）
     * @return 字段对应的整型值，或默认值
     * @throws GsonSyntaxException 解析异常 
     */
    private static Integer getIntFlexible(JsonObject obj, String name, Integer defaultVal) throws GsonSyntaxException {
        return JsonObjectUtils.getInt(obj, name);
    }

    /**
     * 从JsonObject中获取字符串字段，支持null和异常容错
     *
     * @param obj JsonObject对象
     * @param name 字段名
     * @return 字段对应的字符串值，若不存在或异常则返回null
     * @throws GsonSyntaxException 解析异常
     */
    private static String getString(JsonObject obj, String name) throws GsonSyntaxException {
        return JsonObjectUtils.getStr(obj, name);
    }

    /**
     * 获取文件(夹)分页列表，支持备用接口
     * 
     * @param parentCid 父目录cid
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 响应JSON对象，如果请求失败返回null
     * @throws ValidateException 网络请求异常
     */
     private JsonObject fetchEntryPage(String parentCid, int offset, int limit) throws ValidateException {
         // 首先尝试主接口
         JsonObject response = tryFetchEntryPage(BASE_URL, parentCid, offset, limit);
        if (response != null) {
            return response;
        }
        
        // 如果主接口失败，尝试备用接口
//        System.out.println("主接口失败，尝试备用接口...");
         return tryFetchEntryPage(FALLBACK_URL, parentCid, offset, limit);
    }
    
    /**
     * 尝试从指定 URL 获取文件(夹)分页列表
     * 
     * @param baseUrl 基础URL
     * @param parentCid 父目录cid
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 响应JSON对象，如果请求失败返回null
     * @throws ValidateException 获取文件(夹)列表接口返回异常
     */
     private JsonObject tryFetchEntryPage(String baseUrl, String parentCid, int offset, int limit) throws ValidateException {
        // 构建请求URL
        String url = String.format("%s?aid=1&cid=%s&o=file_name&asc=0&offset=%d&show_dir=1&limit=%d&code=&scid=&snap=0&natsort=1&record_open_time=1&count_folders=1&type=&source=&format=json&fc_mix=0&star=&is_share=&suffix=&custom_order=",
                baseUrl, parentCid, offset, limit);
        
        // 发送请求
        MyCall call = http.get(url, null, Map.of("Cookie", cookie));
        JsonObject response;
        try {
            response = HttpApiUtils.executeForJsonObject(
                    call,
                    "获取文件(夹)列表",
                    ValidateException::new,
                    null
            );
        } catch (SocketTimeoutException e) {
            throw new ValidateException("获取文件(夹)列表接口调用超时",e);
        } catch (ConnectException e) {
            throw new ValidateException("获取文件(夹)列表接口连接超时",e);
        } catch (ValidateException e) {
            throw new ValidateException("获取文件(夹)列表接口返回异常",e);
        }
        
        try {
            // 检查错误码
            int errNo = JsonObjectUtils.getInt(response, "errNo", 0);
            if (errNo == 20130827) {
//                System.out.println("检测到错误码20130827，需要切换接口");
                return null; // 返回null表示需要尝试备用接口
            }

            // 检查其他错误
            String error = JsonObjectUtils.getStr(response, "error");
            if (!StringUtils.isEmpty(error)) {
//                System.err.println("API返回错误: " + error);
                return null;
            }
        } catch (GsonSyntaxException e) {
            throw new ValidateException("解析获取文件(夹)列表返回异常",e);
        }
        
        return response;
    }
    
    /**
     * 测试方法
     */
    public static void main(String[] args) {
        OofPathUtil finder = new OofPathUtil(
                new HttpHelper(null, "115"), "xxx"
        );
        
        try {
            // 测试查找根目录
            Optional<String> rootCid = finder.findCidByPath("");
            System.out.println("根目录cid: " + rootCid);
            
            // 测试获取根目录下的文件夹列表
            List<String> rootFolders = finder.listFolderNames("0");
            System.out.println("根目录下的文件夹: " + rootFolders);
            
            // 测试查找单级目录
            Optional<String> singleCid = finder.findCidByPath("云下载");
            System.out.println("云下载目录cid: " + singleCid);
            
            // 测试查找多级目录
            Optional<String> multiCid = finder.findCidByPath("云下载\\子目录");
            System.out.println("多级目录cid: " + multiCid);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 