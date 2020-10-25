/*
 * Copyright (c) 2020 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * http://oss.oracle.com/licenses/upl.
 */

package io.helidon.examples.sockshop.users;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * User data model.
 */
@Data
@Entity
@Schema(description = "User data representing a customer")
public class User implements Serializable {
    /**
     * User identifier.
     */
    @Id
    @Schema(description = "User identifier")
    private String username;

    /**
     * First name.
     */
    @Schema(description = "First name")
    private String firstName;

    /**
     * Last name.
     */
    @Schema(description = "Last name")
    private String lastName;

    /**
     * User's email.
     */
    @Schema(description = "User email")
    private String email;

    /**
     * The password.
     */
    @Schema(description = "User password")
    private String password;

    /**
     * The addresses that are associated with the user.
     */
    @JsonbTransient
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses = new ArrayList<>();

    /**
     * The cards that belongs to the user.
     */
    @JsonbTransient
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Card> cards = new ArrayList<>();

    /**
     * Default constructor.
     */
    public User() {
    }

    /**
     * Construct {@code User} with ID only.
     */
    public User(String username) {
        this.username = username;
    }

    /**
     * Construct {@code User} with specified attributes.
     */
    public User(String firstName, String lastName, String email, String username, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.password = password;
    }

    /**
     * Construct {@code User} with specified parameters.
     */
    protected User(String firstName, String lastName, String email, String username, String password,
                Collection<Address> addresses, Collection<Card> cards) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.password = password;
        addresses.forEach(this::addAddress);
        cards.forEach(this::addCard);
    }

    @JsonbTransient
    public String getPassword() {
        return password;
    }

    /**
     * Return the address for the specified address ID.
     *
     * @param id the address identifier
     *
     * @return the address for the specified address ID
     */
    public Address getAddress(String id) {
        return addresses.stream()
            .filter(address -> address.getAddressId().equals(id))
            .findFirst()
            .orElse(new Address()
                    .setAddressId(id)
                    .setUser(this));
    }

    /**
     * Add the specified address.
     *
     * @param address to be added
     *
     * @return the added address
     */
    public Address addAddress(Address address) {
        if (address.getAddressId() == null) {
            address.setAddressId(Integer.toString(addresses.size() + 1));
        }

        addresses.add(address.setUser(this));
        return address;
    }

    /**
     * Set the addresses.
     *
     * @param addresses the list of addresses
     */
    public void setAddresses(List<Address> addresses) {
        addresses.forEach(this::addAddress);
    }

    /**
     * Remove address for the specified address ID.
     *
     * @param id the address ID
     *
     * @return the user
     */
    public User removeAddress(String id) {
        addresses.remove(getAddress(id));
        return this;
    }

    /**
     * Return the card for the specified card ID.
     *
     * @param id the card ID
     *
     * @return the card for the specified card ID
     */
    public Card getCard(String id) {
        return cards.stream()
            .filter(card -> card.getCardId().equals(id))
            .findFirst()
            .orElse(new Card()
                .setCardId(id)
                .setUser(this));
    }

    /**
     * Add the specified card to the user.
     *
     * @param card the card to be added
     *
     * @return the added card
     */
    public Card addCard(Card card) {
        if (card.getCardId() == null) {
            card.setCardId(card.last4());
        }
        cards.add(card.setUser(this));
        return card;
    }

    /**
     * Set the cards.
     *
     * @param cards the list of cards
     */
    public void setCards(List<Card> cards) {
        cards.forEach(this::addCard);
    }
    
    /**
     * Remove the card with the specified card ID.
     *
     * @param id the card ID
     *
     * @return this user
     */
    public User removeCard(String id) {
        cards.remove(getCard(id));
        return this;
    }

    /**
     * Return {@code _links} attribute for this entity.
     *
     * @return {@code _links} attribute for this entity
     */
    @JsonbProperty("_links")
    public Links getLinks() {
        return Links.customer(username);
    }

    /**
     * Authenticate the user against the specified password.
     *
     * @param password the password
     *
     * @return true if the specified password match the user's password
     */
    public Boolean authenticate(String password) {
        return password.equals(this.password);
    }
}
