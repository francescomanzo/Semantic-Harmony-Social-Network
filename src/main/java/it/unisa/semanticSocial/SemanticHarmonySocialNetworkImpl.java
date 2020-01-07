package it.unisa.semanticSocial;

import net.tomp2p.dht.*;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

public class SemanticHarmonySocialNetworkImpl implements SemanticHarmonySocialNetwork {

    final private Peer peer;
    final private PeerDHT _dht;
    final private int DEFAULT_MASTER_PORT = 4000;
    private int startFromIndex;
    private int step;
    private String _nick_name = "";

    public SemanticHarmonySocialNetworkImpl(int _id, String _master_peer, final MessageListener _listener) throws Exception {
        startFromIndex = 0;
        step = 0;

        peer = new PeerBuilder(Number160.createHash(_id)).ports(DEFAULT_MASTER_PORT + _id).start();
        _dht = new PeerBuilderDHT(peer).start();

        FutureBootstrap fb = peer.bootstrap().inetAddress(InetAddress.getByName(_master_peer)).ports(DEFAULT_MASTER_PORT).start();
        fb.awaitUninterruptibly();
        if (fb.isSuccess()) {
            peer.discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
        } else {
            throw new Exception("Error in master peer bootstrap.");
        }

        peer.objectDataReply((sender, request) -> _listener.parseMessage(request));
    }

