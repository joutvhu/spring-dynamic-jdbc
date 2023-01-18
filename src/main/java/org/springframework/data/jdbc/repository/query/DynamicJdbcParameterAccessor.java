package org.springframework.data.jdbc.repository.query;

import org.springframework.data.relational.repository.query.RelationalParameterAccessor;
import org.springframework.data.relational.repository.query.RelationalParametersParameterAccessor;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.QueryMethod;

import java.util.HashMap;
import java.util.Map;

public class DynamicJdbcParameterAccessor extends RelationalParametersParameterAccessor {
    private RelationalParameterAccessor accessor;

    /**
     * Creates a new {@link RelationalParametersParameterAccessor}.
     */
    private DynamicJdbcParameterAccessor(QueryMethod method, RelationalParameterAccessor accessor) {
        super(method, accessor.getValues());
        this.accessor = accessor;
    }

    public static DynamicJdbcParameterAccessor of(QueryMethod method, RelationalParameterAccessor accessor) {
        return new DynamicJdbcParameterAccessor(method, accessor);
    }

    /**
     * Get map param with value
     *
     * @return a map
     */
    public Map<String, Object> getParamModel() {
        if (this.accessor != null)
            return getParamModel(this.accessor);
        return getParamModel(this);
    }

    private Map<String, Object> getParamModel(RelationalParameterAccessor accessor) {
        Map<String, Object> result = new HashMap<>();
        Parameters<?, ?> parameters = accessor.getBindableParameters();
        Object[] values = accessor.getValues();
        parameters.forEach(parameter -> {
            Object value = values[parameter.getIndex()];
            if (value != null && parameter.isBindable()) {
                String key = parameter.getName().orElse(String.valueOf(parameter.getIndex()));
                result.put(key, value);
            }
        });
        return result;
    }
}
