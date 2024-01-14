package defaults;

import java.lang.Override;
import java.lang.String;
import org.spongepowered.configurate.interfaces.meta.defaults.DefaultBoolean;
import org.spongepowered.configurate.interfaces.meta.defaults.DefaultDecimal;
import org.spongepowered.configurate.interfaces.meta.defaults.DefaultNumeric;
import org.spongepowered.configurate.interfaces.meta.defaults.DefaultString;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

/**
 * Automatically generated implementation of the config */
@ConfigSerializable
final class CorrectDefaultsImpl implements CorrectDefaults {
    @DefaultBoolean(false)
    private boolean apple = false;

    @DefaultBoolean(true)
    private boolean blueberry = true;

    @DefaultDecimal(0.0F)
    private float cherry = 0.0F;

    @DefaultDecimal(123.5F)
    private float dragonfruit = 123.5F;

    @DefaultDecimal(0.0D)
    private double eggplant = 0.0D;

    @DefaultDecimal(234.23D)
    private double fig = 234.23D;

    @DefaultNumeric(0)
    private byte grape = 0;

    @DefaultNumeric(127)
    private byte huckleberry = 127;

    @DefaultNumeric(0)
    private char italianPrunePlum = 0;

    @DefaultNumeric(126)
    private char jackfruit = 126;

    @DefaultNumeric(99)
    private char kiwi = 99;

    @DefaultNumeric(0)
    private short lemon = 0;

    @DefaultNumeric(1341)
    private short mango = 1341;

    @DefaultNumeric(0)
    private int nectarine = 0;

    @DefaultNumeric(1231241)
    private int orange = 1231241;

    @DefaultNumeric(0L)
    private long pineapple = 0L;

    @DefaultNumeric(24524524521L)
    private long quince = 24524524521L;

    @DefaultString("")
    private String raspberry = "";

    @DefaultString("Hello world!")
    private String strawberry = "Hello world!";

    @DefaultString("Hi")
    private String tamarillo = "Hi";

    private String ugli = CorrectDefaults.super.ugli();

    private int velvetApple = CorrectDefaults.super.velvetApple();

    @Override
    public boolean apple() {
        return apple;
    }

    @Override
    public boolean blueberry() {
        return blueberry;
    }

    @Override
    public float cherry() {
        return cherry;
    }

    @Override
    public float dragonfruit() {
        return dragonfruit;
    }

    @Override
    public double eggplant() {
        return eggplant;
    }

    @Override
    public double fig() {
        return fig;
    }

    @Override
    public byte grape() {
        return grape;
    }

    @Override
    public byte huckleberry() {
        return huckleberry;
    }

    @Override
    public char italianPrunePlum() {
        return italianPrunePlum;
    }

    @Override
    public char jackfruit() {
        return jackfruit;
    }

    @Override
    public char kiwi() {
        return kiwi;
    }

    @Override
    public short lemon() {
        return lemon;
    }

    @Override
    public short mango() {
        return mango;
    }

    @Override
    public int nectarine() {
        return nectarine;
    }

    @Override
    public int orange() {
        return orange;
    }

    @Override
    public long pineapple() {
        return pineapple;
    }

    @Override
    public long quince() {
        return quince;
    }

    @Override
    public String raspberry() {
        return raspberry;
    }

    @Override
    public String strawberry() {
        return strawberry;
    }

    @Override
    public void tamarillo(String value) {
        this.tamarillo = value;
    }

    @Override
    public String ugli() {
        return ugli;
    }

    @Override
    public int velvetApple() {
        return velvetApple;
    }
}
