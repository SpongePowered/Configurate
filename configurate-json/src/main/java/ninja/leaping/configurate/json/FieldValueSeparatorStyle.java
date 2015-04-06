package ninja.leaping.configurate.json;

public enum FieldValueSeparatorStyle {
    SPACE_BOTH_SIDES(" : "),
    SPACE_AFTER(": "),
    NO_SPACE(":");
    private final String decorationType;

    FieldValueSeparatorStyle(String decorationType) {
        this.decorationType = decorationType;
    }

    public String getValue() {
        return decorationType;
    }
}
