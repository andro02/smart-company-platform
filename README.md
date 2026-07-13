# Predmet: Napredne Veb Tehnologije

## Frontend: Angular
## Backend: Java + Spring Boot

## Student 1:
- **Ana Spanovic**, SV48/2021

## Student 2:
- **Andrija Slovic**, SV12/2021

## Student 3:
- **Vojkan Markovic**, SV65/2021

---

## Potrebni servisi

Pre pokretanja aplikacije, neophodno je pokrenuti sledeće servise:

- **PostgreSQL** — relaciona baza podataka (lokalno)
- **InfluxDB** — time series baza podataka (Docker)
- **RabbitMQ** — message broker (Docker)

Za pokretanje Docker servisa:

```bash
docker-compose up -d
```

---

## Prvo pokretanje aplikacije

1. U root direktorijumu projekta pokrenuti:

```bash
npm install
```

2. Nakon toga, startovati frontend i backend istovremeno:

```bash
npm run start
```

3. Automatski se kreira superadmin nalog sa username-om `admin`, dok se lozinka nalazi u `superadmin.txt` u direktorijumu `back/smartcompany`.

---

## Pokretanje simulatora

Simulator generiše podatke u realnom vremenu za fabrike, magacine i vozila. Pokreće se iz `factory-simulator` direktorijuma u tri odvojena terminala:

```bash
# Terminal 1 — fabrika
cd factory-simulator
set SPRING_CONFIG_LOCATION=classpath:/application-factory.properties
mvn spring-boot:run

# Terminal 2 — magacin
cd factory-simulator
set SPRING_CONFIG_LOCATION=classpath:/application-warehouse.properties
mvn spring-boot:run

# Terminal 3 — vozilo
cd factory-simulator
set SPRING_CONFIG_LOCATION=classpath:/application-vehicle.properties
mvn spring-boot:run
```

Simulator šalje heartbeat i podatke (temperatura, distanca, proizvodnja) putem RabbitMQ-a ka backend-u koji ih upisuje u InfluxDB.