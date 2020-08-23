package ai.hual.labrador.utils;

import ai.hual.labrador.utils.RegionUtils.Region;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class DirectionalRegionUtils {

    public static class DirectionalRegion {
        @JsonSerialize
        public Region start;
        @JsonSerialize
        public Region end;

        public DirectionalRegion() {
        }

        public DirectionalRegion(Region start, Region end) {
            this.start = start;
            this.end = end;
        }

        public String toString() {
            if (start != null) {
                return start.toString();
            } else if (end != null) {
                return end.toString();
            } else {
                return "Something wrong with this directional region";
            }
        }
    }

    public static DirectionalRegion from(Region region) {
        return new DirectionalRegion(region, null);
    }

    public static DirectionalRegion to(Region region) {
        return new DirectionalRegion(null, region);
    }
}
