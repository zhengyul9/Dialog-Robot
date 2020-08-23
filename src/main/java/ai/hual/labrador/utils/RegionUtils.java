package ai.hual.labrador.utils;

public class RegionUtils {

    public static class Region {

        private RegionType type;
        private String name;

        public Region(RegionType type, String name) {
            this.type = type;
            this.name = name;
        }

        public String toString() {
            return this.name;
        }

        public RegionType getType() {
            return type;
        }

        public void setType(RegionType type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static Region province(String name) {
        return new Region(RegionType.PROVINCE, name);
    }

    public static Region city(String name) {
        return new Region(RegionType.CITY, name);
    }

    public static Region county(String name) {
        return new Region(RegionType.COUNTY, name);
    }

    public static Region district(String name) {
        return new Region(RegionType.DISTRICT, name);
    }
}
