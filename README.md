Coursework for Architetture Distribuite per il Cloud @ University of Salerno

# Architetture Distribuite per il Cloud
## Anno accademico 2019/2020
#### Prof. Alberto Negro
#### Prof. Gennaro Cordasco
#### Dtt. Carmine Spagnuolo

| Studente      | Matricola  |
| ------------- | ---------- |
| Francesco Manzo | 0522500667 |

## Descrizione del progetto assegnato
Progettazione e sviluppo un social network basato sugli interessi degli utenti che sfrutta una rete P2P. Il sistema collezione i profili degli utenti e crea automaticamente le amicizie in accordo ad una strategia di matching. Gli utenti possono vedere i loro amici in qualsiasi momento e sono informati quando un nuovo utente entra nel social e diventa un amico. Il sistema definisce un insieme di domande e le risposte relative vengono usate per definire la chiave-profilo di un utente. Attraverso la strategia di matching il sistema accoppia i vari profili degli utenti creando le amicizie. Il sistema permette all'utente di eseguire le seguenti azioni:

- Ricevere le domande dal social
- Creare una chiave-profilo utente
- Iscriversi al social
- Vedere gli amici
- Aggiungere una domanda 
- Mandare un messaggio ad un amico

## Soluzione proposta
### Docker
#### Build immagine
Per costruire l'immagine docker eseguire nella cartella ```SEMANTIC_HARMONY_SOCIAL_NETWORK/``` il seguente comando:

`docker build --no-cache -t semantic-social .`

#### Eseguire i peer

Per eseguire invece il master-peer eseguire:

`docker run -i --name master-peer -e MASTERIP="127.0.0.1" -e ID=0 semantic-social`

Una volta che il master peer è in esecuzione possono essere eseguiti altri peer. Per farlo bisogna controllare l'indirizzo del container che sta eseguendo il master peer. Quindi: 

- eseguire ` docker ps ` per ottenere l'ID del container 
- eseguire ` docker inspect <container ID> ` in modo da ottenere l'indirizzo IP.

Una volta ottenuto l'indirizzo è possibile eseguire un altro peer ricordando di variare l'ID univoco, ad esempio:

`docker run -i --name peer1 -e MASTERIP="172.17.0.2" -e ID=1 semantic-social`

### Descrizione del progetto

#### Struttura del progetto
Usando Maven sono state aggiunte le varie dipendenze.
```
<repositories>
    <repository>
        <id>tomp2p.net</id>
        <url>http://tomp2p.net/dev/mvn/</url>
    </repository>
</repositories>
<dependencies>
    <dependency>
        <groupId>net.tomp2p</groupId>
        <artifactId>tomp2p-all</artifactId>
        <version>5.0-Beta8</version>
    </dependency>
    <dependency>
        <groupId>junit</groupId>
        <artifactId>junit</artifactId>
        <version>4.12</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>args4j</groupId>
        <artifactId>args4j</artifactId>
        <version>2.33</version>
    </dependency>
</dependencies>
```
Il pacchetto `/src/main/java/it/unisa/semanticSocial` fornisce le seguenti classi JAVA:

- _Utente_ una classe per l'utente che mantiene le sue informazioni.
- _Question_ una classe per le domande, usata per aggiungerne di nuove al social.
- _ExistingNicknameException_ una eccezione per il nickname esistente. 
- _ObsoleteProfileException_ una eccezione per il profilo obsoleto.
- _MessageListener_ una interfaccia per il listener dei messaggi ricevuto da un peer.
- _SemanticHarmonySocialNetwork_ una interfaccia che espone le funzioni del social. 
- _SemanticHarmonySocialNetworkImpl_ una implementazione dell'interfaccia _SemanticHarmonySocialNetwork_ che usa la libreria TomP2P
- _Main_ una classe d'esempio che usa l'implemetazione _SemanticHarmonySocialNetworkImpl_

### Descrizione della soluzione

#### Richiedere domande
Un utente che intende iscriversi al social può richiedere che gli vengano fornite le domande.

#### Richiedere una chiave-utente
L'utente non può richiedere una chiave senza aver prima risposto alle domande. In tal caso viene visualizzato un errore.
Una volta, e solo dopo, aver risposto le domande, l'utente tramite le risposte può richiedere al social di fornirgli una chiave-profilo, utile per iscriversi al social.

#### Iscriversi al social
L'utente non può iscriversi al social senza aver prima chiesto una chiave-profilo.
A questo punto l'utente fornendo al social la propria chiave e il proprio nickname può iscriversi al social. In questa fase il social comunica all'utente i nomi degli altri utenti con cui la sua chiave ha fatto match. Questi suono i suoi amici.

