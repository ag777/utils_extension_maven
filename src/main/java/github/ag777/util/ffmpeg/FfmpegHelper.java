package github.ag777.util.ffmpeg;

import com.ag777.util.file.FileUtils;
import com.ag777.util.gson.GsonUtils;
import com.ag777.util.lang.IOUtils;
import com.ag777.util.lang.RegexUtils;
import com.ag777.util.lang.StringUtils;
import com.ag777.util.lang.collection.ListUtils;
import com.ag777.util.lang.collection.MapUtils;
import github.ag777.util.ffmpeg.model.FfmpegVideoOption;
import org.slf4j.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ffmpeg 命令行封装
 * See <a href="https://ffmpeg.org/download.html#build-windows">download page</a>
 * @author ag777＜ag777@vip.qq.com＞
 * @Date 2021/12/18 21:55
 */
public class FfmpegHelper {
    private static final String DIR_TEMP = System.getProperty("java.io.tmpdir");

    private Logger log;
    private boolean systemOutLog;  // 是否在控制台打印日志
    private final String ffmpegPath;  // ffmpeg.exe文件路径
    private final String ffprobePath; // ffprobe.exe文件路径

    public FfmpegHelper(File ffmpegExeFile) {
        this.ffmpegPath = ffmpegExeFile.getPath();
        // ffprobe.exe 在ffmpeg.exe的同级目录下
        this.ffprobePath = new File(ffmpegExeFile.getParent()+"/ffprobe.exe").getAbsolutePath();
    }

//    /**
//     * 有配置环境变量的情况下，只需要直接用名称调用工具就行
//     * 环境变量的加入方法: 配置ffmpeg/bin到PATH中
//     * 环境变量验证: 命令行ffmpeg -version看看返回
//     */
//    public FfmpegHelper() {
//        this.ffmpegPath = "ffmpeg";
//        this.ffprobePath = "ffprobe";
//    }

    public FfmpegHelper setLogger(Logger logger) {
        this.log = logger;
        return this;
    }

    public void setSystemOutLog(boolean systemOutLog) {
        this.systemOutLog = systemOutLog;
    }

    /**
     * 格式转换
     * @param inputFile 输入视频文件
     * @param outputFile 输出视频文件
     * @return 命令行是否执行成功
     * @throws IOException io异常
     */
    public boolean formatConvert(File inputFile, File outputFile, FfmpegVideoOption option) throws IOException {
        // ffmpeg -i input.avi output.mp4
        List<String> cmdList = ListUtils.of(
                ffmpegPath, "-i", inputFile.getAbsolutePath()
        );
        if (option != null) {
            // 平均码率
            if (option.getRate() != null) {
                cmdList.add("-b:v");
                cmdList.add(option.getRate()+"k");
                // -bufsize 用于设置码率控制缓冲器的大小，设置的好处是，让整体的码率更趋近于希望的值，减少波动。（简单来说，比如1 2的平均值是1.5， 1.49 1.51 也是1.5, 当然是第二种比较好）
                cmdList.add("-bufsize");
                cmdList.add(option.getRate()+"k");
            }
            if (option.getMinRate() != null) {
                cmdList.add("-minrate");
                cmdList.add(option.getMinRate()+"k");
            }
            if (option.getMaxRate() != null) {
                cmdList.add("-maxrate");
                cmdList.add(option.getMaxRate()+"k");
            }
            if (option.getVCodec() != null) {
                cmdList.add("-vcodec");
                cmdList.add(option.getVCodec());
            }
            if (option.getScaleWidth() > 0 || option.getScaleHeight() > 0) {
                cmdList.add("-vf");
                cmdList.add("scale="+option.getScaleWidth()+":"+option.getScaleHeight());
            }

        }
        cmdList.add(outputFile.getAbsolutePath());
        return exec(cmdList.toArray(new String[0]));
    }

