/*
 * Copyright (c) 2020 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * http://oss.oracle.com/licenses/upl.
 */

package io.helidon.examples.sockshop.users.atpsoda;

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

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Specializes;
import javax.inject.Inject;



import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.eclipse.microprofile.opentracing.Traced;

import io.helidon.examples.sockshop.catalog.atpsoda.AtpSodaProducers;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonArray;

import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;


import io.helidon.config.Config;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;

import java.io.*;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.stream.Stream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import oracle.soda.rdbms.OracleRDBMSClient;
import oracle.soda.OracleDatabase;
import oracle.soda.OracleCursor;
import oracle.soda.OracleCollection;
import oracle.soda.OracleDocument;
import oracle.soda.OracleException;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

import org.apache.commons.lang3.StringUtils;

/**
 * An implementation of {@link io.helidon.examples.sockshop.users.UserRepository}
 * that that uses MongoDB as a backend data store.
 */
@ApplicationScoped
@Alternative
@Priority(APPLICATION)
@Traced
public class AtpSodaUserRepository extends DefaultUserRepository {



    public static AtpSodaProducers asp = new AtpSodaProducers();
    public static OracleDatabase db = asp.dbConnect();

    @Inject
    AtpSodaUserRepository() {
        try {
            String UserResponse = createData();
            System.out.println(UserResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public AddressId addAddress(String userID, Address address) {
        User user = findUser(userID);
        if (user != null) {
            AddressId id = user.addAddress(address).getId();
            updateUser(userID, user);
            return id;
        }
        return null;
    }

    @Override
    public Address getAddress(AddressId id) {
        return findUser(id.getUser()).getAddress(id.getAddressId());
    }
  



    @Override
    public void removeAddress(AddressId id) {
        String userID = id.getUser();
        User user = findUser(userID);
        if (user != null) {
            user.removeAddress(id.getAddressId());
            updateUser(userID, user);
        }
    }

    @Override
    public CardId addCard(String userID, Card card) {
        User user = findUser(userID);
        if (user != null) {
            CardId id = user.addCard(card).getId();
            updateUser(userID, user);
            return id;
        }
        return null;
    }

    @Override
    public Card getCard(CardId id) {
        return findUser(id.getUser()).getCard(id.getCardId());
    }

    @Override
    public void removeCard(CardId id) {
        String userID = id.getUser();
        User user = findUser(userID);
        if (user != null) {
            user.removeCard(id.getCardId());
            updateUser(userID, user);
        }
    }

    @Override
    public Collection < ? extends User > getAllUsers() {
        List < User > results = new ArrayList < > ();
        // users.find().forEach((Consumer << ? super User > ) results::add);
        return results;
    }

    @Override
    public User getOrCreate(String id) {
        return Optional.ofNullable(findUser(id))
            .orElse(new User(id));
    }

    @Override
    public User getUser(String id) {
        return findUser(id);
    }

    @Override
    public User removeUser(String id) {
        User user = findUser(id);
        String k1 = null;
        if (user != null) {
            try {


                OracleCollection col = this.db.admin().createCollection("users");

                OracleDocument filterSpec =
                    this.db.createDocumentFromString("{ \"username\" : \"" + id + "\"}");

                OracleCursor c = col.find().filter(filterSpec).getCursor();

                while (c.hasNext()) {
                    OracleDocument resultDoc = c.next();

                    k1 = resultDoc.getKey();
                }

                c.close();

                col.find().key("\"" + k1 + "\"").remove();

            } catch (Exception e) {
                //TODO: handle exception
            }
            // users.deleteOne(eq("username", id));
        }
        return user;
    }

    @Override
    public boolean authenticate(String username, String password) {
        User user = findUser(username);
        return user != null ? user.authenticate(password) : false;
    }
    ////////////
    @Override
    public User register(User user) {
        User _user = null;
        User existing = findUser(user.getUsername());

        if (existing.getUsername() == null) {
            existing = _user;


            //String stringToParse = "[{\"addresses\":[{\"_id\":{\"addressId\":\"1\",\"user\":\"sssxx\"},\"addressId\":\"1\",\"city\":\"Denver\",\"country\":\"USA\",\"links\":{\"address\":{\"href\":\"http://user/addresses/randy:1\"},\"self\":{\"href\":\"http://user/addresses/randy:1\"}},\"number\":\"123\",\"postcode\":\"74765\",\"street\":\"Mountain St\"}],\"cards\":[{\"_id\":{\"cardId\":\"7865\",\"user\":\"randy\"},\"cardId\":\"7865\",\"ccv\":\"042\",\"expires\":\"08/23\",\"links\":{\"card\":{\"href\":\"http://user/cards/randy:7865\"},\"self\":{\"href\":\"http://user/cards/randy:7865\"}},\"longNum\":\"6543123465437865\"}],\"email\":\"randy@weavesocks.com\",\"firstName\":\"Randy\",\"lastName\":\"Stafford\",\"links\":{\"customer\":{\"href\":\"http://user/customers/randy\"},\"self\":{\"href\":\"http://user/customers/randy\"},\"addresses\":{\"href\":\"http://user/customers/randy/addresses\"},\"cards\":{\"href\":\"http://user/customers/randy/cards\"}},\"password\":\"pass\",\"username\":\"randy\"}]";
            // User user = new User("Test", "User", "user@weavesocks.com", "user", "pass");
            // user.addCard(new Card("1234123412341234", "12/19", "123"));
            // user.addAddress(new Address("123", "Main St", "Springfield", "12123", "USA"));
            try {
                String stringToParse = "[{\"addresses\":[],\"cards\":[],\"email\":\"" + user.email + "\",\"firstName\":\"" + user.firstName + "\",\"lastName\":\"" + user.lastName + "\",\"links\":{\"customer\":{\"href\":\"http://user/customers/" + user.getUsername() + "\"},\"self\":{\"href\":\"http://user/customers/" + user.getUsername() + "\"},\"addresses\":{\"href\":\"http://user/customers/" + user.getUsername() + "/addresses\"},\"cards\":{\"href\":\"http://user/customers/" + user.getUsername() + "/cards\"}},\"password\":\"" + user.password + "\",\"username\":\"" + user.getUsername() + "\"}]";

                JSONParser parser = new JSONParser();
                JSONObject jsonObjects = new JSONObject();
                JSONArray jsonArray = (JSONArray) parser.parse(stringToParse.replace("\\", ""));



                // Create a collection with the name "MyJSONCollection".
                // This creates a database table, also named "MyJSONCollection", to store the collection.\

                OracleCollection col = this.db.admin().createCollection("users");

                for (int i = 0; i < jsonArray.size(); i++) {

                    // Create a JSON document.
                    OracleDocument doc =
                        this.db.createDocumentFromString(jsonArray.get(i).toString());

                    // Insert the document into a collection.
                    col.insert(doc);

                }

            } catch (OracleException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //  users.insertOne(user);

        }

        return existing;
    }

    // --- helpers ----------------------------------------------------------

    private User findUser(String userID) {


        User user = new User();
        // Address address = new Address();
        // Card card = new Card();
        org.json.simple.JSONObject _jsonObject = new JSONObject();
        org.json.simple.parser.JSONParser _parser = new JSONParser();

        try {
            // Get a collection with the name "socks".
            // This creates a database table, also named "socks", to store the collection.

            OracleCollection col = this.db.admin().createCollection("users");

            // Find a documents in the collection.
            OracleDocument filterSpec =
                this.db.createDocumentFromString("{ \"username\" : \"" + userID + "\"}");
            OracleCursor c = col.find().filter(filterSpec).getCursor();
            String jsonFormattedString = null;

            try {
                OracleDocument resultDoc;
                while (c.hasNext()) {
                    // ArrayList < Address > addressesList = new ArrayList < > ();
                    // ArrayList < Card > cardsList = new ArrayList < > ();
                    //     String firstName, String lastName, String email, String username, String password,
                    // Collection<Address> addresses, Collection<Card> cards
                    resultDoc = c.next();



                    JSONParser parser = new JSONParser();

                    Object obj = parser.parse(resultDoc.getContentAsString());

                    JSONObject jsonObject = (JSONObject) obj;
                    System.out.println(jsonObject.get("username"));

                    user.username = jsonObject.get("username").toString();
                    user.firstName = jsonObject.get("firstName").toString();
                    user.lastName = jsonObject.get("lastName").toString();
                    user.email = jsonObject.get("email").toString();
                    user.password = jsonObject.get("password").toString();


                    JSONArray addressesList = (JSONArray) jsonObject.get("addresses");

                    // for (int i = 0; i < _jsonArrayaddresses.size(); i++) {
                    //     addressesList.add(_jsonArrayaddresses.get(i).toString());
                    // }

                    JSONArray cardsList = (JSONArray) jsonObject.get("cards");

                    // for (int i = 0; i < _jsonArraycards.size(); i++) {
                    //     cardsList.add(_jsonArraycards.get(i).toString());
                    // }

                    user.addresses = addressesList;
                    user.cards = cardsList;
                    System.out.println("findUser(String userID)  " + userID + ".. GET Request 200OK");
                    System.out.println("findUser(String userID)  " + user.toString() + ".. GET Request 200OK");

                }
            } finally {
                // IMPORTANT: YOU MUST CLOSE THE CURSOR TO RELEASE RESOURCES.
                if (c != null) c.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return user;


        //return users.find(eq("username", userID)).first();
    }

    private void updateUser(String userID, User user) {
        try {
            String k1 = null;

            OracleCollection col = this.db.admin().createCollection("users");

            OracleDocument filterSpec =
                this.db.createDocumentFromString("{ \"username\" : \"" + userID + "\"}");

            OracleCursor c = col.find().filter(filterSpec).getCursor();

            while (c.hasNext()) {
                OracleDocument resultDoc = c.next();
                System.out.println("Key:         " + resultDoc.getKey());
                System.out.println("Content:     " + resultDoc.getContentAsString());
                System.out.println("Version:     " + resultDoc.getVersion());
                System.out.println("Last modified: " + resultDoc.getLastModified());
                System.out.println("Created on:    " + resultDoc.getCreatedOn());
                System.out.println("Media:         " + resultDoc.getMediaType());
                System.out.println("\n");


                System.out.println("*************************");
                System.out.println("*************************");
                System.out.println("*************************");
                System.out.println(user.toString());
                System.out.println("*************************");
                System.out.println("*************************");

                JSONArray jsonAddressArray = new JSONArray();
                if(user.addresses != null) {        

                for (Address address: user.addresses) {
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("addressId", address.addressId.toString());
                    jsonObj.put("number", address.number.toString());
                    jsonObj.put("street", address.street.toString());
                    jsonObj.put("city", address.city.toString());
                    jsonObj.put("postcode", address.postcode.toString());
                    jsonObj.put("country", address.country.toString());

                  
                    jsonAddressArray.add(jsonObj);

                }
            }

            JSONArray jsonCardsArray = new JSONArray();
            if(user.cards != null) {                

                for (Card card: user.cards) {
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("longNum", card.longNum.toString());
                    jsonObj.put("expires", card.expires.toString());
                    jsonObj.put("ccv", card.ccv.toString());

                    jsonCardsArray.add(jsonObj);
         
                }
            }

                String document = "{\"addresses\":" + jsonAddressArray.toString() + ",\"cards\":" + jsonCardsArray.toString() + ",\"email\":\"" + user.email + "\",\"firstName\":\"" + user.firstName + "\",\"lastName\":\"" + user.lastName + "\",\"links\":{\"customer\":{\"href\":\"http://user/customers/" + user.username + "\"},\"self\":{\"href\":\"http://user/customers/" + user.username + "\"},\"addresses\":{\"href\":\"http://user/customers/" + user.username + "/addresses\"},\"cards\":{\"href\":\"http://user/customers/" + user.username + "/cards\"}},\"password\":\"" + user.password + "\",\"username\":\"" + user.username + "\"}";
                System.out.println(document);
                System.out.println(user.addresses.toString());
                System.out.println(user.cards.toString());
                OracleDocument newDoc = this.db.createDocumentFromString(document);


                resultDoc = col.find().key(resultDoc.getKey()).version(resultDoc.getVersion()).replaceOneAndGet(newDoc);
                System.out.println(resultDoc);

                // users.replaceOne(eq("username", userID), user);
                System.out.println("UpdateUser(String userID, User user).... GET Request 200OK");

            }

            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String createData() {
        // Create a collection with the name "MyJSONCollection".
        // This creates a database table, also named "MyJSONCollection", to store the collection.

        String stringToParse = "[{\"addresses\":[{\"_id\":{\"addressId\":\"1\",\"user\":\"randy\"},\"addressId\":\"1\",\"city\":\"Denver\",\"country\":\"USA\",\"links\":{\"address\":{\"href\":\"http://user/addresses/randy:1\"},\"self\":{\"href\":\"http://user/addresses/randy:1\"}},\"number\":\"123\",\"postcode\":\"74765\",\"street\":\"Mountain St\"}],\"cards\":[{\"_id\":{\"cardId\":\"7865\",\"user\":\"randy\"},\"cardId\":\"7865\",\"ccv\":\"042\",\"expires\":\"08/23\",\"links\":{\"card\":{\"href\":\"http://user/cards/randy:7865\"},\"self\":{\"href\":\"http://user/cards/randy:7865\"}},\"longNum\":\"6543123465437865\"}],\"email\":\"randy@weavesocks.com\",\"firstName\":\"Randy\",\"lastName\":\"Stafford\",\"links\":{\"customer\":{\"href\":\"http://user/customers/randy\"},\"self\":{\"href\":\"http://user/customers/randy\"},\"addresses\":{\"href\":\"http://user/customers/randy/addresses\"},\"cards\":{\"href\":\"http://user/customers/randy/cards\"}},\"password\":\"pass\",\"username\":\"randy\"},{\"addresses\":[{\"_id\":{\"addressId\":\"1\",\"user\":\"user\"},\"addressId\":\"1\",\"city\":\"Springfield\",\"country\":\"USA\",\"links\":{\"address\":{\"href\":\"http://user/addresses/user:1\"},\"self\":{\"href\":\"http://user/addresses/user:1\"}},\"number\":\"123\",\"postcode\":\"12123\",\"street\":\"Main St\"}],\"cards\":[{\"_id\":{\"cardId\":\"1234\",\"user\":\"user\"},\"cardId\":\"1234\",\"ccv\":\"123\",\"expires\":\"12/19\",\"links\":{\"card\":{\"href\":\"http://user/cards/user:1234\"},\"self\":{\"href\":\"http://user/cards/user:1234\"}},\"longNum\":\"1234123412341234\"}],\"email\":\"user@weavesocks.com\",\"firstName\":\"Test\",\"lastName\":\"User\",\"links\":{\"customer\":{\"href\":\"http://user/customers/user\"},\"self\":{\"href\":\"http://user/customers/user\"},\"addresses\":{\"href\":\"http://user/customers/user/addresses\"},\"cards\":{\"href\":\"http://user/customers/user/cards\"}},\"password\":\"pass\",\"username\":\"user\"},{\"addresses\":[{\"_id\":{\"addressId\":\"1\",\"user\":\"aleks\"},\"addressId\":\"1\",\"city\":\"Tampa\",\"country\":\"USA\",\"links\":{\"address\":{\"href\":\"http://user/addresses/aleks:1\"},\"self\":{\"href\":\"http://user/addresses/aleks:1\"}},\"number\":\"555\",\"postcode\":\"33633\",\"street\":\"Spruce St\"}],\"cards\":[{\"_id\":{\"cardId\":\"4567\",\"user\":\"aleks\"},\"cardId\":\"4567\",\"ccv\":\"007\",\"expires\":\"10/20\",\"links\":{\"card\":{\"href\":\"http://user/cards/aleks:4567\"},\"self\":{\"href\":\"http://user/cards/aleks:4567\"}},\"longNum\":\"4567456745674567\"}],\"email\":\"aleks@weavesocks.com\",\"firstName\":\"Aleks\",\"lastName\":\"Seovic\",\"links\":{\"customer\":{\"href\":\"http://user/customers/aleks\"},\"self\":{\"href\":\"http://user/customers/aleks\"},\"addresses\":{\"href\":\"http://user/customers/aleks/addresses\"},\"cards\":{\"href\":\"http://user/customers/aleks/cards\"}},\"password\":\"pass\",\"username\":\"aleks\"},{\"addresses\":[{\"_id\":{\"addressId\":\"1\",\"user\":\"bin\"},\"addressId\":\"1\",\"city\":\"Boston\",\"country\":\"USA\",\"links\":{\"address\":{\"href\":\"http://user/addresses/bin:1\"},\"self\":{\"href\":\"http://user/addresses/bin:1\"}},\"number\":\"123\",\"postcode\":\"01555\",\"street\":\"Boston St\"}],\"cards\":[{\"_id\":{\"cardId\":\"3691\",\"user\":\"bin\"},\"cardId\":\"3691\",\"ccv\":\"789\",\"expires\":\"01/21\",\"links\":{\"card\":{\"href\":\"http://user/cards/bin:3691\"},\"self\":{\"href\":\"http://user/cards/bin:3691\"}},\"longNum\":\"3691369136913691\"}],\"email\":\"bin@weavesocks.com\",\"firstName\":\"Bin\",\"lastName\":\"Chen\",\"links\":{\"customer\":{\"href\":\"http://user/customers/bin\"},\"self\":{\"href\":\"http://user/customers/bin\"},\"addresses\":{\"href\":\"http://user/customers/bin/addresses\"},\"cards\":{\"href\":\"http://user/customers/bin/cards\"}},\"password\":\"pass\",\"username\":\"bin\"},{\"addresses\":[{\"_id\":{\"addressId\":\"1\",\"user\":\"harvey\"},\"addressId\":\"1\",\"city\":\"San Francisco\",\"country\":\"USA\",\"links\":{\"address\":{\"href\":\"http://user/addresses/harvey:1\"},\"self\":{\"href\":\"http://user/addresses/harvey:1\"}},\"number\":\"123\",\"postcode\":\"99123\",\"street\":\"O'Farrell St\"}],\"cards\":[{\"_id\":{\"cardId\":\"5476\",\"user\":\"harvey\"},\"cardId\":\"5476\",\"ccv\":\"456\",\"expires\":\"03/22\",\"links\":{\"card\":{\"href\":\"http://user/cards/harvey:5476\"},\"self\":{\"href\":\"http://user/cards/harvey:5476\"}},\"longNum\":\"6854657645765476\"}],\"email\":\"harvey@weavesocks.com\",\"firstName\":\"Harvey\",\"lastName\":\"Raja\",\"links\":{\"customer\":{\"href\":\"http://user/customers/harvey\"},\"self\":{\"href\":\"http://user/customers/harvey\"},\"addresses\":{\"href\":\"http://user/customers/harvey/addresses\"},\"cards\":{\"href\":\"http://user/customers/harvey/cards\"}},\"password\":\"pass\",\"username\":\"harvey\"}]";
        try {

            JSONParser parser = new JSONParser();
            JSONObject jsonObjects = new JSONObject();
            JSONArray jsonArray = (JSONArray) parser.parse(stringToParse.replace("\\", ""));



            // Create a collection with the name "MyJSONCollection".
            // This creates a database table, also named "MyJSONCollection", to store the collection.\

            OracleCollection col = this.db.admin().createCollection("users");

            col.admin().truncate();

            for (int i = 0; i < jsonArray.size(); i++) {

                // Create a JSON document.
                OracleDocument doc =
                    this.db.createDocumentFromString(jsonArray.get(i).toString());

                // Insert the document into a collection.
                col.insert(doc);

            }

        } catch (OracleException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "successfully created users collection !!!";
    }
}