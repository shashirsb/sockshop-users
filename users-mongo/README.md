# orders-mongo

This module implements [MongoDB backend](./src/main/java/io/helidon/examples/sockshop/users/mongo/MongoUserRepository.java)
for the [User Service](../README.md).

## Building the Service

See [main documentation page](../README.md#building-the-service) for instructions.

## Running the Service

This implementation is slightly more complex to run, because it requires a MongoDB instance
to use as a data store.

First you will need to create a Docker network that will be used by both MongoDB and the service 
containers, if you haven't done that already:

```bash
$ docker network create sockshop 
``` 

Then you can run MongoDB container, but you need to assign it to the `sockshop` network 
created above, and give it a name that the service container expects (`users-db` in this case):

```bash
$ docker run --rm --name users-db --network sockshop mongo:4.2.2
``` 
> **Note:** The `--rm` flag above ensures that the container is removed automatically after it is 
> stopped. This allows you to re-run the command above without having to remove the `users-db`
> container manually between runs.

Finally, you can start the service container in the same network:

```bash
$ docker run --network sockshop -p 7001:7001 helidon/sockshop/users-mongo
``` 

Once the container is up and running, you should be able to access [service API](../README.md#api) 
by navigating to http://localhost:7001/.

As a basic test, you should be able to perform an HTTP GET against `/cards` endpoint:

```bash
$ curl -i http://localhost:7001/cards
``` 
which should return 200 response code.

## License

The Universal Permissive License (UPL), Version 1.0