    /**
     * 提取片段
     * See <a href="https://trac.ffmpeg.org/wiki/Seeking#Notes">api doc</a>
     * @param srcFile 原视频文件
     * @param destFile 目标视频文件
     * @param startTime 开始时间(00:00:00)
     * @param endTime 结束时间(00:00:00)
     * @return 是否成功
     * @throws IOException IO异常
     */
    public boolean cut(File srcFile, File destFile, String startTime, String endTime) throws IOException {
        // 如果没有指定开始时间，则从0秒开始
        if (startTime ==  null) {
            startTime = "00:00:00";
        }
        // 如果没有结束时间，则到视频末尾时间结束
        if (endTime == null) {
            endTime = getDurationStr(srcFile);
            debug("=========endtime: "+endTime);
        }
        // ffmpeg -i video.mp4 -ss 00:01:00 -to 00:02:00 -c copy cut.mp4
        // -accurate_seek 必须放在-i前，目前效果上看可有可无
        // avoid_negative_ts 1 目前效果来看可有可无
        String[] cmds = {ffmpegPath, "-accurate_seek", "-i", srcFile.getAbsolutePath(), "-ss",startTime,"-to", endTime, "-avoid_negative_ts","1", "-c", "copy", destFile.getAbsolutePath()};
        return exec(cmds);
    }

    public String cmdCut(File srcFile, File destFile, String startTime, String endTime) {
        return ffmpegPath+" -i "+srcFile.getAbsolutePath()+" -ss "+startTime+" -to "+endTime+" -c copy "+destFile.getAbsolutePath();
    }

    /**
     * 视频拼接(任意文件)
     * See <a href="https://trac.ffmpeg.org/wiki/Concatenate">api doc</a>
     * @param destFile 目标文件
     * @param option 转换配置
     * @param srcFiles 源文件
     * @return 是否拼接成功
     * @throws IOException io异常
     */
    public boolean concat(File destFile, FfmpegVideoOption option, File... srcFiles) throws IOException {
        if (srcFiles == null || srcFiles.length == 0) {
            return false;
        }
        if (option == null) {
            return concatSameFormat(destFile, srcFiles);
        }
        /*
        1.根据配置转为相同格式mp4
        2.调用同格式视频的拼接方法concat完成视频拼接
         */
        String tempDir = DIR_TEMP+StringUtils.uuid()+"/";
        new File(tempDir).mkdir();
        try {
            for (int i = 0; i < srcFiles.length; i++) {
                File srcFile = srcFiles[i];
                File tempFile = new File(tempDir+StringUtils.uuid()+".mp4");
                formatConvert(srcFile, tempFile, option);
                srcFiles[i] = tempFile;
            }
            return concatSameFormat(destFile, srcFiles);
        } finally {
            FileUtils.delete(tempDir);
        }

        
//        // 没有音频流时不对音频流进行操作
//        // 分辨率/格式不一致时报错: Input link in0:v0 parameters (size 1280x720, SAR 1:1) do not match the corresponding output link in0:v0 parameters (1920x1080, SAR 1:1)
//        // ffmpeg -i input1.mp4 -i input2.webm -i input3.mov \
//        // -filter_complex "[0:v:0][0:a:0][1:v:0][1:a:0][2:v:0][2:a:0]concat=n=3:v=1:a=1[outv][outa]" \
//        // -map "[outv]" -map "[outa]" output.mkv
//        List<String> cmdList = ListUtils.of(ffmpegPath);
//        for (File srcFile : srcFiles) {
//            cmdList.add("-i");
//            cmdList.add(srcFile.getAbsolutePath());
//        }
//
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < srcFiles.length; i++) {
//            sb.append("[").append(i).append(":v:0][").append(i).append(":a:0]");
//        }
//        sb.append("concat=n=").append(srcFiles.length).append(":v=1:a=1[outv][outa]");
//        cmdList.add("-filter_complex");
//        cmdList.add(sb.toString());
//        cmdList.add("-map");
//        cmdList.add("\"[outv]\"");
//        cmdList.add("-map");
//        cmdList.add("\"[outa]\"");
//        cmdList.add(destFile.getAbsolutePath());
//        return exec(cmdList.toArray(new String[0]));
    }

