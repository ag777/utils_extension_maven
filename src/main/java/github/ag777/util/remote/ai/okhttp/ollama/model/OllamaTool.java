package github.ag777.util.remote.ai.okhttp.ollama.model;

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
public class OllamaTool {

    private String type;
    private FunctionDTO function;

    public static OllamaTool of(String name, String description) {
        return new OllamaTool()
                .type("function")
                .function(new FunctionDTO()
                        .name(name)
                        .description(description));
    }

    public OllamaTool addParameters(String name, String type, String description, boolean required, List<Object> enumX) {
        if (function == null) {
            function = new FunctionDTO();
        }
        if (function.parameters == null) {
            function.parameters(
                    new FunctionDTO.ParametersDTO()
                            .type("object")
            );
        }

        if (function.parameters.properties == null) {
            function.parameters.properties(new HashMap<>(3));
        }
        Map<String, FunctionDTO.ParametersDTO.PropertiesDTO> functionMap = function.parameters.properties;
        functionMap.put(
                name,
                new FunctionDTO.ParametersDTO.PropertiesDTO()
                        .type(type)
                        .description(description)
                        .enumX(enumX)
        );
        if (required) {
            if (function.parameters.required == null) {
                function.parameters.required(new ArrayList<>(1));
            }
            function.parameters.required.add(name);
        }
        return this;
    }



    @NoArgsConstructor
    @Data
    public static class FunctionDTO {
        private String name;
        private String description;
        private ParametersDTO parameters;

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
