package myau.property.properties;

import com.google.gson.JsonObject;
import myau.property.Property;

import java.util.function.BooleanSupplier;

public class FloatProperty extends Property<Float> {
    private final Float minimum;
    private final Float maximum;

    public FloatProperty(String name, Float value, Float minimum, Float maximum) {
        this(name, value, minimum, maximum, null);
    }

    public FloatProperty(String string, Float value, Float minimum, Float maximum, BooleanSupplier check) {
        super(string, value, floatV -> floatV >= 0 && floatV <= Float.MAX_VALUE, check);
        this.minimum = minimum;
        this.maximum = maximum;
    }

    /** configName 用于配置文件（英文键），name 用于 UI 显示 */
    public FloatProperty(String configName, String name, Float value, Float minimum, Float maximum) {
        this(configName, name, value, minimum, maximum, null);
    }

    /** configName 用于配置文件（英文键），name 用于 UI 显示 */
    public FloatProperty(String configName, String name, Float value, Float minimum, Float maximum, BooleanSupplier check) {
        super(configName, name, value, floatV -> floatV >= 0 && floatV <= Float.MAX_VALUE, check);
        this.minimum = minimum;
        this.maximum = maximum;
    }

    @Override
    public String getValuePrompt() {
        return String.format("%s-%s", this.minimum, this.maximum);
    }

    @Override
    public String formatValue() {
        return String.format("&6%s", this.getValue());
    }

    @Override
    public boolean parseString(String string) {
        return this.setValue(Float.parseFloat(string));
    }

    @Override
    public boolean read(JsonObject jsonObject) {
        return this.setValue(jsonObject.get(this.effectiveKey(jsonObject)).getAsNumber().floatValue());
    }

    @Override
    public void write(JsonObject jsonObject) {
        jsonObject.addProperty(this.getConfigName(), this.getValue());
    }

    public Float getMinimum() {
        return minimum;
    }

    public Float getMaximum() {
        return maximum;
    }
}
