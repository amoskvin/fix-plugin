/*
 * Copyright (C) 2023  Alec Moskvin <code@netreq.com>
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.netreq.fix.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.components.Service;
import com.netreq.fix.plugin.psi.FixField;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public final class FixDictionaryService {

    private final Field[][] specFields;
    private final String[] descriptions;
    private final String[] versions;
    private final Map<String, Integer> versionMap;

    public FixDictionaryService() {
        ObjectMapper mapper = new ObjectMapper();
        Spec[] specs;
        Field[] fields;
        try {
            try (InputStream in = FixDictionaryService.class.getResourceAsStream("/data/desc.json")) {
                descriptions = mapper.readValue(in, String[].class);
            }

            try (InputStream in = FixDictionaryService.class.getResourceAsStream("/data/fields.json")) {
                fields = mapper.readValue(in, Field[].class);
            }

            try (InputStream in = FixDictionaryService.class.getResourceAsStream("/data/specs.json")) {
                specs = mapper.readValue(in, Spec[].class);
            }

        } catch (IOException | RuntimeException e) {
            throw new RuntimeException("Failed to deserialize data", e);
        }

        specFields = new Field[specs.length][];
        versions = new String[specs.length];
        Map<String, Integer> versionMap = new HashMap<>(specs.length);
        for (int i = 0; i < specs.length; i++) {
            specFields[i] = new Field[specs[i].max()];
            for (var ref : specs[i].fields().entrySet()) {
                specFields[i][ref.getKey() - 1] = fields[ref.getValue()];
            }
            versionMap.put(specs[i].name(), i);
            versions[i] = specs[i].name();
        }
        this.versionMap = Collections.unmodifiableMap(versionMap);
    }

    public FieldInfo getFieldInfo(FixField fixField) {
        int tag = fixField.getTag();
        Integer spec = fixField.getSpecVersionId();
        if (spec != null && tag > 0 && tag <= specFields[spec].length) {
            Field fieldData = specFields[spec][tag - 1];
            if (fieldData != null) {
                String valueName = null;
                if (fieldData.enums() != null) {
                    String value = fixField.getValue();
                    for (Enum e : fieldData.enums()) {
                        if (e.value().equals(value)) {
                            valueName = e.name();
                            break;
                        }
                    }
                }
                return new FieldInfo(fieldData.name(), valueName);
            }
        }
        return null;
    }

    public FieldDetails getFieldDetails(FixField fixField) {
        int tag = fixField.getTag();
        Integer spec = fixField.getSpecVersionId();
        if (spec != null && tag > 0 && tag <= specFields[spec].length) {
            Field fieldData = specFields[spec][tag - 1];
            if (fieldData != null) {
                String description = null;
                Integer descId = fieldData.desc();
                if (descId != null) {
                    description = descriptions[descId];
                }
                String valueName = null;
                String valueDescription = null;
                if (fieldData.enums() != null) {
                    String value = fixField.getValue();
                    for (Enum e : fieldData.enums()) {
                        if (e.value().equals(value)) {
                            valueName = e.name();
                            if (e.desc() != null) {
                                valueDescription = descriptions[e.desc()];
                            }
                            break;
                        }
                    }
                }
                return new FieldDetails(fieldData.name(), valueName, description, valueDescription, fieldData.type(), versions[spec]);
            }
        }
        return null;
    }

    public Integer getSpecVersionId(String verStr) {
        if ("FIXT.1.1".equals(verStr)) {
            // assume it's the latest 5.0 for now
            return specFields.length - 1;
        }
        return versionMap.get(verStr);
    }

    public record FieldInfo(String fieldName, String valueName) {

    }

    public record FieldDetails(String fieldName, String valueName, String fieldDescription, String valueDescription, String type, String spec) {

    }

    record Spec(String name, int max, Map<Integer, Integer> fields) {

    }

    record Field(String name, String type, Integer desc, Enum[] enums) {

    }

    record Enum(String value, String name, Integer desc) {

    }
}
