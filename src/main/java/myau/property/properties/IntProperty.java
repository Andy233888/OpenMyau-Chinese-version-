package myau.property.properties;

import com.google.gson.JsonObject;
import myau.property.Property;

import java.util.function.BooleanSupplier;

public class IntProperty extends Property<Integer> {
    private final Integer minimum;
    private final Integer maximum;

    public IntProperty(String name, Integer value, Integer minimum, Integer maximum) {
        this(name, value, minimum, maximum, null);
    }

    public IntProperty(
            String name, Integer value, Integer minimum, Integer maximum, BooleanSupplier check
    ) {
        super(name, value, v -> v >= minimum && v <= maximum, check);
        this.minimum = minimum;
        this.maximum = maximum;
    }

    /** configName 用于配置文件（英文键），name 用于 UI 显示 */
    public IntProperty(String configName, String name, Integer value, Integer minimum, Integer maximum) {
        this(configName, name, value, minimum, maximum, null);
    }

    /** configName 用于配置文件（英文键），name 用于 UI 显示 */
    public IntProperty(
            String configName, String name, Integer value, Integer minimum, Integer maximum, BooleanSupplier check
    ) {
        super(configName, name, value, v -> v >= minimum && v <= maximum, check);
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public IntProperty(String string, int i, Object object) {
        this(string, i, Integer.MIN_VALUE, Integer.MAX_VALUE, null);
        //TODO Auto-generated constructor stub
    }

    @Override
    public String getValuePrompt() {
        return String.format("%d-%d", this.minimum, this.maximum);
    }

    @Override
    public String formatValue() {
        return String.format("&e%s", this.getValue());
    }

    @Override
    public boolean parseString(String string) {
        return this.setValue(Integer.parseInt(string));
    }

    @Override
    public boolean read(JsonObject jsonObject) {
        return this.setValue(jsonObject.get(this.effectiveKey(jsonObject)).getAsNumber().intValue());
    }

    @Override
    public void write(JsonObject jsonObject) {
        jsonObject.addProperty(this.getConfigName(), this.getValue());
    }

    public Integer getMinimum() {
        return minimum;
    }

    public Integer getMaximum() {
        return maximum;
    }
}
