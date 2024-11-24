package github.ag777.util.remote.ollama.okhttp.model;

import com.ag777.util.gson.GsonUtils;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true, fluent = true)
@NoArgsConstructor
public class OllamaOptions {
    @SerializedName("numa")
    private Boolean useNUMA;
    @SerializedName("num_ctx")
    private Integer numCtx;
    @SerializedName("num_batch")
    private Integer numBatch;
    @SerializedName("num_gpu")
    private Integer numGPU;
    @SerializedName("main_gpu")
    private Integer mainGPU;
    @SerializedName("low_vram")
    private Boolean lowVRAM;
    @SerializedName("f16_kv")
    private Boolean f16KV;
    @SerializedName("logits_all")
    private Boolean logitsAll;
    @SerializedName("vocab_only")
    private Boolean vocabOnly;
    @SerializedName("use_mmap")
    private Boolean useMMap;
    @SerializedName("use_mlock")
    private Boolean useMLock;
    @SerializedName("num_thread")
    private Integer numThread;
    @SerializedName("num_keep")
    private Integer numKeep;
    @SerializedName("seed")
    private Integer seed;
    @SerializedName("num_predict")
    private Integer numPredict;
    @SerializedName("top_k")
    private Integer topK;
    @SerializedName("top_p")
    private Float topP;
    @SerializedName("tfs_z")
    private Float tfsZ;
    @SerializedName("typical_p")
    private Float typicalP;
    @SerializedName("repeat_last_n")
    private Integer repeatLastN;
    @SerializedName("temperature")
    private Float temperature;
    @SerializedName("repeat_penalty")
    private Float repeatPenalty;
    @SerializedName("presence_penalty")
    private Float presencePenalty;
    @SerializedName("frequency_penalty")
    private Float frequencyPenalty;
    @SerializedName("mirostat")
    private Integer mirostat;
    @SerializedName("mirostat_tau")
    private Float mirostatTau;
    @SerializedName("mirostat_eta")
    private Float mirostatEta;
    @SerializedName("penalize_newline")
    private Boolean penalizeNewline;
    @SerializedName("stop")
    private List<String> stop;

    public static OllamaOptions newInstance() {
        return new OllamaOptions();
    }

    public static void main(String[] args) {
        System.out.println(new OllamaOptions().temperature(11f).toMap());
    }

    public Map<String, Object> toMap() {
        String json = GsonUtils.get().toJson(this);
        return GsonUtils.get().toMap(json);
    }
}
