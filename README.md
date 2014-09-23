# Cheddar

![Cheddar logo](http://googledrive.com/host/0B3KE77--Zs7eSlN2MzF5ekdDZFE/)

## Click's Hexagonal Domain-Driven Architecture

Cheddar is a Java framework for enterprise applications on Amazon Web Services (AWS) using _domain-driven design_ (DDD). Bounded contexts are implemented as microservices which are integrated using an _event-driven architecture_ and expose a REST API. Cheddar has full AWS integration using native services such as [SQS](http://aws.amazon.com/sqs/), [SNS](http://aws.amazon.com/sns/), [DynamoDB](http://aws.amazon.com/dynamodb/) and [CloudSearch](http://aws.amazon.com/cloudsearch/).

## Domain-Driven Design
Domain-Driven Design (DDD) is an approach for developing software that closely aligns the implementation to evolving business concepts.

DDD promotes focus on the subjects most important to the business problem at hand, identifying these as _core domains_. Complex systems are decomposed to several orthogonal domains using _strategic modelling_, avoiding cross-contamination and enabling modeling of relationships between domains.

To talk about the domain, a _domain model_ and supporting [_ubiquitous language_](http://martinfowler.com/bliki/UbiquitousLanguage.html) is used. _Domain experts_ use the ubiquitous language every day when talking about the domain. The ubiquitous language and domain model form the basis for a solution that addresses the domain, known as a _bounded context_. A [rich domain model](http://www.martinfowler.com/bliki/AnemicDomainModel.html) encapsulates all its domain (business) logic.

Domain models publish _domain events_ when something of potential interest occurs in the model. Domain events may be consumed by the local or (after mapping by an _anti-corruption layer_) foreign bounded contexts. This application of the [observer pattern](http://en.wikipedia.org/wiki/Observer_pattern) promotes decoupling of the domains.

Practical concerns irrelevant to the domain but vital for a working implementation are kept out of the domain, such as persistence, security and transactions.

By modelling the business in software, DDD enables building of flexible, scalable solutions tightly aligned with business goals.

### More resources

#### Online articles
- [Wikipedia article on Domain-Driven Design](http://en.wikipedia.org/wiki/Domain-driven_design)
- [Vaughn Vernon's blog](http://vaughnvernon.co/)
- [Martin Fowler's blog articles on Domain-Driven Design](http://martinfowler.com/tags/domain%20driven%20design.html)
- [DDD Community](http://dddcommunity.org/)
- [Short intro to DDD](http://domainlanguage.com/ddd/)
- [Domain-Driven Design presentation](http://www.slideshare.net/panesofglass/domain-driven-design)
- [An Introduction To Domain-Driven Design](http://msdn.microsoft.com/en-us/magazine/dd419654.aspx)

#### Books
- [Vaughn Vernon - Implementing Domain-Driven Design](https://vaughnvernon.co/?page_id=168)
- [Eric Evans - Domain-driven Design: Tackling Complexity in the Heart of Software](http://www.amazon.co.uk/Domain-driven-Design-Tackling-Complexity-Software/dp/0321125215)
- [InfoQ free book - Domain Driven Design Quickly](http://www.infoq.com/minibooks/domain-driven-design-quickly)

## Cheddar applications
Cheddar provides a well defined Java project structure for implementing each bounded context as a REST HTTP application hosted on AWS.

### Hexagonal architecture
Cheddar uses a [_hexagonal architecture_](http://alistair.cockburn.us/Hexagonal+architecture) to house each bounded context, meaning the domain model is surrounded by an _application layer_ and an _adapter layer_.

![Hexagonal architecture diagram](http://googledrive.com/host/0B3KE77--Zs7eOVJUZko4eDhQbkk/ "Hexagonal architecture")

#### Domain model
Central to the implementation is the domain model, containing rich domain objects, repositories and supporting domain services. All domain logic belongs in the domain model.

#### Application layer
The application layer is responsible for co-ordination of operations performed on the domain model, application of security and transaction boundaries. The public interfaces for the application layer form the API for the bounded context. This API satisfies the use cases for the bounded context. No domain logic resides in the application layer.

#### Adapter layer
The adapter layer adapts all communication for the bounded context, both inbound and outbound. It abstracts technical detail for various communcation forms:
* Messaging using [SQS](http://aws.amazon.com/sqs/)
* Persistence using [DynamoDB](http://aws.amazon.com/dynamodb/)
* Multiple data versions and formats
* RESTful web services

The adapter layer is also responsible for mapping between foreign concepts outside the bounded context and the native ubiquitous language inside.

The adapter layer maps between data types present in the APIs and a _canonical data model_ (CDM). The CDM is shared by all bounded contexts and is authored using [XML Schema](http://www.w3schools.com/schema/). The REST resource representations are defined using types defined in the CDM.

### Event-driven architecture

![Event-driven architecture diagram](https://googledrive.com/host/0B3KE77--Zs7eTjZ5MXlNM1RSN3c/ "Event-driven architecture")

The bounded contexts are integrated using a loosely coupled event-driven architecture. The adapter layer supports this integration by implementing event messaging via SQS and SNS.

### Cheddar software stack
Each bounded context is packaged as a standalone Java application, and should be deployed on a dedicated [AWS EC2](http://aws.amazon.com/ec2/) instance (groups of multiple EC2 instances for each application will be supported in future). [Grizzly](https://grizzly.java.net/) and [Jersey](https://jersey.java.net/) are used as the basis for the REST HTTP server application.  Cheddar provides a driver package for application clients (currently only Java is supported), but any client capable of consuming REST services can easily work with Cheddar applications.
