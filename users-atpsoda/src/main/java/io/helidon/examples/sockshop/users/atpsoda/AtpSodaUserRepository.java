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

import com.google.gson.Gson;
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

    public boolean isNullOrEmptyList(List<?> l ) {
        return l == null || l.isEmpty();
    }

    public boolean isNullOrEmptyCollection(Collection<?> c ) {
        return c == null || c.isEmpty();
    }

    private User findUser(String userID) {


        //return users.find(eq("username", userID)).first();

        ////////////////////////


        User user = new User(null, null, null, null, null);

        org.json.simple.JSONObject _jsonObject = new JSONObject();
        org.json.simple.parser.JSONParser _parser = new JSONParser();


        try {

            OracleCollection col = this.db.admin().createCollection("users");

            // Find a documents in the collection.
            OracleDocument filterSpec =
                this.db.createDocumentFromString("{ \"username\" : \"" + userID + "\"}");
            
            OracleDocument localuser = col.find().filter(filterSpec).getOne();
            Gson gson =new Gson();
            

                   JSONParser parser = new JSONParser();
                   Object obj = parser.parse(localuser.getContentAsString());
                   JSONObject jsonObject = (JSONObject) obj;

                   System.out.println("1---------------------------------------");
                   System.out.println(jsonObject.toString());
                   System.out.println("2---------------------------------------");

                   user = gson.fromJson(jsonObject, User.class);
                   //user = new User(jsonObject.get("firstName").toString(), jsonObject.get("lastName").toString(), jsonObject.get("email").toString(), jsonObject.get("username").toString(), jsonObject.get("password").toString());

                   JSONObject _itemsObject = new JSONObject();

                    // from  soda data
                   //orders.items = jsonObject.get("items").toString();       // Convert to Collection<Item>
                //    JSONArray _addressArray = (JSONArray) jsonObject.get("addresses");
                //    Collection <Address> addresses = user.addresses;
                //    if (_addressArray != null ) {
                //        for (Object o: _addressArray) {
                //            if (o instanceof JSONObject) {
                //                _itemsObject = (JSONObject) o;                               
                //                user.addAddress(new Address(_itemsObject.get("number").toString(), _itemsObject.get("street").toString(), _itemsObject.get("city").toString(), _itemsObject.get("postcode").toString(), _itemsObject.get("country").toString()));
                      
                //            }
                //        }
                //    }  else {
                //        user.addAddress(new Address("","","","",""));
                      
                //    }               



                   // from  soda data
                   //orders.items = jsonObject.get("items").toString();       // Convert to Collection<Item>
                //    JSONArray _cardArray = (JSONArray) jsonObject.get("card");
                //    Collection <Card> cards = user.cards;
                //    if (_cardArray != null ) {
                //        for (Object o: _cardArray) {
                //            if (o instanceof JSONObject) {
                //                _itemsObject = (JSONObject) o;
                //                user.addCard(new Card(_itemsObject.get("longNum").toString(), _itemsObject.get("expires").toString(), _itemsObject.get("ccv").toString()));
                //            }
                //        }
                //    } else {
                //        user.addCard(new Card("","",""));
                //    }
            
               
//            OracleCursor c = col.find().filter(filterSpec).getCursor();
//            String jsonFormattedString = null;
//            try {
//                OracleDocument resultDoc;
//
//                while (c.hasNext()) {
//                    JSONObject _itemsObject = new JSONObject();
//                    // String orderId, String carrier, String trackingNumber, LocalDate deliveryDate
//                    resultDoc = c.next();
//                    JSONParser parser = new JSONParser();
//                    Object obj = parser.parse(resultDoc.getContentAsString());
//                    JSONObject jsonObject = (JSONObject) obj;
//
//                    System.out.println("1---------------------------------------");
//                    System.out.println(jsonObject.toString());
//                    System.out.println("2---------------------------------------");
//
//                    user = new User(jsonObject.get("firstName").toString(), jsonObject.get("lastName").toString(), jsonObject.get("email").toString(), jsonObject.get("username").toString(), jsonObject.get("password").toString());
//
//
//                     // from  soda data
//                    //orders.items = jsonObject.get("items").toString();       // Convert to Collection<Item>
//                    JSONArray _addressArray = (JSONArray) jsonObject.get("addresses");
//                    Collection <Address> addresses = user.addresses;
//                    if (_addressArray != null ) {
//                        for (Object o: _addressArray) {
//                            if (o instanceof JSONObject) {
//                                _itemsObject = (JSONObject) o;                               
//                                user.addAddress(new Address(_itemsObject.get("number").toString(), _itemsObject.get("street").toString(), _itemsObject.get("city").toString(), _itemsObject.get("postcode").toString(), _itemsObject.get("country").toString()));
//                       
//                            }
//                        }
//                    }  else {
//                        user.addAddress(new Address("","","","",""));
//                       
//                    }               
//
//
//
//                    // from  soda data
//                    //orders.items = jsonObject.get("items").toString();       // Convert to Collection<Item>
//                    JSONArray _cardArray = (JSONArray) jsonObject.get("card");
//                    Collection <Card> cards = user.cards;
//                    if (_cardArray != null && this.isNullOrEmptyCollection(cards)) {
//                        for (Object o: _cardArray) {
//                            if (o instanceof JSONObject) {
//                                _itemsObject = (JSONObject) o;
//                                user.addCard(new Card(_itemsObject.get("longNum").toString(), _itemsObject.get("expires").toString(), _itemsObject.get("ccv").toString()));
//                            }
//                        }
//                    } else {
//                        user.addCard(new Card("","",""));
//                    }
//                }
//                    
//            } finally {
//                // IMPORTANT: YOU MUST CLOSE THE CURSOR TO RELEASE RESOURCES.
//                if (c != null) c.close();
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("User findUser:" + user);
        System.out.println("User findUser. GET Request 200OK");
        return user;
    }

    private void updateUser(String userID, User user) {

              

        try {
            String k1 = null;

            OracleCollection col = this.db.admin().createCollection("users");

            OracleDocument filterSpec =
                this.db.createDocumentFromString("{ \"username\" : \"" + userID + "\"}");

            OracleCursor c = col.find().filter(filterSpec).getCursor();

            while (c.hasNext()) {
                JSONObject _itemsObject = new JSONObject();
                OracleDocument resultDoc = c.next();


                JSONParser parser = new JSONParser();
                Object obj = parser.parse(resultDoc.getContentAsString());
                JSONObject jsonObject = (JSONObject) obj;

                JSONArray addressesList = new JSONArray();

                  // from  soda data
                    //orders.items = jsonObject.get("items").toString();       // Convert to Collection<Item>
                    JSONArray _addressArray = (JSONArray) jsonObject.get("addresses");
                    Collection <Address> addressClass = user.addresses;
                    if (_addressArray != null && this.isNullOrEmptyCollection(addressClass)) {
                        for (Object o: _addressArray) {
                            if (o instanceof JSONObject) {
                                _itemsObject = (JSONObject) o;
                                addressesList.add(_itemsObject);
                            }
                        }
                    } else {
    

                        int i = 1;
                        for (Address _address: addressClass) {
    
                            
                            if( i == addressClass.size()){
                            JSONObject objaddress = new JSONObject();
                            objaddress.put("number", _address.number.toString());
                            objaddress.put("street", _address.street.toString());
                            objaddress.put("city", _address.city.toString());
                            objaddress.put("postcode", _address.postcode.toString());
                            objaddress.put("country", _address.country.toString());
                            addressesList.add(objaddress);
                            }
                            i++;
                        }

           



                    JSONArray cardsList = new JSONArray();



                    // from  soda data
                    //orders.items = jsonObject.get("items").toString();       // Convert to Collection<Item>

                    JSONArray _cardArray = (JSONArray) jsonObject.get("cards");
                    List <Card> cardClass = user.cards;
                    if (_cardArray != null && this.isNullOrEmptyList(cardClass)) {
                        for (Object o: _cardArray) {
                            if (o instanceof JSONObject) {
                                _itemsObject = (JSONObject) o;
                                cardsList.add(_itemsObject);
                            }
                        }
                    } else {
                   

                        i = 1;
                        for (Card _card: cardClass) {
                       
                            
                            if( i == cardClass.size()){
                                JSONObject objcard = new JSONObject();
                                objcard.put("longNum", Long.parseLong(_card.longNum.toString()));
                                objcard.put("expires", _card.expires.toString());
                                objcard.put("ccv", _card.ccv.toString());
                                cardsList.add(objcard);
                            }
                            i++;
                        }

                    }     



               String _document = "{\"addresses\":" + addressesList + ",\"card\":" + cardsList + ",\"email\":\"" + user.email + "\",\"firstName\":\"" + user.firstName + "\",\"lastName\":\"" + user.lastName + "\",\"links\":{\"customer\":{\"href\":\"http://user/customers/" + user.username + "\"},\"self\":{\"href\":\"http://user/customers/" + user.username + "\"},\"addresses\":{\"href\":\"http://user/customers/" + user.username + "/addresses\"},\"cards\":{\"href\":\"http://user/customers/" + user.username + "/cards\"}},\"password\":\"" + user.password + "\",\"username\":\"" + user.username + "\"}";

          

                OracleDocument newDoc = this.db.createDocumentFromString(_document);


                resultDoc = col.find().key(resultDoc.getKey()).version(resultDoc.getVersion()).replaceOneAndGet(newDoc);
         

                // users.replaceOne(eq("username", userID), user);
                System.out.println("UpdateUser(String userID, User user).... GET Request 200OK");

            }

            c.close();
        }} catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String createData() {
        // Create a collection with the name "MyJSONCollection".
        // This creates a database table, also named "MyJSONCollection", to store the collection.

        String stringToParse = "[{\"addresses\":[{\"_id\":{\"addressId\":\"1\",\"user\":\"randy\"},\"addressId\":\"1\",\"city\":\"Denver\",\"country\":\"USA\",\"links\":{\"address\":{\"href\":\"http://user/addresses/randy:1\"},\"self\":{\"href\":\"http://user/addresses/randy:1\"}},\"number\":\"123\",\"postcode\":\"74765\",\"street\":\"Mountain St\"}],\"card\":[{\"_id\":{\"cardId\":\"7865\",\"user\":\"randy\"},\"cardId\":\"7865\",\"ccv\":\"042\",\"expires\":\"08/23\",\"links\":{\"card\":{\"href\":\"http://user/cards/randy:7865\"},\"self\":{\"href\":\"http://user/cards/randy:7865\"}},\"longNum\":\"6543123465437865\"}],\"email\":\"randy@weavesocks.com\",\"firstName\":\"Randy\",\"lastName\":\"Stafford\",\"links\":{\"customer\":{\"href\":\"http://user/customers/randy\"},\"self\":{\"href\":\"http://user/customers/randy\"},\"addresses\":{\"href\":\"http://user/customers/randy/addresses\"},\"cards\":{\"href\":\"http://user/customers/randy/cards\"}},\"password\":\"pass\",\"username\":\"randy\"},{\"addresses\":[{\"_id\":{\"addressId\":\"1\",\"user\":\"user\"},\"addressId\":\"1\",\"city\":\"Springfield\",\"country\":\"USA\",\"links\":{\"address\":{\"href\":\"http://user/addresses/user:1\"},\"self\":{\"href\":\"http://user/addresses/user:1\"}},\"number\":\"123\",\"postcode\":\"12123\",\"street\":\"Main St\"}],\"cards\":[{\"_id\":{\"cardId\":\"1234\",\"user\":\"user\"},\"cardId\":\"1234\",\"ccv\":\"123\",\"expires\":\"12/19\",\"links\":{\"card\":{\"href\":\"http://user/cards/user:1234\"},\"self\":{\"href\":\"http://user/cards/user:1234\"}},\"longNum\":\"1234123412341234\"}],\"email\":\"user@weavesocks.com\",\"firstName\":\"Test\",\"lastName\":\"User\",\"links\":{\"customer\":{\"href\":\"http://user/customers/user\"},\"self\":{\"href\":\"http://user/customers/user\"},\"addresses\":{\"href\":\"http://user/customers/user/addresses\"},\"cards\":{\"href\":\"http://user/customers/user/cards\"}},\"password\":\"pass\",\"username\":\"user\"},{\"addresses\":[{\"_id\":{\"addressId\":\"1\",\"user\":\"aleks\"},\"addressId\":\"1\",\"city\":\"Tampa\",\"country\":\"USA\",\"links\":{\"address\":{\"href\":\"http://user/addresses/aleks:1\"},\"self\":{\"href\":\"http://user/addresses/aleks:1\"}},\"number\":\"555\",\"postcode\":\"33633\",\"street\":\"Spruce St\"}],\"cards\":[{\"_id\":{\"cardId\":\"4567\",\"user\":\"aleks\"},\"cardId\":\"4567\",\"ccv\":\"007\",\"expires\":\"10/20\",\"links\":{\"card\":{\"href\":\"http://user/cards/aleks:4567\"},\"self\":{\"href\":\"http://user/cards/aleks:4567\"}},\"longNum\":\"4567456745674567\"}],\"email\":\"aleks@weavesocks.com\",\"firstName\":\"Aleks\",\"lastName\":\"Seovic\",\"links\":{\"customer\":{\"href\":\"http://user/customers/aleks\"},\"self\":{\"href\":\"http://user/customers/aleks\"},\"addresses\":{\"href\":\"http://user/customers/aleks/addresses\"},\"cards\":{\"href\":\"http://user/customers/aleks/cards\"}},\"password\":\"pass\",\"username\":\"aleks\"},{\"addresses\":[{\"_id\":{\"addressId\":\"1\",\"user\":\"bin\"},\"addressId\":\"1\",\"city\":\"Boston\",\"country\":\"USA\",\"links\":{\"address\":{\"href\":\"http://user/addresses/bin:1\"},\"self\":{\"href\":\"http://user/addresses/bin:1\"}},\"number\":\"123\",\"postcode\":\"01555\",\"street\":\"Boston St\"}],\"cards\":[{\"_id\":{\"cardId\":\"3691\",\"user\":\"bin\"},\"cardId\":\"3691\",\"ccv\":\"789\",\"expires\":\"01/21\",\"links\":{\"card\":{\"href\":\"http://user/cards/bin:3691\"},\"self\":{\"href\":\"http://user/cards/bin:3691\"}},\"longNum\":\"3691369136913691\"}],\"email\":\"bin@weavesocks.com\",\"firstName\":\"Bin\",\"lastName\":\"Chen\",\"links\":{\"customer\":{\"href\":\"http://user/customers/bin\"},\"self\":{\"href\":\"http://user/customers/bin\"},\"addresses\":{\"href\":\"http://user/customers/bin/addresses\"},\"cards\":{\"href\":\"http://user/customers/bin/cards\"}},\"password\":\"pass\",\"username\":\"bin\"},{\"addresses\":[{\"_id\":{\"addressId\":\"1\",\"user\":\"harvey\"},\"addressId\":\"1\",\"city\":\"San Francisco\",\"country\":\"USA\",\"links\":{\"address\":{\"href\":\"http://user/addresses/harvey:1\"},\"self\":{\"href\":\"http://user/addresses/harvey:1\"}},\"number\":\"123\",\"postcode\":\"99123\",\"street\":\"O'Farrell St\"}],\"cards\":[{\"_id\":{\"cardId\":\"5476\",\"user\":\"harvey\"},\"cardId\":\"5476\",\"ccv\":\"456\",\"expires\":\"03/22\",\"links\":{\"card\":{\"href\":\"http://user/cards/harvey:5476\"},\"self\":{\"href\":\"http://user/cards/harvey:5476\"}},\"longNum\":\"6854657645765476\"}],\"email\":\"harvey@weavesocks.com\",\"firstName\":\"Harvey\",\"lastName\":\"Raja\",\"links\":{\"customer\":{\"href\":\"http://user/customers/harvey\"},\"self\":{\"href\":\"http://user/customers/harvey\"},\"addresses\":{\"href\":\"http://user/customers/harvey/addresses\"},\"cards\":{\"href\":\"http://user/customers/harvey/cards\"}},\"password\":\"pass\",\"username\":\"harvey\"}]";
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