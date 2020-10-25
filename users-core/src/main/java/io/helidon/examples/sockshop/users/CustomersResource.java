/*
 * Copyright (c) 2020 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * http://oss.oracle.com/licenses/upl.
 */

package io.helidon.examples.sockshop.users;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import static io.helidon.examples.sockshop.users.JsonHelpers.embed;
import static io.helidon.examples.sockshop.users.JsonHelpers.obj;

@ApplicationScoped
@Path("/customers")
public class CustomersResource implements CustomerApi {

    @Inject
    private UserRepository users;

    @Override
    public Response getAllCustomers() {
        return Response.ok(embed("customer", users.getAllUsers())).build();
    }

    @Override
    public Response getCustomer(String id) {
        return Response.ok(users.getOrCreate(id)).build();
    }

    @Override
    public Response deleteCustomer(String id) {
        User prev = users.removeUser(id);
        return Response.ok(obj().add("status", prev != null).build()).build();
    }

    @Override
    public Response getCustomerCards(String id) {
        User user = users.getUser(id);
        return Response.ok(embed("card", user.getCards().stream().map(Card::mask).toArray())).build();
    }

    @Override
    public Response getCustomerAddresses(String id) {
        User user = users.getUser(id);
        return Response.ok(embed("address", user.getAddresses())).build();
    }
}
