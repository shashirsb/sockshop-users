/*
 * Copyright (c) 2020 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * http://oss.oracle.com/licenses/upl.
 */

package io.helidon.examples.sockshop.users;

import java.util.Map;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;

import static java.util.Collections.singletonMap;

abstract class JsonHelpers {
    private static final JsonBuilderFactory JSON = Json.createBuilderFactory(null);

    static JsonObjectBuilder obj() {
        return JSON.createObjectBuilder();
    }

    static Map<String, Object> embed(String name, Object value) {
        return singletonMap("_embedded", singletonMap(name, value));
    }
}
