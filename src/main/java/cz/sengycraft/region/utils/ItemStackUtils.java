package cz.sengycraft.region.utils;

import java.util.HashSet;

public class ItemStackUtils {

    public static HashSet<Integer> getSlots(String slot) {

        HashSet<Integer> slots = new HashSet<>();

        String[] parts = slot.split(";");

        for (String part : parts) {

            if (part.contains("-")) {
                String[] range = part.split("-");
                if (range.length == 2 && NumberUtils.isInteger(range[0]) && NumberUtils.isInteger(range[1])) {
                    int start = Integer.parseInt(range[0]);
                    int end = Integer.parseInt(range[1]);

                    if (start <= end) {
                        for (int i = start; i <= end; i++) {
                            slots.add(i);
                        }
                    } else {
                        for (int i = start; i >= end; i--) {
                            slots.add(i);
                        }
                    }
                }
            } else if (NumberUtils.isInteger(part)) {
                slots.add(Integer.parseInt(part));
            }
        }

        return slots;
    }
}