    public boolean concatSameFormat(File destFile, File... srcFiles) throws IOException {
        if (srcFiles == null || srcFiles.length == 0) {
            return false;
        }
        /*
        1. 写出txt文件
        2. 执行ffmpeg -f concat -safe 0 -i mylist.txt -c copy output
        */
        String txtPath = DIR_TEMP +StringUtils.uuid();
        debug("temp txt path: "+txtPath);
        try {
            Files.write(Paths.get(txtPath), Arrays.stream(srcFiles).map(file->"file '"+file.getAbsolutePath()+"'").collect(Collectors.toList()));
            String[] cmds = {ffmpegPath, "-f", "concat", "-safe", "0", "-i", txtPath, "-c", "copy", destFile.getAbsolutePath()};
            return exec(cmds);
        } finally {
            Files.delete(Paths.get(txtPath));
        }
    }

    public boolean cutAndConcat(File srcFile, File destFile, String startTime, String endTime, String... otherTimes) throws IOException {
        String tempDirPath = DIR_TEMP+StringUtils.uuid()+"/";
        File tempDir = new File(tempDirPath);
        tempDir.mkdir();
        String extension = StringUtils.emptyIfNull(RegexUtils.find(srcFile.getName(), "\\..+$"));
        debug("extension: "+extension);
        try {
            // 如果没有指定开始时间，则从0秒开始
            if (startTime ==  null) {
                startTime = "00:00:00";
            }
            // 如果没有结束时间，则到视频末尾时间结束
            if (endTime == null) {
                endTime = getDurationStr(srcFile);
                debug("=========endtime: "+endTime);
            }

            // 剪切出第一个文件
            File firstFile = new File(tempDirPath+StringUtils.uuid()+extension);
            if (!cut(srcFile, firstFile, startTime, endTime)) {
                return false;
            }
            debug("output: "+firstFile.getAbsolutePath());
            if (otherTimes == null || otherTimes.length==0) {
                // 只有一个片段
                FileUtils.move(firstFile.getAbsolutePath(), destFile.getAbsolutePath());
                return true;
            } else {
                List<File> files = ListUtils.of(firstFile);
                // 多个片段
                for (int i = 0; i < otherTimes.length; ) {
                    startTime = otherTimes[i];
                    try {
                        endTime = otherTimes[i+1];
                    } catch (ArrayIndexOutOfBoundsException e) {
                        // 没有指定结束时间，则结束时间定为视频的结束时间
                        endTime = getDurationStr(srcFile);
                        debug("=========endtime: "+endTime);
                    }
                    File tempFile = new File(tempDirPath+StringUtils.uuid()+extension);
                    if(!cut(srcFile, tempFile, startTime, endTime)) {
                        return false;
                    }
                    files.add(tempFile);
                    debug("output: "+tempFile.getAbsolutePath());
                    i+=2;
                }
                return concatSameFormat(destFile, files.toArray(new File[0]));
            }
        } finally {
            FileUtils.delete(tempDir);
        }
    }

    /**
     *
     * @param file 文件
     * @return 流列表
     * @throws IOException IO异常
     */
    public List<Map<String, Object>> streams(File file) throws IOException {
        Map<String, Object> infoMap = getInfo(file);
        return MapUtils.get(infoMap, "streams");
    }

    /**
     * @param file 视频文件
     * @return 视频长度 HH:mm:ss.SSS
     * @throws IOException io异常
     */
    public Double getDuration(File file) throws IOException {
        Map<String, Object> infoMap = getInfo(file);
        Map<String, Object> formatMap = MapUtils.get(infoMap, "format");
        return MapUtils.getDouble(formatMap, "duration");
    }

