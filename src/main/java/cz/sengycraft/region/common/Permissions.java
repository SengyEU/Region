package cz.sengycraft.region.common;

public enum Permissions {

    MENU("menu"),
    WAND("wand"),
    ADD("add"),
    REMOVE("remove"),
    WHITELIST("whitelist"),
    CREATE("create"),
    FLAG("flag"),
    BYPASS("bypass"),
    RELOAD("reload");

    private final String permission;

    Permissions(String permission) {
        this.permission = permission;
    }

    public String permission() {
        return "region." + permission;
    }

}
