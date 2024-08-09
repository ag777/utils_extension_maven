package github.ag777.util.file.everything.model;

/**
 * dwRequestFlags 枚举
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/8/9 下午3:55
 */
public class DwRequestFlags {
    public static final int EVERYTHING_REQUEST_FILE_NAME=0x00000001;
    public static final int EVERYTHING_REQUEST_PATH=0x00000002;
    /** EVERYTHING_REQUEST_FILE_NAME | EVERYTHING_REQUEST_PATH */
    public static final int EVERYTHING_DEFAULT=0x00000003;
    public static final int EVERYTHING_REQUEST_FULL_PATH_AND_FILE_NAME=0x00000004;
    public static final int EVERYTHING_REQUEST_EXTENSION=0x00000008;
    public static final int EVERYTHING_REQUEST_SIZE=0x00000010;
    public static final int EVERYTHING_REQUEST_DATE_CREATED=0x00000020;
    public static final int EVERYTHING_REQUEST_DATE_MODIFIED=0x00000040;
    public static final int EVERYTHING_REQUEST_DATE_ACCESSED=0x00000080;
    public static final int EVERYTHING_REQUEST_ATTRIBUTES=0x00000100;
    public static final int EVERYTHING_REQUEST_FILE_LIST_FILE_NAME=0x00000200;
    public static final int EVERYTHING_REQUEST_RUN_COUNT=0x00000400;
    public static final int EVERYTHING_REQUEST_DATE_RUN=0x00000800;
    public static final int EVERYTHING_REQUEST_DATE_RECENTLY_CHANGED=0x00001000;
    public static final int EVERYTHING_REQUEST_HIGHLIGHTED_FILE_NAME=0x00002000;
    public static final int EVERYTHING_REQUEST_HIGHLIGHTED_PATH=0x00004000;
    public static final int EVERYTHING_REQUEST_HIGHLIGHTED_FULL_PATH_AND_FILE_NAME=0x00008000;
}
