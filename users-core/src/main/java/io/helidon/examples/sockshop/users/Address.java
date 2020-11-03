/*
 * Copyright (c) 2020 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * http://oss.oracle.com/licenses/upl.
 */

package io.helidon.examples.sockshop.users;

import java.io.Serializable;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Representation of an address.
 */
@Data
@NoArgsConstructor
@Entity
@IdClass(AddressId.class)
@Schema(description = "User address")
public class Address implements Serializable {
    /**
     * The address identifier.
     */
    @Id
    @Schema(description = "Address identifier")
    public String addressId;

    /**
     * The street number.
     */
    @Schema(description = "Street number")
    public String number;

    /**
     * The street name.
     */
    @Schema(description = "Street name")
    public String street;

    /**
     * The city name.
     */
    @Schema(description = "City name")
    public String city;

    /**
     * The postal code.
     */
    @Schema(description = "Postal code")
    public String postcode;

    /**
     * The country name.
     */
    @Schema(description = "Country name")
    public String country;

    /**
     * The user this address is associated with, purely for JPA optimization.
     */
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonbTransient
    public User user;

    /**
     * Construct {@code Address} with specified parameters.
     */
    @Builder
    public Address(String number, String street, String city, String postcode, String country) {
        this.number = number;
        this.street = street;
        this.city = city;
        this.postcode = postcode;
        this.country = country;
    }

    /**
     * Return the user this address is associated with.
     *
     * @return the user this address is associated with
     */
    User getUser() {
    return user;
    }

    /**
     * Set the uer this address belongs to.
     *
     * @param user the user to set
     *
     * @return this user
     */
    Address setUser(User user) {
        this.user = user;
        return this;
    }

    /**
     * Set the address id.
     */
    Address setAddressId(String id) {
        this.addressId = id;
        return this;
    }

    /**
     * Return Address.Id for this address.
     */
    public AddressId getId() {
        return new AddressId(user.getUsername(), addressId);
    }

    /**
     * Return {@code _links} attribute for this entity.
     *
     * @return {@code _links} attribute for this entity
     */
    @JsonbProperty("links")
    public Links getLinks() {
        return Links.address(getId());
    }
}