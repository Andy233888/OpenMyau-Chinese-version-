package myau.property.properties;

import com.google.gson.JsonObject;
import myau.property.Property;

import java.util.function.BooleanSupplier;

public class PercentProperty extends Property<Integer> {
    private final Integer minimum;
    private final Integer maximum;

    public PercentProperty(String name, Integer value) {
        this(name, value, null);
    }

    public PercentProperty(String name, Integer value, BooleanSupplier check) {
        this(name, value, 0, 100, check);
    }

    public PercentProperty(String name, Integer value, Integer minimum, Integer maximum, BooleanSupplier booleanSupplier) {
        super(name, value, value1 -> value1 >= minimum && value1 <= maximum, booleanSupplier);
        this.minimum = minimum;
        this.maximum = maximum;
    }

    /** configName 用于配置文件（英文键），name 用于 UI 显示 */
    public PercentProperty(String configName, String name, Integer value) {
        this(configName, name, value, 0, 100, null);
    }

    /** configName 用于配置文件（英文键），name 用于 UI 显示 */
    public PercentProperty(String configName, String name, Integer value, Integer minimum, Integer maximum, BooleanSupplier booleanSupplier) {
        super(configName, name, value, value1 -> value1 >= minimum && value1 <= maximum, booleanSupplier);
        this.minimum = minimum;
        this.maximum = maximum;
    }

    @Override
    public String getValuePrompt() {
        return String.format("%d-%d%%", this.minimum, this.maximum);
    }

    @Override
    public String formatValue() {
        return String.format("&b%d%%", this.getValue());
    }

    @Override
    public boolean parseString(String string) {
        return this.setValue(Integer.parseInt(string.replace("%", "")));
    }

    @Override
    public boolean read(JsonObject jsonObject) {
        return this.setValue(jsonObject.get(this.effectiveKey(jsonObject)).getAsNumber().intValue());
    }

    @Override
    public void write(JsonObject jsonObject) {
        jsonObject.addProperty(this.getConfigName(), this.getValue());
    }

    public Integer getMaximum() {
        return maximum;
    }

    public Integer getMinimum() {
        return minimum;
    }
}