    public List<String> getUserProfileQuestions() {
        List<Question> questions = new ArrayList<>();
        try {
            FutureGet futureGet = _dht.get(Number160.createHash("domande")).start();
            futureGet.awaitUninterruptibly();
            if (futureGet.isSuccess()) {

                if (futureGet.isEmpty()) {

                    questions.add(new Question("Quale animale ti piace di più?").addAnswer("Cane").addAnswer("Gatto"));
                    questions.add(new Question("Quale è il tuo colore preferito?").addAnswer("Rosso").addAnswer("Blu").addAnswer("Verde").addAnswer("Giallo"));
                    questions.add(new Question("Quale è il tuo sport preferito?").addAnswer("Calcio").addAnswer("Pallavolo").addAnswer("Nuoto").addAnswer("Danza").addAnswer("Formula1").addAnswer("MotoGP"));
                    questions.add(new Question("Caffè amaro o dolce?").addAnswer("Amaro").addAnswer("Dolce"));
                    questions.add(new Question("Ti piace l'hashtag #happiness?").addAnswer("Sì").addAnswer("No"));
                    questions.add(new Question("Sei timido o estroverso?").addAnswer("Timido").addAnswer("Estroverso"));

                    _dht.put(Number160.createHash("domande")).data(new Data(questions)).start().awaitUninterruptibly();
                } else {
                    questions = (List<Question>) futureGet.dataMap().values().iterator().next().object();
                }
            }

            List<String> domande = new ArrayList<String>();
            for (int c = startFromIndex; c < questions.size(); c++) {
                Question q = questions.get(c);
                String text = "";
                text += q.getQuestion();
                int i = 1;
                for (String s : q.getAnswers()) {
                    text += " " + i + ". " + s;
                    i++;
                }
                domande.add(text);
                startFromIndex++;
            }
            nextStep(1);
            return domande;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String createAUserProfileKey(List<Integer> _answer) throws Exception {
        if(step < 1)
            throw new Exception("[ERRORE] Rispondi prima alle domande.");
        String aKey = "";

        for (int a : _answer) {
            aKey += a;
        }
        nextStep(2);
        return aKey;
    }

    public boolean join(String _profile_key, String _nick_name) throws Exception {
        if(step < 2)
            throw new Exception("[ERRORE] Non hai richiesto la tua chiave-profilo.");
        try {
            FutureGet futureGet = _dht.get(Number160.createHash(_nick_name)).start();
            futureGet.awaitUninterruptibly();
            if (futureGet.isSuccess()) {
                if (!futureGet.isEmpty())
                    throw new Exception("[ERRORE] Iscrizione fallita! Nickname esistente.");
                FutureGet futureGet2 = _dht.get(Number160.createHash("lista_utenti")).start();
                futureGet2.awaitUninterruptibly();
                this._nick_name = _nick_name;
                if (futureGet2.isSuccess()) {
                    List<String> utenti = new ArrayList<String>();
                    if (!futureGet2.isEmpty())
                        utenti = (List<String>) futureGet2.dataMap().values().iterator().next().object();

                    FutureGet futureGet3;
                    for (String u : utenti) {
                        futureGet3 = _dht.get(Number160.createHash(u)).start();
                        futureGet3.awaitUninterruptibly();
                        User possibleFriend = (User) futureGet3.dataMap().values().iterator().next().object();
                        try {
                            if (areFriend(_profile_key, possibleFriend.getProfileKey())) {
                                System.out.println("[INFO] " + u + " è un tuo nuovo amico!");
                                FutureDirect f = _dht.peer().sendDirect(possibleFriend.getPeerAddress()).object("[INFO] " + _nick_name + " è un tuo nuovo amico!").start();
                                f.awaitUninterruptibly();
                            }
                        } catch (Exception e) {

                        }
                    }

                    User newUser = new User(_profile_key, _dht.peer().peerAddress());
                    utenti.add(_nick_name);
                    _dht.put(Number160.createHash("lista_utenti")).data(new Data(utenti)).start().awaitUninterruptibly();
                    _dht.put(Number160.createHash(_nick_name)).data(new Data(newUser)).start().awaitUninterruptibly();
                    nextStep(3);
                    return true;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateProfile(String _profile_key) throws Exception {
        if(step < 3)
            return false;
        try {
            User user = new User(_profile_key, _dht.peer().peerAddress());
            _dht.put(Number160.createHash(_nick_name)).data(new Data(user)).start().awaitUninterruptibly();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<String> getFriends() throws Exception {
        if(step < 3)
            throw new Exception("[ERRORE] Non sei ancora iscritto al social.");
        List<String> amici = null;
        try {
            FutureGet futureGet;
            futureGet = _dht.get(Number160.createHash("lista_utenti")).start();
            futureGet.awaitUninterruptibly();
            List<String> utenti = null;
            if (futureGet.isSuccess()) {
                utenti = (List<String>) futureGet.dataMap().values().iterator().next().object();
            }

            amici = new ArrayList<String>();

            for (String u : utenti) {
                futureGet = _dht.get(Number160.createHash(u)).start();
                futureGet.awaitUninterruptibly();
                User possibleFriend = (User) futureGet.dataMap().values().iterator().next().object();
                futureGet = _dht.get(Number160.createHash(_nick_name)).start();
                futureGet.awaitUninterruptibly();
                User myUser = (User) futureGet.dataMap().values().iterator().next().object();
                if (!u.equals(_nick_name) && areFriend(myUser.getProfileKey(), possibleFriend.getProfileKey())) {

                    amici.add(u);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return amici;
    }

    public boolean addQuestionToNetwork(Question newQuestion) throws Exception {
        if(step < 3)
            throw new Exception("[ERRORE] Non sei ancora iscritto al social.");
        List<Question> questions = null;
        try {
            FutureGet futureGet = _dht.get(Number160.createHash("domande")).start();
            futureGet.awaitUninterruptibly();
            if (futureGet.isSuccess()) {
                questions = (List<Question>) futureGet.dataMap().values().iterator().next().object();
                questions.add(newQuestion);

                FutureGet futureGet2 = _dht.get(Number160.createHash("lista_utenti")).start();
                futureGet2.awaitUninterruptibly();
                List<String> users = null;
                if (futureGet2.isSuccess()) {
                    users = (List<String>) futureGet2.dataMap().values().iterator().next().object();
                }

                _dht.put(Number160.createHash("domande")).data(new Data(questions)).start().awaitUninterruptibly();

                FutureGet futureGet3;
                for (String u : users) {
                    futureGet3 = _dht.get(Number160.createHash(u)).start();
                    futureGet3.awaitUninterruptibly();
                    User user = (User) futureGet3.dataMap().values().iterator().next().object();
                    FutureDirect f = _dht.peer().sendDirect(user.getPeerAddress()).object("[INFO] Sono state aggiunte altre domande! Rispondi di nuovo alle domande per avere un profilo aggiornato.").start();
                    f.awaitUninterruptibly();
                }
                return true;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean areFriend(String firstKey, String secondKey) throws Exception {
        if (firstKey.length() < secondKey.length()) {
            throw new Exception("[ERRORE] Hai un profilo obsoleto, rispondi di nuovo alle domande.");
        }
        int c = 0;
        for (int i = 0; i < secondKey.length(); i++) {
            if (firstKey.charAt(i) != secondKey.charAt(i))
                c++;
        }
        if (c < firstKey.length() / 4)
            return true;
        return false;
    }

    public boolean leaveNetwork() {
        try {
            FutureGet futureGet = _dht.get(Number160.createHash("lista_utenti")).start();
            futureGet.awaitUninterruptibly();

            List<String> utenti;

            if (futureGet.isSuccess()) {
                if (!futureGet.isEmpty()) {
                    utenti = (List<String>) futureGet.dataMap().values().iterator().next().object();
                    utenti.remove(this._nick_name);
                    _dht.put(Number160.createHash("lista_utenti")).data(new Data(utenti)).start().awaitUninterruptibly();
                }
            }

            _dht.remove(Number160.createHash(this._nick_name));

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        _dht.peer().shutdown();
        return true;
    }

    public boolean sendMessage(String target, String message) throws Exception {
        if(step < 3)
            throw new Exception("[ERRORE] Non sei ancora iscritto al social.");
        try {
            FutureGet futureGet = _dht.get(Number160.createHash(target)).start();
            futureGet.awaitUninterruptibly();

            if (futureGet.isSuccess()) {
                if (!futureGet.isEmpty()) {
                    User targetUser = (User) futureGet.dataMap().values().iterator().next().object();

                    futureGet = _dht.get(Number160.createHash(_nick_name)).start();
                    futureGet.awaitUninterruptibly();
                    User myself = (User) futureGet.dataMap().values().iterator().next().object();

                    if(areFriend(targetUser.getProfileKey(), myself.getProfileKey())) {
                        FutureDirect futureDirect = _dht.peer().sendDirect(targetUser.getPeerAddress()).object("[MESSAGE] " + _nick_name + ": " + message).start();
                        futureDirect.awaitUninterruptibly();
                        return true;
                    }
                    else
                        return false;
                } else
                    return false;
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void nextStep(int fromStep){
        if(step < fromStep)
            step++;
    }
}