    /**
     *
     * @param file 文件
     * @return 时长: 00:01:39.267
     * @throws IOException IO异常
     */
    public String getDurationStr(File file) throws IOException {
        Double duration = getDuration(file);
        if (duration != null) {
            LocalTime t = LocalTime.ofNanoOfDay((long) (99.267000*1000000000));
            return t.toString();
        }
        return null;
    }

    /**
     *
     * @param file 视频文件
     * @return {@code {
     *   "streams": [
     *     {
     *       "index": 0,
     *       "codec_name": "mpeg4",
     *       "codec_long_name": "MPEG-4 part 2",
     *       "profile": "Simple Profile",
     *       "codec_type": "video",
     *       "codec_tag_string": "FMP4",
     *       "codec_tag": "0x34504d46",
     *       "width": 1280,
     *       "height": 720,
     *       "coded_width": 1280,
     *       "coded_height": 720,
     *       "closed_captions": 0,
     *       "film_grain": 0,
     *       "has_b_frames": 0,
     *       "sample_aspect_ratio": "1:1",
     *       "display_aspect_ratio": "16:9",
     *       "pix_fmt": "yuv420p",
     *       "level": 1,
     *       "chroma_location": "left",
     *       "refs": 1,
     *       "quarter_sample": "false",
     *       "divx_packed": "false",
     *       "r_frame_rate": "25/1",
     *       "avg_frame_rate": "25/1",
     *       "time_base": "1/25",
     *       "start_pts": 0,
     *       "start_time": "0.000000",
     *       "duration_ts": 74,
     *       "duration": "2.960000",
     *       "bit_rate": "923225",
     *       "nb_frames": "74",
     *       "extradata_size": 47,
     *       "disposition": {
     *         "default": 0,
     *         "dub": 0,
     *         "original": 0,
     *         "comment": 0,
     *         "lyrics": 0,
     *         "karaoke": 0,
     *         "forced": 0,
     *         "hearing_impaired": 0,
     *         "visual_impaired": 0,
     *         "clean_effects": 0,
     *         "attached_pic": 0,
     *         "timed_thumbnails": 0,
     *         "captions": 0,
     *         "descriptions": 0,
     *         "metadata": 0,
     *         "dependent": 0,
     *         "still_image": 0
     *       }
     *     },
     *     {
     *       "index": 1,
     *       "codec_name": "mp3",
     *       "codec_long_name": "MP3 (MPEG audio layer 3)",
     *       "codec_type": "audio",
     *       "codec_tag_string": "U[0][0][0]",
     *       "codec_tag": "0x0055",
     *       "sample_fmt": "fltp",
     *       "sample_rate": "48000",
     *       "channels": 2,
     *       "channel_layout": "stereo",
     *       "bits_per_sample": 0,
     *       "r_frame_rate": "0/0",
     *       "avg_frame_rate": "0/0",
     *       "time_base": "3/125",
     *       "start_pts": 0,
     *       "start_time": "0.000000",
     *       "duration_ts": 127,
     *       "duration": "3.048000",
     *       "bit_rate": "129016",
     *       "nb_frames": "127",
     *       "extradata_size": 12,
     *       "disposition": {
     *         "default": 0,
     *         "dub": 0,
     *         "original": 0,
     *         "comment": 0,
     *         "lyrics": 0,
     *         "karaoke": 0,
     *         "forced": 0,
     *         "hearing_impaired": 0,
     *         "visual_impaired": 0,
     *         "clean_effects": 0,
     *         "attached_pic": 0,
     *         "timed_thumbnails": 0,
     *         "captions": 0,
     *         "descriptions": 0,
     *         "metadata": 0,
     *         "dependent": 0,
     *         "still_image": 0
     *       }
     *     }
     *   ],
     *   "format": {
     *     "filename": "D:\\temp\\程序测试\\ffmpeg_output\\1.avi",
     *     "nb_streams": 2,
     *     "nb_programs": 0,
     *     "format_name": "avi",
     *     "format_long_name": "AVI (Audio Video Interleaved)",
     *     "start_time": "0.000000",
     *     "duration": "3.048000",
     *     "size": "400604",
     *     "bit_rate": "1051454",
     *     "probe_score": 100,
     *     "tags": {
     *       "software": "Lavf59.10.100"
     *     }
     *   }
     * }}
     * @throws IOException 异常
     */
    public Map<String, Object> getInfo(File file) throws IOException {
        // ffprobe -v quiet -show_format -show_streams -print_format json 1.mp4
        String text = readText(
                new String[]{ffprobePath,"-v","quiet","-show_format","-show_streams","-print_format","json",file.getAbsolutePath()},
                StandardCharsets.UTF_8);
        return GsonUtils.get().toMap(text);
    }

