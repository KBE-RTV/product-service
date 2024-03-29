# Product Service
This project provides the functionality to create and manage products form a warehouse.
It was written for the Course "Component based development" at HTW Berlin SoSe22 as part of the final project.
For the other Services see https://github.com/KBE-RTV.
## Functionality
This Service runs as a Docker container, the corresponding docker compose file is found in the projects root folder. If started the Docker Container uses Port 8082.

On startup the warehouse loads default data into its database via an CSV import.  
Use ```docker-compose up``` to start the container, ```docker-compose up --build``` to rebuild before start.
## Communication
This Service communicates via RabbitMQ, so an RabbitMQ Server is required.
It connects to the kbeTopicExchange and uses one queue for incoming requests and one for responses.
For price Calculation it needs this https://github.com/KBE-RTV/price-service service to be running.  
All communication is JSON Strings.
### Queues
productServiceQueueCall with productService.call as routing key for incoming requests.
productServiceQueueResponse with productService.response as routing key for outgoing responses.
priceServiceQueueCall with priceService.call as routing key to communicate with the price service.
priceServiceQueueResponse to listen to responses from the price service.
### Behavior
#### Get a specific entry
To get one specific entry the CallRequestDTO is used and the ArrayList contains only the UUID of the wanted entry. The Type corresponds to "celestialBody" or "planetarySystem".  
The response will be an CelestialBodyDetailDTO or PlanetarySystemDetailDTO, depending on the type chosen.
#### Get all entries
To get all entries the CallRequestDTO is used and the ArrayList is empty. The Type corresponds to "celestialBody" or "planetarySystem".
The response will be an CelestialBodyDetailDTO or PlanetarySystemDetailDTO, depending on the type chosen.
#### Creating an entry
To create an entry the CallCreateDTO is used.  
The response will be an PlanetarySystemDetailDTO, the ArrayList contains either the created element or is empty if the creation failed.
