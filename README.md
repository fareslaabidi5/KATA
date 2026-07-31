# KataBank — Lecture et stockage de messages IBM MQ

Application Spring Boot qui consomme les messages déposés par les applications Back Office sur une file IBM MQ, les stocke en base relationnelle, et expose une API REST pour les consulter.

## Stack technique

| Composant | Version / choix |
|---|---|
| Java | 21 |
| Spring Boot | 4.1.0 |
| Client IBM MQ | `com.ibm.mq.jakarta.client` 9.4.x |
| Base de données | H2 (fichier/mémoire selon config) |
| Build | Maven |
| Conteneur MQ | Image Docker officielle `icr.io/ibm-messaging/mq:latest` |

**Note sur le client MQ** : on utilise le client `com.ibm.mq.jakarta.client` directement (pas le starter `mq-jms-spring-boot-starter`, qui n'a pas encore de version compatible Spring Boot 4). La configuration de connexion se fait dans `JmsConfig.java`.

## Lancer IBM MQ en local (Docker)

```bash
docker run -d --name ibm-mq \
  --env LICENSE=accept \
  --env MQ_QMGR_NAME=QM1 \
  --env MQ_APP_PASSWORD=passw0rd \
  --publish 1414:1414 \
  --publish 9443:9443 \
  icr.io/ibm-messaging/mq:latest
```

Créer la file et donner les droits à l'utilisateur `app` :

```bash
docker exec ibm-mq bash -c "echo \"DEFINE QLOCAL('NOTIFICATION.Q') REPLACE\" | runmqsc QM1"
docker exec ibm-mq bash -c "setmqaut -m QM1 -t queue -n NOTIFICATION.Q -p app +put +get +browse +inq"
```

## Configuration (`application.properties`)

```properties
ibm.mq.queue-manager=QM1
ibm.mq.channel=DEV.APP.SVRCONN
ibm.mq.conn-name=localhost(1414)
ibm.mq.user=app
ibm.mq.password=passw0rd
ibm.mq.queue=NOTIFICATION.Q

spring.jms.listener.session-transacted=true
```

## Lancer l'application

```bash
mvn clean install
mvn spring-boot:run
```

## API REST disponibles

| Méthode | Endpoint | Description |
|---|---|---|
| `GET` | `/messages` | Liste tous les messages stockés en base |
| `GET` | `/messages/{id}` | Récupère un message par son ID |
| `POST` | `/messages` | Envoie un message de test sur la file (corps en texte brut) |
| `GET` | `/messages/queue-status` | Nombre de messages encore en attente sur la file |
| `GET` | `/messages/queue-status/preview` | Aperçu du contenu des messages en attente, sans les consommer |
| `GET` | `/messages/stats` | Nombre total de messages persistés en base |

## Tester avec Postman

```
POST https://localhost:9443/ibmmq/rest/v2/messaging/qmgr/QM1/queue/NOTIFICATION.Q/message
```
Auth Basic : `app` / `passw0rd` — Header `ibm-mq-rest-csrf-token: dummy` — corps en texte brut.

Ou directement via l'API de l'application :
```
POST http://localhost:8080/messages
```

## Résilience et performance

- `session-transacted=true` : l'acquittement MQ n'a lieu que si la sauvegarde en base réussit, évitant la perte de messages.
- `@Retryable` sur le listener : 3 tentatives avec backoff en cas d'échec.
- Traitement message par message (pas de batch en mémoire) pour éviter la perte ou la duplication de messages en cas de crash.
