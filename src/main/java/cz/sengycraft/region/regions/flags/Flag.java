package cz.sengycraft.region.regions.flags;

public class Flag {

    String name;

    public Flag(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flag flag = (Flag) o;
        return name.equals(flag.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
