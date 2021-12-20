package github.ag777.util.ffmpeg.model;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @Description 视频转换配置
 * @Date 2021/12/20 8:56
 */
public class FfmpegVideoOption {
    public static final String VCODEC_H264 = "h264";
    public static final String VCODEC_MPEG4 = "mpeg4";

    private Integer rate;   // 平均码率, 单位k
    private Integer minRate;    // 最小码率, 单位k
    private Integer maxRate;    // 最大码率, 单位k
    private String vCodec;  // 视频编码
    private int scaleWidth;  // 视频宽度, -1为保留原始宽高比
    private int scaleHeight;    // 视频高度, -1为保留原始宽高比

    public FfmpegVideoOption() {
        scaleWidth = -1;
        scaleHeight = -1;
    }

    public Integer getRate() {
        return rate;
    }

    public FfmpegVideoOption setRate(Integer rate) {
        this.rate = rate;
        return this;
    }

    public Integer getMinRate() {
        return minRate;
    }

    public FfmpegVideoOption setMinRate(Integer minRate) {
        this.minRate = minRate;
        return this;
    }

    public Integer getMaxRate() {
        return maxRate;
    }

    public FfmpegVideoOption setMaxRate(Integer maxRate) {
        this.maxRate = maxRate;
        return this;
    }

    public String getVCodec() {
        return vCodec;
    }

    public FfmpegVideoOption setVCodec(String vCodec) {
        this.vCodec = vCodec;
        return this;
    }

    public int getScaleWidth() {
        return scaleWidth;
    }

    public FfmpegVideoOption setScaleWidth(int scaleWidth) {
        this.scaleWidth = scaleWidth;
        return this;
    }

    public int getScaleHeight() {
        return scaleHeight;
    }

    public FfmpegVideoOption setScaleHeight(int scaleHeight) {
        this.scaleHeight = scaleHeight;
        return this;
    }
}
