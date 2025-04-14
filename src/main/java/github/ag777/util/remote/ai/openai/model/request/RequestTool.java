package github.ag777.util.remote.ai.openai.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tool DTO
 * @author ag777
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true, fluent = true)
public class RequestTool {

    private String type;
    private FunctionDTO function;

    public static RequestTool of(String name, String description) {
        return new RequestTool()
                .type("function")
                .function(new FunctionDTO()
                        .name(name)
                        .description(description));
    }

    public RequestTool addParameter(String name, String type, String description, boolean required, List<Object> enumX) {
        if (function == null) {
            function = new FunctionDTO();
        }
        function.addParameter(name, type, description, required, enumX);
        return this;
    }



    @NoArgsConstructor
    @Data
    public static class FunctionDTO {
        private String name;
        private String description;
        private ParametersDTO parameters;

        public FunctionDTO addParameter(String name, String type, String description, boolean required, List<Object> enumX) {
            if (parameters == null) {
                parameters(
                        new ParametersDTO()
                                .type("object")
                );
            }

            if (parameters.properties == null) {
                parameters.properties(new HashMap<>(3));
            }
            Map<String, ParametersDTO.PropertiesDTO> functionMap = parameters.properties;
            functionMap.put(
                    name,
                    new ParametersDTO.PropertiesDTO()
                            .type(type)
                            .description(description)
                            .enumX(enumX)
            );
            if (required) {
                if (parameters.required == null) {
                    parameters.required(new ArrayList<>(1));
                }
                parameters.required.add(name);
            }
            return this;
        }

        @NoArgsConstructor
        @Data
        @Accessors(chain = true, fluent = true)
        public static class ParametersDTO {
            private String type;
            private Map<String, PropertiesDTO> properties;
            private List<String> required;

            @NoArgsConstructor
            @Data
            @Accessors(chain = true, fluent = true)
            public static class PropertiesDTO {
                private String type;
                private String description;
                private List<Object> enumX;
            }
        }
    }
}