    private String readText(String[] cmds, Charset charset) throws IOException {
        debug("cmd: "+String.join( " ", cmds));
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(cmds);
        pb.redirectErrorStream(true);
        Process p = pb.start();
        p.getOutputStream().close();
        return new String(IOUtils.readBytes(p.getInputStream()), charset);
    }



    /**
     * 执行命令
     * @param cmds 命令
     * @return 是否成功
     * @throws IOException IO异常
     */
    private boolean exec(String[] cmds) throws IOException {
        debug("cmd: "+String.join( " ", cmds));
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(cmds);
        pb.redirectErrorStream(true);
        Process p = pb.start();
        BufferedInputStream in = null;
        OutputStream out = null;
        try {
            in = new BufferedInputStream(p.getInputStream());
            out = p.getOutputStream();
            byte[] buff = new byte[1024];

            while(true) {
                int n = in.read(buff);
                if (n == -1) {
                    break;
                }
                String text = new String(buff, 0, n, StandardCharsets.UTF_8);
                debug(text);
                if (text.endsWith("Overwrite? [y/N] ")) {
                    out.write("y\n".getBytes(StandardCharsets.UTF_8));
                    out.flush();
                    info("intput y===");
                }
            }
        } finally {
            IOUtils.close(out, in);
            p.destroy();
        }
        return p.exitValue() == 0;

    }

    private void info(String msg) {
        if (log != null) {
            log.info(msg);
        }
        if (systemOutLog) {
            System.out.println(msg);
        }
    }

    private void debug(String msg) {
        if (log != null) {
            log.debug(msg);
        }
        if (systemOutLog) {
            System.out.println(msg);
        }
    }

    private void error(String msg) {
        if (log != null) {
            log.error(msg);
        }
        if (systemOutLog) {
            System.err.println(msg);
        }
    }

    public static void main(String[] args) {
        FfmpegHelper u = new FfmpegHelper(new File("D:\\软件\\制作工具\\ffmpeg-2021-12-17-git-b780b6db64-full_build\\bin\\ffmpeg.exe"));
        u.setSystemOutLog(true);
        // 提取
//        u.cut(
//                new File("G:\\in.mp4"),
//                new File("D:\\out.mp4"),
//                "00:01:51",
//                "00:06:50"
//        );
        // 拼接
//        u.concat(
//            new File("D:\\\\out.mp4"),
//            new FfmpegVideoOption().setScaleWidth(720).setScaleHeight(576),
//            new File("G:\\in1.mp4"),
//            new File("G:\\in2.avi")
//            );
        // 拼接同类视频
//        u.concatSameFormat(
//                new File("D:\\out.mp4"),
//                new File("D:\\in1.mp4"),
//                new File("D:\\in2.mp4")
//        );
        // 获取视频信息
//        u.getInfo(new File(
//                "G:\\1.mp4"
//        ));
        // 提取视频片段并拼接(用于删除部分片段,比如广告)
//        u.cutAndConcat(
//                new File("G:\\in.mp4"),
//                new File("D:\\out.mp4"),
//                "00:00:42",
//                null
//        );
    }

}