#### Richiedere la lista di amici
L'utente non può chiedere la lista di amici se non è prima iscritto al social. 
Se lo è gli viene mostrata la lista di amici.

#### Aggiungere una domanda al social
L'utente non può aggiungere una domanda se non è prima iscritto al social. 
L'utente può creare una nuova domanda e aggiungerla al social. Così facendo fornisce nuove informazioni su stesso (e anche gli altri rispondendo alle nuove domande) creando un profilo più dettagliato e potenzialmente creare nuove amicizie.

#### Aggioranre la propria chiave-profilo
L'utente non può aggiornare la propria chiave se non è prima iscritto al social.
L'utente dopo aver risposto a nuove domande aggiorna la propria chiave-profilo.

#### Mandare un messaggio agli amici
L'utente non può mandare messaggi se non è prima iscritto al social.
L'utente può mandare messaggi ai propri amici e solo escusivamente a loro.

## Testing


| Test case                                           | Descrizione                                                                         | Comportamento atteso |
| --------------------------------------------------- | ----------------------------------------------------------------------------------- | -------------------- |
| getUserProfileQuestions                             | L'utente chiede le domande al sistema.                                              | L'utente ottiene la lista delle domande.                    |
| getUserProfileQuestions_withTwoPeer                 | Due utenti chiedono le domande al sistema senza altri eventi nel mezzo.             | I due utenti ottengono le stesse liste.                    |
| createAUserProfileKey_ErrorNotAnsweredQuestions     | L'utente chiede la chiave-profilo, ma non ha ancora chiesto la lista delle domande. | L'utente non ottiene la chiave profilo e gli viene mostrato un errore |
| createAUserProfileKey_NoErrorAndCreatedProfileKey   | L'utente chiede la chiave-profilo dopo aver richiesto la lista delle domande.       | L'utente ottiene la chiave profilo                   |
| join_ErrorProfileKeyNotCreated                      | L'utente chiede di iscriversi al sistema, senza aver chiesto la chiave-profilo.     | L'utente non si iscrive al social e gli viene mostrato un errore     |
| join_ErrorExistingNickname                          | L'utente chiede di iscriversi al sistema, ma il nickname fornito è gia in uso.      | L'utente non si iscrive al social e gli viene mostrato un errore              |
| join_NoErrorAndUserJoinsNetwork                     | L'utente chiede di iscriversi al sistema, seguendo la giusta procedura.             | L'utente si iscrive al social                    |
| getFriends_ErrorUserNotJoined                       | L'utente chiede la lista dei suoi amici, ma non è iscritto al sistema.              | L'utente non ottiene la lista di amici e gli viene mostrato un errore                    |
| getFriends_NoErrorFriendsGivenAndMatchWithEachOther | 4 utenti sono iscritti al sistema: master e peer1 hanno dato le stesse risposte; peer2 e peer3 hanno dato le stesse risposte, diverse, però, da quelle di master e peer1. Ciascuno chiede la lista degli amici. | Gli utenti master e peer1 sono amici a vicenda così come peer2 e peer3. master non è amico con peer2 e peer3. peer1 non è amico con peer2 e peer3. peer2 non è amico con master e peer1. peer3 non è amico con master e pee1. |
| getFriends_ErrorObsoleteProfile                     | L'utene chiede la lista dei suoi amici, ma il suo profilo è obsoleto.| L'utente non ottiene la lista di amici |
| updateProfile_ErrorOperationNotAllowedUserNotJoined | L'utene chiede di aggiornare il suo profilo, ma non è ancora iscitto e l'operazione non gli è permessa. | Nessun effetto al sistema |
| updateProfile_NoErrorProfilesUpdated                | Due utenti non sono amici dato che le risposte alle domande sono diverse. Dopo aver risposto alle nuove domande (in modo poi da risultare amici, aggiornano i loro profili | Le chiavi vengono aggiornate e i due utenti risultano ora amici |
| addQuestionToNetwork_ErrorUserNotJoined             | L'utente aggiunge una domanda al sistema, ma non è ancora iscritto. | L'utente non aggiunge la domanda e gli viene mostrato un errore |
| addQuestionToNetwork                                | L'utente aggiunge una domanda al sistema. | La domanda viene aggiunta al sistema |
| sendMessage_ErrorUserNotJoined                     | L'utene prova a inviare un messaggio ma non è iscritto | L'utente non manda il messaggio e gli viene mostrato un errore |
| sendMessage_NoErrorMessagesSent                     | L'utene invia un messaggio ad un amico e anche ad un non-amico. |  L'utente riesce a mandare il messaggio all'amico e non all'altro. |
| leaveNetwork_NoErrorUserLeavesNetwork               | L'utente master e peer 5 sono amici. peer5 lascia la rete.  | peer5 abbandona la rete, e master non lo ha più tra gli amici |



