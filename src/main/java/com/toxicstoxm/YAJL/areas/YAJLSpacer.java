package com.toxicstoxm.YAJL.areas;


import com.toxicstoxm.YAJL.colors.YAJLMessage;

import java.util.HashMap;

import static com.toxicstoxm.YAJL.YAJLSettingsBundle.*;

public class YAJLSpacer implements Spacer {

    private static final HashMap<String, Integer> spacings = new HashMap<>();

    @Override
    public String getSpacingFor(String messageElementGroup, String messageElement) {
        if (EnableAutoSpacing.getInstance().get()) {
            spacings.putIfAbsent(messageElementGroup, messageElement.length());
            int currentMax = spacings.get(messageElementGroup);
            int current = messageElement.length();
            if (currentMax < current) {
                spacings.put(messageElementGroup, current);
                return messageElement + getRSTAndBase();
            }
            if (currentMax > current)
                return messageElement + getRSTAndBase() + genSpacing(currentMax - current);
        }
        return messageElement + getRSTAndBase();
    }

    private String getRSTAndBase() {
        return (EnableAutoReset.getInstance().get() ? YAJLMessage.builder().reset().build() : "") + BaseSpacing.getInstance().get();
    }

    private String genSpacing(int i) {
        return " ".repeat(i);
    }
}
