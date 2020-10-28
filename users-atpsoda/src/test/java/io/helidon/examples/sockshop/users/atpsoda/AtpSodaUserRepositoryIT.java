/*
 * Copyright (c) 2020 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * http://oss.oracle.com/licenses/upl.
 */

package io.helidon.examples.sockshop.users.atpsoda;

import io.helidon.examples.sockshop.users.UserRepository;
import io.helidon.examples.sockshop.users.UserRepositoryTest;

//import io.helidon.examples.sockshop.users.atpsoda.*;

/**
 * Integration tests for {@link io.helidon.examples.sockshop.users.mongo.MongoUserRepository}.
 */
public class AtpSodaUserRepositoryIT extends UserRepositoryTest {
    @Override
    protected UserRepository getUserRepository() {
        String host = System.getProperty("db.host","localhost");
        int    port = Integer.parseInt(System.getProperty("db.port","27017"));

        return new AtpSodaUserRepository();
    }
}
