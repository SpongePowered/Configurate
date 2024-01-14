package defaults;

import org.spongepowered.configurate.interfaces.meta.defaults.DefaultBoolean;
import org.spongepowered.configurate.interfaces.meta.defaults.DefaultDecimal;
import org.spongepowered.configurate.interfaces.meta.defaults.DefaultNumeric;
import org.spongepowered.configurate.interfaces.meta.defaults.DefaultString;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public interface CorrectDefaults {
    @DefaultBoolean
    boolean apple();

    @DefaultBoolean(true)
    boolean blueberry();

    @DefaultDecimal
    float cherry();

    @DefaultDecimal(123.5)
    float dragonfruit();

    @DefaultDecimal
    double eggplant();

    @DefaultDecimal(234.23)
    double fig();

    @DefaultNumeric
    byte grape();

    @DefaultNumeric(127)
    byte huckleberry();

    @DefaultNumeric
    char italianPrunePlum();

    @DefaultNumeric(126)
    char jackfruit();

    @DefaultNumeric('c')
    char kiwi();

    @DefaultNumeric
    short lemon();

    @DefaultNumeric(1341)
    short mango();

    @DefaultNumeric
    int nectarine();

    @DefaultNumeric(1231241)
    int orange();

    @DefaultNumeric
    long pineapple();

    @DefaultNumeric(24524524521L)
    long quince();

    @DefaultString
    String raspberry();

    @DefaultString("Hello world!")
    String strawberry();

    @DefaultString("Hi")
    void tamarillo(String value);

    default String ugli() {
        return "A fruit";
    }

    default int velvetApple() {
        return 500;
    }
}
