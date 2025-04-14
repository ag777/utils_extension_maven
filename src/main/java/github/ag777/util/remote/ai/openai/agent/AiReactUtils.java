package github.ag777.util.remote.ai.openai.agent;

import com.ag777.util.gson.GsonUtils;
import com.ag777.util.gson.JsonObjectUtils;
import com.ag777.util.lang.StringUtils;
import com.ag777.util.lang.exception.model.JsonSyntaxException;
import com.ag777.util.lang.exception.model.ValidateException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import github.ag777.util.remote.ai.openai.agent.model.*;
import github.ag777.util.remote.ai.openai.agent.util.AiFunctionResultUtils;
import github.ag777.util.remote.ai.openai.model.AiMessage;
import github.ag777.util.remote.ai.openai.model.AiReply;
import github.ag777.util.remote.ai.openai.model.AiTool;
import github.ag777.util.remote.ai.openai.model.request.RequestTool;
import github.ag777.util.remote.ai.openai.openai.OpenaiApiClient;
import github.ag777.util.remote.ai.openai.openai.request.OpenaiRequestChat;
import github.ag777.util.remote.ai.openai.util.AiReplyUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 大模型agent工具
 */
public class AiReactUtils {

    private static final String PROMPT_SYSTEM = """
          Respond to the human as helpfully and accurately as possible.
          You have access to the following tools:

          %s
          
          Use a json blob to specify a tool by providing an action key (tool name) and an action_input key (tool input).
          Valid "action" values: "Final Answer" or localtime_to_timestamp, GetShopPosition, dataset_4215bd09_beff_45e7_9bf7_7f822b251897, dataset_8f00e603_3b49_457a_96f2_be39cb4e2d37

          Provide only ONE action per $JSON_BLOB, as shown:

          ```
          {
            "action": $TOOL_NAME,
            "action_input": $ACTION_INPUT
          }
          ```

          Follow this format:

          Question: input question to answer
          Thought: consider previous and subsequent steps
          Action:
          ```
          $JSON_BLOB
          ```
          Observation: action result
          ... (repeat Thought/Action/Observation N times)
          Thought: I know what to respond
          Action:
          ```
          {
            "action": "Final Answer",
            "action_input": "Final response to human"
          }
          ```

          Begin! Reminder to ALWAYS respond with a valid json blob of a single action. Use tools if necessary. Respond directly if appropriate. Format is Action:```$JSON_BLOB```then Observation:.
          """;

    private static final String PROMPT_USER = """
            Thought: <think>%s</think>Action:
            
            Action: %s
            
            Observation: %s
            """;


    public static AiAgentReply exec(OpenaiApiClient client, String modelName, List<AiMessage> histories, String input, List<RequestTool.FunctionDTO> tools, ToolCaller toolCaller, OnAgentMsg onMessage) throws ValidateException, IOException, InterruptedException {
        List<AiMessage> agentHistories = new ArrayList<>((histories==null?0:histories.size())+20);
        agentHistories.add(AiMessage.system(String.format(PROMPT_SYSTEM, GsonUtils.get().toJson(tools))));
        if (histories != null) {
            agentHistories.addAll(histories);
        }
        agentHistories.add(AiMessage.user(input));

        return exec(client, modelName, agentHistories, toolCaller, "", onMessage, 1);
    }

    private static AiAgentReply exec(OpenaiApiClient client, String modelName, List<AiMessage> histories, ToolCaller toolCaller, String reactPrompt, OnAgentMsg onMessage, int reactTimes) throws ValidateException, IOException, InterruptedException {
        AiAgentReply reply = callAi(client, modelName, histories, reactPrompt, onMessage);
        if (reply.getTool() == null) {
            return reply;
        }
        if (onMessage != null) {
            onMessage.accept(AgentStepInfo.toolStart(reply.getTool()));
        }

        ToolReply toolReply = toolCaller.call(reply.getTool());

        if (onMessage != null) {
            onMessage.accept(AgentStepInfo.toolEnd(reply.getTool(), toolReply));
        }
        reactPrompt+=
                String.format(
                        PROMPT_USER,
                        StringUtils.emptyIfNull(reply.getReply().getThink()),
                        StringUtils.emptyIfNull(reply.getReply().getContent()),
                        GsonUtils.get().toJson(toolReply)
                )+"\n";

        return exec(client, modelName, histories, toolCaller, reactPrompt, onMessage, reactTimes+1);
    }

    private static AiAgentReply callAi(OpenaiApiClient client, String modelName, List<AiMessage> histories, String reactPrompt, OnAgentMsg onMessage) throws ValidateException, IOException, InterruptedException {
        List<AiMessage> newHistories;
        if (StringUtils.isEmpty(reactPrompt)) {
            newHistories = histories;
        } else {
            newHistories = new ArrayList<>(histories.size()+2);
            newHistories.addAll(histories);
            newHistories.add(AiMessage.system(reactPrompt));
            newHistories.add(AiMessage.user("continue"));
        }

        StringBuilder replyMsg = new StringBuilder();
        client.chatStream(
                OpenaiRequestChat.of(modelName)
                        .messages(newHistories)
                        .stop("Observation"),
                (msg, tool, responseChatUtil)-> {
                    replyMsg.append(msg);
                    if (!StringUtils.isEmpty(msg) && onMessage != null) {
                        onMessage.accept(AgentStepInfo.message(msg));
                    }
                }
        );
        AiReply reply = AiReplyUtils.handle(replyMsg.toString());
        String json = AiFunctionResultUtils.getJson(reply.getContent());
        try {
            JsonObject jo = GsonUtils.toJsonObjectWithException(json);
            String functionName = JsonObjectUtils.getStr(jo, "action");
            if (functionName == null) {
                throw new ValidateException("大模型未返回函数名:"+json);
            }
            JsonElement actionInput = JsonObjectUtils.get(jo, "action_input");
            if ("Final Answer".equals(functionName)) {
                return new AiAgentReply(reactPrompt, new AiReply(null, actionInput != null ? actionInput.getAsString() : ""), null);
            } else {
                Map<String, Object> params = actionInput==null? Collections.emptyMap():GsonUtils.get().toMapWithException(actionInput);
                return new AiAgentReply(reactPrompt, reply, new AiTool(functionName, params));
            }
        } catch (JsonSyntaxException | IllegalStateException e) {
            return new AiAgentReply(reactPrompt, reply, null);
//            throw new ValidateException("大模型返回json格式有误:"+json,e);
        }
    }

}
