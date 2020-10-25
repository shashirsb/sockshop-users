/*
 * Copyright (c) 2020 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * http://oss.oracle.com/licenses/upl.
 */

package io.helidon.examples.sockshop.users;

import java.util.Collections;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import static io.helidon.examples.sockshop.users.JsonHelpers.embed;
import static io.helidon.examples.sockshop.users.JsonHelpers.obj;

@ApplicationScoped
@Path("/cards")
public class CardsResource implements CardApi{

    @Inject
    private UserRepository users;

    @Override
    public Response getAllCards() {
        return Response.ok(embed("card", Collections.emptyList())).build();
    }

    @Override
    public Response registerCard(AddCardRequest req) {
        Card card = new Card(req.longNum, req.expires, req.ccv);
        CardId id = users.addCard(req.userID, card);

        return Response.ok(obj().add("id", id.toString()).build()).build();
    }

    @Override
    public Card getCard(CardId id) {
        return users.getCard(id).mask();
    }

    @Override
    public Response deleteCard(CardId id) {
        try {
            users.removeCard(id);
            return status(true);
        }
        catch (RuntimeException e) {
            return status(false);
        }
    }

    // --- helpers ----------------------------------------------------------

    private static Response status(boolean fSuccess) {
        return Response.ok(obj().add("status", fSuccess).build()).build();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddCardRequest {
        public String longNum;
        public String expires;
        public String ccv;
        public String userID;
    }
}
