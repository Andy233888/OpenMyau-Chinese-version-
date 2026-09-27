package myau.property;

import com.google.gson.JsonObject;
import myau.module.Module;

import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

public abstract class Property<T> {
    private final String name;
    private final T type;
    private final Predicate<T> validator;
    private final BooleanSupplier visibleChecker;
    private T value;
    private Module owner;
    /** 语言无关的英文配置键，用于读写配置文件；为 null 时回退到显示名 */
    private String configName;

    protected Property(String name, Object value, BooleanSupplier visibleChecker) {
        this(name, value, null, visibleChecker);
    }

    protected Property(String name, Object value, Predicate<T> predicate, BooleanSupplier visibleChecker) {
        this.name = name;
        this.type = (T) value;
        this.validator = predicate;
        this.visibleChecker = visibleChecker;
        this.value = (T) value;
        this.owner = null;
    }

    /** 带英文配置键的构造：configName 用于配置文件，name 用于 UI 显示 */
    protected Property(String configName, String name, Object value, BooleanSupplier visibleChecker) {
        this(name, value, null, visibleChecker);
        this.configName = configName;
    }

    /** 带英文配置键的构造：configName 用于配置文件，name 用于 UI 显示 */
    protected Property(String configName, String name, Object value, Predicate<T> predicate, BooleanSupplier visibleChecker) {
        this(name, value, predicate, visibleChecker);
        this.configName = configName;
    }

    public String getName() {
        return this.name;
    }

    /** 语言无关的英文配置键；未显式设置时回退到显示名 */
    public String getConfigName() {
        return this.configName != null ? this.configName : this.name;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    /** 判断 JSON 中是否包含此属性的配置键（英文键优先，兼容中文显示名键） */
    public boolean hasKey(JsonObject jsonObject) {
        return jsonObject.has(this.getConfigName())
                || (!this.getConfigName().equals(this.name) && jsonObject.has(this.name));
    }

    /** 返回 JSON 中实际存在的键（英文键优先，回退中文显示名） */
    public String effectiveKey(JsonObject jsonObject) {
        if (jsonObject.has(this.getConfigName())) {
            return this.getConfigName();
        }
        return this.name;
    }

    public abstract String getValuePrompt();

    public boolean isVisible() {
        return this.visibleChecker == null || this.visibleChecker.getAsBoolean();
    }

    public T getValue() {
        return this.value;
    }

    public abstract String formatValue();

    public boolean setValue(Object object) {
        if (this.validator != null && !this.validator.test((T) object)) {
            return false;
        } else {
            this.value = (T) object;
            if (this.owner != null) {
                this.owner.verifyValue(this.name);
            }
            return true;
        }
    }

    public void parseString() {
    }

    public void setOwner(Module module) {
        this.owner = module;
    }

    public abstract boolean parseString(String string);

    public abstract boolean read(JsonObject jsonObject);

    public abstract void write(JsonObject jsonObject);
}
