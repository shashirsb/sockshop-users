/*
 * Copyright (c) 2020 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * http://oss.oracle.com/licenses/upl.
 */

package io.helidon.examples.sockshop.users.mongo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.mongodb.client.model.Indexes;
import io.helidon.examples.sockshop.users.AddressId;
import io.helidon.examples.sockshop.users.CardId;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import io.helidon.examples.sockshop.users.Address;
import io.helidon.examples.sockshop.users.Card;
import io.helidon.examples.sockshop.users.User;
import io.helidon.examples.sockshop.users.DefaultUserRepository;

import com.mongodb.client.MongoCollection;
import org.eclipse.microprofile.opentracing.Traced;

import static com.mongodb.client.model.Filters.eq;
import static javax.interceptor.Interceptor.Priority.APPLICATION;

/**
 * An implementation of {@link io.helidon.examples.sockshop.users.UserRepository}
 * that that uses MongoDB as a backend data store.
 */
@ApplicationScoped
@Alternative
@Priority(APPLICATION)
@Traced
public class MongoUserRepository extends DefaultUserRepository {

    private MongoCollection<User> users;

    @Inject
    MongoUserRepository(MongoCollection<User> users) {
        this.users = users;
    }

    @PostConstruct
    void configure() {
        users.createIndex(Indexes.hashed("username"));
    }

    @Override
    public AddressId addAddress(String userID, Address address) {
        User user = findUser(userID);
        
        System.out.println("1-------------------------------");
        System.out.println(userID);
        System.out.println(address.toString());
        System.out.println(user.toString());
        System.out.println("2-------------------------------");
        
        if (user != null) {
            AddressId id = user.addAddress(address).getId();
            updateUser(userID, user);
            System.out.println("3-------------------------------");
            System.out.println(id.toString());
            System.out.println("4-------------------------------");
            return id;
        }
        return null;
    }

    @Override
    public Address getAddress(AddressId id) {
            System.out.println("5-------------------------------");
            System.out.println(findUser(id.getUser()).getAddress(id.getAddressId())+"");
            System.out.println("6-------------------------------");
        return findUser(id.getUser()).getAddress(id.getAddressId());
    }

    @Override
    public void removeAddress(AddressId id) {
        String userID = id.getUser();
        User user = findUser(userID);

            System.out.println("7-------------------------------");
            System.out.println(userID+"");
            System.out.println(user+"");
            System.out.println("8-------------------------------");

        if (user != null) {
            user.removeAddress(id.getAddressId());
            updateUser(userID, user);
        }
    }

    @Override
    public CardId addCard(String userID, Card card) {
        User user = findUser(userID);
        System.out.println("0-------------------------------");
        System.out.println(userID+"");
        System.out.println(card+"");
        System.out.println("10-------------------------------");
        if (user != null) {
            CardId id = user.addCard(card).getId();
            updateUser(userID, user);
            System.out.println(id+"");
            System.out.println("10-------------------------------");
            return id;
        }
        return null;
    }

    @Override
    public Card getCard(CardId id) {
        System.out.println("11-------------------------------");
        System.out.println(findUser(id.getUser()).getCard(id.getCardId())+"");
        System.out.println("12-------------------------------");
        return findUser(id.getUser()).getCard(id.getCardId());
    }

    @Override
    public void removeCard(CardId id) {
        String userID = id.getUser();
        User user = findUser(userID);
        if (user != null) {
            System.out.println("12-------------------------------");
            System.out.println(user.removeCard(id.getCardId())+"");
            System.out.println("13-------------------------------");
            user.removeCard(id.getCardId());
            updateUser(userID, user);
        }
    }

    @Override
    public Collection<? extends User> getAllUsers() {
        List<User> results = new ArrayList<>();
        users.find().forEach((Consumer<? super User>) results::add);
        System.out.println("14-------------------------------");
        System.out.println(results+"");
        System.out.println("15-------------------------------");
        return results;
    }

    @Override
    public User getOrCreate(String id) {
        return Optional.ofNullable(findUser(id))
                .orElse(new User(id));
    }

    @Override
    public User getUser(String id) {
        System.out.println("14-------------------------------");
        System.out.println(findUser(id)+"");
        System.out.println("15-------------------------------");
        return findUser(id);
    }

    @Override
    public User removeUser(String id) {
        User user = findUser(id);
        if (user != null) {
            users.deleteOne(eq("username", id));
        }
        System.out.println("16-------------------------------");
        System.out.println(user+"");
        System.out.println("17-------------------------------");
        return user;
    }

    @Override
    public boolean authenticate(String username, String password) {
        User user = findUser(username);
        return user != null ? user.authenticate(password) : false;
    }

    @Override
    public User register(User user) {
        User existing = findUser(user.getUsername());
        if (existing == null) {
            users.insertOne(user);
        }

        System.out.println("16-------------------------------");
        System.out.println(existing+"");
        System.out.println("17-------------------------------");
        return existing;
    }

    // --- helpers ----------------------------------------------------------

    private User findUser(String userID) {
        return users.find(eq("username", userID)).first();
    }

    private void updateUser(String userID, User user) {
        users.replaceOne(eq("username", userID), user);
    }
}
