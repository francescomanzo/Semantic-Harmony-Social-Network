import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.*;

import it.unisa.semanticSocial.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SemanticHarmonySocialNetworkImplTest {
    SemanticHarmonySocialNetworkImpl master;
    SemanticHarmonySocialNetworkImpl peer1;
    SemanticHarmonySocialNetworkImpl peer2;
    SemanticHarmonySocialNetworkImpl peer3;

    @Before
    public void init() {
        MessageListener listener = new MessageListener() {
            @Override
            public Object parseMessage(Object obj) {
                return null;
            }
        };
        try {
            master = new SemanticHarmonySocialNetworkImpl(0, "127.0.0.1", listener);
            peer1 = new SemanticHarmonySocialNetworkImpl(1, "127.0.0.1", listener);
            peer2 = new SemanticHarmonySocialNetworkImpl(2, "127.0.0.1", listener);
            peer3 = new SemanticHarmonySocialNetworkImpl(3, "127.0.0.1", listener);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @After
    public void finish() {
        try {
            master.leaveNetwork();
            peer1.leaveNetwork();
            peer2.leaveNetwork();
            peer3.leaveNetwork();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void getUserProfileQuestions() {
        List<String> domande = master.getUserProfileQuestions();
        assertNotNull(domande);
        assertEquals(6, domande.size());
    }
    @Test
    public void getUserProfileQuestions_withTwoPeer() {
        List<String> domande = master.getUserProfileQuestions();
        List<String> domande2 = peer2.getUserProfileQuestions();
        assert(domande.equals(domande2));
    }

    @Test(expected = Exception.class)
    public void createAUserProfileKey_ErrorNotAnsweredQuestions() throws Exception {
        master.createAUserProfileKey(new ArrayList<Integer>()); //
    }

    @Test
    public void createAUserProfileKey_NoErrorAndCreatedProfileKey(){
        String profile_key = null;
        try {
            master.getUserProfileQuestions();  // questions
            List<Integer> risposte_tipo1 = new ArrayList<>();
            risposte_tipo1.add(1);
            risposte_tipo1.add(1);
            risposte_tipo1.add(1);
            risposte_tipo1.add(1);
            risposte_tipo1.add(1);
            risposte_tipo1.add(1);  // answers
            profile_key = master.createAUserProfileKey(risposte_tipo1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotNull(profile_key);
        assertEquals("111111", profile_key);
    }

    @Test(expected = Exception.class)
    public void join_ErrorProfileKeyNotCreated() throws Exception {
        boolean result = master.join("111111", "master");
    }

    @Test(expected = Exception.class)
    public void join_ErrorExistingNickname() throws Exception {
        master.getUserProfileQuestions();
        List<Integer> risposte_tipo1 = new ArrayList<>();
        risposte_tipo1.add(1);
        risposte_tipo1.add(1);
        risposte_tipo1.add(1);
        risposte_tipo1.add(1);
        risposte_tipo1.add(1);
        risposte_tipo1.add(1);
        String profile_key_master = master.createAUserProfileKey(risposte_tipo1);
        boolean result = master.join(profile_key_master, "master");
        peer1.getUserProfileQuestions();
        String profile_key_peer1 = peer1.createAUserProfileKey(risposte_tipo1);
        peer1.join(profile_key_peer1,"master"); // existing nickname
    }

    @Test
    public void join_NoErrorAndUserJoinsNetwork(){
        master.getUserProfileQuestions();
        List<Integer> risposte_tipo1 = new ArrayList<>();
        risposte_tipo1.add(1);
        risposte_tipo1.add(1);
        risposte_tipo1.add(1);
        risposte_tipo1.add(1);
        risposte_tipo1.add(1);
        risposte_tipo1.add(1);
        String profile_key_master = null;
        boolean result = false;
        try {
            profile_key_master = master.createAUserProfileKey(risposte_tipo1);
            result = master.join(profile_key_master, "master");
        } catch (Exception e) {
            e.printStackTrace();
        }

        assert(result);
    }

    @Test(expected = Exception.class)
    public void getFriends_ErrorUserNotJoined() throws Exception{
        List<String> friends = master.getFriends();
    }

    @Test
    public void getFriends_NoErrorFriendsGivenAndMatchWithEachOther(){
        try {
            master.getUserProfileQuestions();
            List<Integer> risposte_tipo1 = new ArrayList<>();
            risposte_tipo1.add(1);
            risposte_tipo1.add(1);
            risposte_tipo1.add(1);
            risposte_tipo1.add(1);
            risposte_tipo1.add(1);
            risposte_tipo1.add(1);
            peer1.getUserProfileQuestions();
            List<Integer> risposte_tipo2 = new ArrayList<>();
            risposte_tipo2.add(1);
            risposte_tipo2.add(1);
            risposte_tipo2.add(1);
            risposte_tipo2.add(1);
            risposte_tipo2.add(2);
            risposte_tipo2.add(2);
            peer2.getUserProfileQuestions();
            peer3.getUserProfileQuestions();
            String profile_key_master = master.createAUserProfileKey(risposte_tipo1);
            boolean result = master.join(profile_key_master, "master");
            String profile_key_peer1 = peer1.createAUserProfileKey(risposte_tipo1);
            boolean result2 = peer1.join(profile_key_peer1,"peer1");
            String profile_key_peer2 = peer2.createAUserProfileKey(risposte_tipo2);
            boolean result3 = peer2.join(profile_key_peer2, "peer2");
            String profile_key_peer3 = peer3.createAUserProfileKey(risposte_tipo2);
            boolean result4 = peer3.join(profile_key_peer3, "peer3");

            List<String> friends = master.getFriends();
            List<String> friends1 = peer1.getFriends();
            List<String> friends2 = peer2.getFriends();
            List<String> friends3 = peer3.getFriends();

            assert(friends.contains("peer1"));
            assert(friends1.contains("master"));
            assert(friends3.contains("peer2"));
            assert(friends2.contains("peer3"));

            assert(!friends2.contains("master"));
            assert(!friends3.contains("master"));

            assert(!friends2.contains("peer1"));
            assert(!friends3.contains("peer1"));

            assert(!friends.contains("peer2"));
            assert(!friends.contains("peer3"));

            assert(!friends1.contains("peer2"));
            assert(!friends1.contains("peer3"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test(expected = Exception.class)
    public void getFriends_ErrorObsoleteProfile() throws Exception {
        master.getUserProfileQuestions();
        List<Integer> risposte_tipo1 = new ArrayList<>();
        risposte_tipo1.add(1);
        risposte_tipo1.add(1);
        risposte_tipo1.add(1);
        risposte_tipo1.add(1);

        peer1.getUserProfileQuestions();
        List<Integer> risposte_tipo2 = new ArrayList<>();
        risposte_tipo2.add(1);
        risposte_tipo2.add(1);
        risposte_tipo2.add(1);
        risposte_tipo2.add(1);
        risposte_tipo2.add(2);
        risposte_tipo2.add(2);

        String profile_key_master = master.createAUserProfileKey(risposte_tipo1);
        boolean result = master.join(profile_key_master, "master");
        String profile_key_peer1 = peer1.createAUserProfileKey(risposte_tipo2);
        boolean result2 = peer1.join(profile_key_peer1,"peer1");
        List<String> friends = master.getFriends();
    }

    @Test
    public void updateProfile_ErrorOperationNotAllowedUserNotJoined() {
        boolean result = false;
        try {
            result = master.updateProfile("111111");
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert(!result);
    }

    @Test
    public void updateProfile_NoErrorProfilesUpdated(){
        try {
            master.getUserProfileQuestions();
            List<Integer> risposte_tipo1 = new ArrayList<>();
            risposte_tipo1.add(1);
            risposte_tipo1.add(1);
            risposte_tipo1.add(1);
            risposte_tipo1.add(1);
            risposte_tipo1.add(1);
            risposte_tipo1.add(1);
            peer1.getUserProfileQuestions();
            List<Integer> risposte_tipo2 = new ArrayList<>();
            risposte_tipo2.add(1);
            risposte_tipo2.add(1);
            risposte_tipo2.add(1);
            risposte_tipo2.add(1);
            risposte_tipo2.add(2);
            risposte_tipo2.add(2);

            String profile_key_master = master.createAUserProfileKey(risposte_tipo1);
            boolean result = master.join(profile_key_master, "master");
            String profile_key_peer1 = peer1.createAUserProfileKey(risposte_tipo2);
            boolean result2 = peer1.join(profile_key_peer1,"peer1");
            List<String> friends = master.getFriends();
            assert(!friends.contains("peer1"));
            risposte_tipo1.add(1);
            risposte_tipo1.add(2);
            risposte_tipo1.add(3);
            risposte_tipo1.add(4);
            risposte_tipo2.add(1);
            risposte_tipo2.add(2);
            risposte_tipo2.add(3);
            risposte_tipo2.add(4);
            master.updateProfile(master.createAUserProfileKey(risposte_tipo1));
            peer1.updateProfile(peer1.createAUserProfileKey(risposte_tipo1));
            friends = master.getFriends();
            assert(friends.contains("peer1"));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test(expected = Exception.class)
    public void addQuestionToNetwork_ErrorUserNotJoined() throws Exception {
        master.addQuestionToNetwork(new Question("Sei generoso?"));

    }

    @Test
    public void addQuestionToNetwork(){

        try {
            master.getUserProfileQuestions();

            List<Integer> risposte_tipo1 = new ArrayList<>();
            risposte_tipo1.add(1);
            risposte_tipo1.add(1);
            risposte_tipo1.add(1);
            risposte_tipo1.add(1);
            risposte_tipo1.add(1);
            risposte_tipo1.add(1);

            master.getUserProfileQuestions();
            String profile_key_master = master.createAUserProfileKey(risposte_tipo1);
            boolean result = master.join(profile_key_master, "master");
            Question q = new Question("Ti piace l'hashtag #enjoy?");
            q.addAnswer("Sì");
            q.addAnswer("No");
            master.addQuestionToNetwork(q);
            List<String> domande = master.getUserProfileQuestions();
            assertEquals(1, domande.size());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test(expected = Exception.class)
    public void sendMessage_ErrorUserNotJoined() throws Exception {
        master.sendMessage("peer2", "Ciao");
    }

    @Test
    public void sendMessage_NoErrorMessagesSent(){
        try {
            master.getUserProfileQuestions();
            List<Integer> risposte_tipo1 = new ArrayList<>();
            risposte_tipo1.add(1);
            risposte_tipo1.add(1);
            risposte_tipo1.add(1);
            risposte_tipo1.add(1);
            risposte_tipo1.add(1);
            risposte_tipo1.add(1);

            peer1.getUserProfileQuestions();
            List<Integer> risposte_tipo2 = new ArrayList<>();
            risposte_tipo2.add(1);
            risposte_tipo2.add(1);
            risposte_tipo2.add(1);
            risposte_tipo2.add(1);
            risposte_tipo2.add(2);
            risposte_tipo2.add(2);
            String profile_key_master = master.createAUserProfileKey(risposte_tipo1);
            boolean result = master.join(profile_key_master, "master");
            String profile_key_peer1 = peer1.createAUserProfileKey(risposte_tipo1);
            boolean result2 = peer1.join(profile_key_peer1, "peer1");
            peer2.getUserProfileQuestions();
            String profile_key_peer2 = peer2.createAUserProfileKey(risposte_tipo2);
            boolean result3 = peer2.join(profile_key_peer2, "peer2");
            boolean messageSent = master.sendMessage("peer1", "Ciao");
            assert(messageSent);
            messageSent = master.sendMessage("peer2", "Ciao");
            assert(!messageSent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void leaveNetwork_NoErrorUserLeavesNetwork(){
        try {
            master.getUserProfileQuestions();
            List<Integer> risposte_tipo1 = new ArrayList<>();
            risposte_tipo1.add(1);
            risposte_tipo1.add(1);
            risposte_tipo1.add(1);
            risposte_tipo1.add(1);
            risposte_tipo1.add(1);
            risposte_tipo1.add(1);

            String profile_key_master = master.createAUserProfileKey(risposte_tipo1);
            boolean result = master.join(profile_key_master, "master");

            SemanticHarmonySocialNetworkImpl peer5 = new SemanticHarmonySocialNetworkImpl(4, "127.0.0.1", new MessageListener() {
                @Override
                public Object parseMessage(Object obj) {
                    return null;
                }
            });
            peer5.getUserProfileQuestions();
            String profile_key_peer5 = peer5.createAUserProfileKey(risposte_tipo1);
            boolean result2 = peer5.join(profile_key_peer5, "peer5");
            List<String> amici = master.getFriends();
            assert (amici.contains("peer5"));
            peer5.leaveNetwork();
            amici = master.getFriends();
            assert (!amici.contains("peer5"));
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
