
# Event-Driven Ledger

An event-driven financial ledger demonstrating distributed system patterns and solid data consistency.

This project was made to highlight how an event-driven application works in an asynchronous processing manner, being able to prevent dual-write failures and ensure robust data consistency for financial transactions.
 
 
 

***Stack: Java, Spring Boot, Kafka, PostgreSQL, Maven and Docker***




## Architecture & Core Patterns chosen

**The Transactional Outbox Pattern:** Implemented to solve the "dual-write" problem and to be prepared against a Kafka outage, it saves transactions to the `transOutBox` outbox table and `transEventStored` table. Both inserts are within the same ACID transaction. A scheduled relay component constantly consults for unpublished transactions until they're successfully sent from the database to the message broker.

**Event Sourcing (CQRS-lite):** Financial facts are appended immutably to the event store. States are then created over it (Frauds or Balance).

**Dead Letter Queues (DLT)**: All Kafka consumers are equipped with a `DeadLetterPublishingRecoverer` and `FixedBackOff` so problematic events are handled to a new topic rather than crashing the application. 

**Asynchronous Projections**: The consumer group composed of `transaction-auditLog`, `transaction-balance-projector`, and `transaction-fraud-checker` is responsible for the asynchronous processing; they react to events in order to cover the downstream reporting in this case.

## Engineering choices and Trade-offs

**Correct and slow rather than eventual and quick:** `BalanceProjector` is a consumer that updates the balance at the `accountsBalance` table. The `TransCommandImpl` service which processes (`operate()`) the transactions bases the account's balance upon its `reconstituteBalance()` method, which sums every transaction existent for that account. If we're to consult `accountsBalance` at the `operate()`, the balance might not be updated, as the consumer could still be processing it, or the event might not have arrived there yet. This is a problem because debits could be applied upon falsely sufficient balances.

**Solution:** As in this project we're not caring much about speed and a high volume of transactions, calculating the balance in the chosen way is fine. In the end it's a discussion between speed vs certainty. Certainty has been chosen for this little case, even though it'll rarely be put upon speed test.

**Idempotency Handling:** In our consumers, we deal with the idempotency problem by checking whether the conjunction of Kafka's partition and offset is already under a determined in-memory HashSet. This scenario generates a problem, which is: if Kafka were restarted, its partitions and offsets would be reset again, and we wouldn't be able to process an event because their conjunctions would be already within the uncleared in-memory HashSet.

**Solution:** As the scope of this prototype doesn't focus on a production application, but rather to simply deal with Kafka's idempotency, the in-memory HashSet approach fits as a solution. Ideally, to solve the problem more safely, a database table can be easily used as a record for the conjunctions.
## Run Locally
In order to run locally you'll need: Java 17+, Maven, and Docker (for Postgres & Kafka).

Clone the project

```bash
  git clone https://github.com/eduardofl6/Ledger-Project.git
```

Go to the project directory

```bash
  cd Ledger-Project
```

Compose docker images

```bash
docker-compose up -d
```
Start the Spring Boot application using the Maven wrapper:

```bash
./mvnw clean install
./mvnw spring-boot:run
```

Test the API by submitting a test transaction using this curl command to see the asynchronous processing in action:

```bash
curl -X POST http://localhost:8080/account/operation \
     -H "Content-Type: application/json" \
     -d '{
           "accountId": "acc-12345",
           "amount": 250.50,
           "type": "credit"
         }'
```        

## Authors

- [Eduardo Fortes Luiz](https://www.linkedin.com/in/eduardo-ffortes-luiz-/)


