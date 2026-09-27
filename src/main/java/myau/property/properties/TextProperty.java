package myau.property.properties;

import com.google.gson.JsonObject;
import myau.property.Property;

import java.util.function.BooleanSupplier;

public class TextProperty extends Property<String> {
    public TextProperty(String name, String value) {
        this(name, value, (BooleanSupplier) null);
    }

    public TextProperty(String name, String value, BooleanSupplier booleanSupplier) {
        super(name, value, booleanSupplier);
    }

    /** configName 用于配置文件（英文键），name 用于 UI 显示 */
    public TextProperty(String configName, String name, String value) {
        this(configName, name, value, null);
    }

    /** configName 用于配置文件（英文键），name 用于 UI 显示 */
    public TextProperty(String configName, String name, String value, BooleanSupplier booleanSupplier) {
        super(configName, name, value, booleanSupplier);
    }

    @Override
    public String getValuePrompt() {
        return "text";
    }

    @Override
    public String formatValue() {
        return String.format("&f%s", this.getValue());
    }

    @Override
    public boolean parseString(String string) {
        return this.setValue(string);
    }

    @Override
    public boolean read(JsonObject jsonObject) {
        return this.parseString(jsonObject.get(this.effectiveKey(jsonObject)).getAsString());
    }

    @Override
    public void write(JsonObject jsonObject) {
        jsonObject.addProperty(this.getConfigName(), this.getValue());
    }
}
