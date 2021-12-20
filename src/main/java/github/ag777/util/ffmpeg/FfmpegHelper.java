package github.ag777.util.ffmpeg;

import com.ag777.util.file.FileUtils;
import com.ag777.util.gson.GsonUtils;
import com.ag777.util.lang.Console;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private String ffmpegPath;  // ffmpeg.exe文件路径
    private String ffprobePath; // ffprobe.exe文件路径

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
            endTime = getDuration(srcFile);
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
                endTime = getDuration(srcFile);
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
                        endTime = getDuration(srcFile);
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
     * @param file 视频文件
     * @return 视频长度 HH:mm:ss.SSS
     * @throws IOException io异常
     */
    public String getDuration(File file) throws IOException {
        Map<String, Object> infoMap = getInfo(file);
        return MapUtils.get(infoMap, "duration");
    }

    /**
     *
     * @param file 视频文件
     * @return {
     *   "major_brand": "mp42",
     *   "minor_version": "0",
     *   "compatible_brands": "mp42isomavc1",
     *   "creation_time": "2017-07-15T05:13:36.000000Z",
     *   "duration": "02:21:36.94",
     *   "start": 0.0
     * }
     * @throws IOException 异常
     */
    public Map<String, Object> getInfo(File file) throws IOException {
        Map<String, Object> infoMap = new LinkedHashMap<>();
        /*
        ffprobe version 2021-12-17-git-b780b6db64-full_build-www.gyan.dev Copyright (c) 2007-2021 the FFmpeg developers
          built with gcc 11.2.0 (Rev2, Built by MSYS2 project)
          configuration: --enable-gpl --enable-version3 --enable-static --disable-w32threads --disable-autodetect --enable-fontconfig --enable-iconv --enable-gnutls --enable-libxml2 --enable-gmp --enable-lzma --enable-libsnappy --enable-zlib --enable-librist --enable-libsrt --enable-libssh --enable-libzmq --enable-avisynth --enable-libbluray --enable-libcaca --enable-sdl2 --enable-libdav1d --enable-libdavs2 --enable-libuavs3d --enable-libzvbi --enable-librav1e --enable-libsvtav1 --enable-libwebp --enable-libx264 --enable-libx265 --enable-libxavs2 --enable-libxvid --enable-libaom --enable-libopenjpeg --enable-libvpx --enable-libass --enable-frei0r --enable-libfreetype --enable-libfribidi --enable-libvidstab --enable-libvmaf --enable-libzimg --enable-amf --enable-cuda-llvm --enable-cuvid --enable-ffnvcodec --enable-nvdec --enable-nvenc --enable-d3d11va --enable-dxva2 --enable-libmfx --enable-libshaderc --enable-vulkan --enable-libplacebo --enable-opencl --enable-libcdio --enable-libgme --enable-libmodplug --enable-libopenmpt --enable-libopencore-amrwb --enable-libmp3lame --enable-libshine --enable-libtheora --enable-libtwolame --enable-libvo-amrwbenc --enable-libilbc --enable-libgsm --enable-libopencore-amrnb --enable-libopus --enable-libspeex --enable-libvorbis --enable-ladspa --enable-libbs2b --enable-libflite --enable-libmysofa --enable-librubberband --enable-libsoxr --enable-chromaprint
          libavutil      57. 11.100 / 57. 11.100
          libavcodec     59. 14.100 / 59. 14.100
          libavformat    59. 10.100 / 59. 10.100
          libavdevice    59.  0.101 / 59.  0.101
          libavfilter     8. 20.100 /  8. 20.100
          libswscale      6.  1.101 /  6.  1.101
          libswresample   4.  0.100 /  4.  0.100
          libpostproc    56.  0.100 / 56.  0.100
        Input #0, mov,mp4,m4a,3gp,3g2,mj2, from 'G:\RECYCLED\UDrives.{25336920-03F9-11CF-8FD0-00AA00686F13}\新建文件夹\三次元\あやみ旬果\ABP-616=あやみ旬果.mp4':
          Metadata:
            major_brand     : mp42
            minor_version   : 0
            compatible_brands: mp42isomavc1
            creation_time   : 2017-07-15T05:13:36.000000Z
          Duration: 02:21:36.94, start: 0.000000, bitrate: 1317 kb/s
          Stream #0:0[0x1](und): Audio: aac (LC) (mp4a / 0x6134706D), 48000 Hz, stereo, fltp, 125 kb/s (default)
            Metadata:
              creation_time   : 2017-07-15T05:13:36.000000Z
              handler_name    : Sound Media Handler
              vendor_id       : [0][0][0][0]
          Stream #0:1[0x2](und): Video: h264 (High) (avc1 / 0x31637661), yuv420p(tv, bt709, progressive), 856x480 [SAR 1:1 DAR 107:60], 1188 kb/s, 29.97 fps, 29.97 tbr, 60k tbn (default)
            Metadata:
              creation_time   : 2017-07-15T05:13:36.000000Z
              handler_name    : Video Media Handler
              vendor_id       : [0][0][0][0]
              encoder         : AVC Coding
         */
        List<String> lines = readLines(new String[]{ffprobePath, file.getAbsolutePath()});
        Pattern pFirst = Pattern.compile("^\\s{2}(\\S*):.*");
        Pattern pPair = Pattern.compile("([^\\s]*)\\s*:\\s*(\\S.*)");
        // Stream #0:0[0x1](eng): Video: h264 (High) (avc1 / 0x31637661), yuv420p(progressive), 1920x1080 [SAR 1:1 DAR 16:9], 1164 kb/s, 25 fps, 25 tbr, 25 tbn
        /*
        1: format h264
        2: width 1920
        3: height 1080
        4: rate 1164
        5: tbr 25
        6: tbn 25
         */
        Pattern pStreamVideo = Pattern.compile("Stream\\s+#\\d:\\d(?:.+)?: Video:\\s+(\\S*)\\s+.+,\\s+(\\d+)x(\\d+)[^,]*,\\s+(\\d+)\\s*kb/s,\\s+(\\d+)\\s+fps,\\s+(\\d+)\\s+tbr,\\s+(\\d+)\\s+tbn");
        /*
        1: format aac
        2: sampling_rate 音频采样率 48000
        3: 不知道是啥 125 kb/s
         */
        Pattern pStreamAudio = Pattern.compile("Stream\\s+#\\d:\\d(?:.+)?:\\s+Audio:\\s+(\\S*).+,\\s+(\\d+)\\s+Hz,.+,.+,\\s+(\\d+)\\s+kb/s");
        String group = null;
        for (String line : lines) {
            debug(line);
            Matcher m = pFirst.matcher(line);
            if (m.matches()) {
                group = m.group(1);
                if ("Duration".equals(group)) {
                    // Duration: 02:21:36.94, start: 0.000000, bitrate: 1317 kb/s
                    Pattern pDuration = Pattern.compile("Duration:\\s*(.+),\\s*start:\\s*(.+),\\s*bitrate:\\s*(.+)\\s*kb/s");
                    m = pDuration.matcher(line);
                    if(m.find()) {
                        infoMap.put("duration", m.group(1));
                        infoMap.put("start", StringUtils.toFloat(m.group(2)));
                        infoMap.put("bitrate", StringUtils.toInt(m.group(3)));
                    }
                }
            } else if (group != null) {
                if ("Metadata".equals(group)) {
                    m = pPair.matcher(line);
                    if (m.find()) {
                        infoMap.put(m.group(1), m.group(2));
                    }
                } else if ("Duration".equals(group)){
                    if (!infoMap.containsKey("video")) {
                        m = pStreamVideo.matcher(line);
                        if (m.find()) {
                            infoMap.put("video", MapUtils.putAll(
                                    new LinkedHashMap<>(6),
                                    "format", m.group(1),
                                    "width", m.group(2),
                                    "height", m.group(3),
                                    "rate", m.group(4),
                                    "tbr", m.group(5),
                                    "tbn", m.group(6)
                            ));
                            continue;
                        }
                    }
                    if (!infoMap.containsKey("audio")) {
                        m = pStreamAudio.matcher(line);
                        if (m.find()) {
                            infoMap.put("audio", MapUtils.putAll(
                                    new LinkedHashMap<>(2),
                                    "format", m.group(1),
                                    "sampling_rate", m.group(2)
                            ));
//                            continue;
                        }
                    }
                }
            }
        }
        return infoMap;
    }

    private List<String> readLines(String[] cmds) throws IOException {
        debug("cmd: "+String.join( " ", cmds));
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(cmds);
        pb.redirectErrorStream(true);
        Process p = pb.start();
        p.getOutputStream().close();
        BufferedInputStream in;
        try {
            in = new BufferedInputStream(p.getInputStream());
            return IOUtils.readLines(in, StandardCharsets.UTF_8);
        } finally {
            p.destroy();
        }
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
//        u.cut(
//                new File("G:\\in.mp4"),
//                new File("D:\\out.mp4"),
//                "00:01:51",
//                "00:06:50"
//        );
//        u.concatSameFormat(
//                new File("D:\\out.mp4"),
//                new File("D:\\in1.mp4"),
//                new File("D:\\in2.mp4")
//        );
//        u.getInfo(new File(
//                "G:\\1.mp4"
//        ));
//
//        u.cutAndConcat(
//                new File("G:\\in.mp4"),
//                new File("D:\\out.mp4"),
//                "00:00:42",
//                null
//        );
    }

}
