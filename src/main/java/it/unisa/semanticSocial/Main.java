package it.unisa.semanticSocial;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    @Option(name = "-m", aliases = "--masterip", usage = "the master peer ip address", required = true)
    private static String master;

    @Option(name = "-id", aliases = "--identifierpeer", usage = "the unique identifier for this peer", required = true)
    private static int id;

    public static void main(String[] args) {

        class MessageListenerImpl implements MessageListener {
            int peerid;

            public MessageListenerImpl(int peerid) {
                this.peerid = peerid;

            }

            public Object parseMessage(Object obj) {
                System.out.println("NOTIFICA - " + obj.toString());
                return "success";
            }

        }

        Main m = new Main();
        final CmdLineParser parser = new CmdLineParser(m);

        SemanticHarmonySocialNetworkImpl impl = null;

        try {
            parser.parseArgument(args);

            Scanner keyboard = new Scanner(System.in);
            String scelta = "";
            List<String> domande = null;
            List<Integer> risposte = new ArrayList<Integer>();
            String nickname = "";
            System.out.println("Scrivi il tuo nickaname");
            impl = new SemanticHarmonySocialNetworkImpl(id, master, new MessageListenerImpl(id));

            while (true) {
                System.out.println();
                System.out.println("r - RISPONDI ALLE DOMANDE");
                System.out.println("    Rispondi alle domande per descrivere la tua personalità, così potrai iscriverti!");
                System.out.println("i - ISCRIVITI AL SOCIAL");
                System.out.println("    Iscrivendoti al social avrai nuovi amici, persone che sono d'accordo con la tua personalità.");
                System.out.println("a - VISUALIZZA AMICI");
                System.out.println("m - MANDA UN MESSAGGIO");
                System.out.println("    Puoi scrivere un messaggio ad un tuo amico.");
                System.out.println("d - AGGIUNGI UNA DOMANDA");
                System.out.println("e - ESCI E LASCIA LA RETE");
                System.out.println();

                scelta = keyboard.nextLine();

                try {
                    switch (scelta) {
                        case "r":
                            domande = impl.getUserProfileQuestions();
                            if (domande.isEmpty()) {
                                System.out.println("[INFO] Non ci sono domande non risposte.");
                                break;
                            }
                            System.out.println("[INFO] Ci sono domande non risposte, eccole:");
                            for (String d : domande) {
                                System.out.println(d);
                                risposte.add(keyboard.nextInt());
                                keyboard.nextLine();
                            }

                            impl.updateProfile(impl.createAUserProfileKey(risposte));
                            break;
                        case "d":
                            System.out.println("Scrivi il testo della domanda: ");
                            String domanda = keyboard.nextLine();
                            Question q = new Question(domanda);
                            int i = 1;
                            while (true) {
                                System.out.println("Inserisci la risposta numero " + i + ". (premi 0 per terminare)");
                                i++;
                                String s = keyboard.next();
                                keyboard.nextLine();
                                if (s.equals("0"))
                                    break;
                                q.addAnswer(s);
                            }
                            impl.addQuestionToNetwork(q);
                            break;
                        case "i":
                            System.out.println("Inserisci il tuo nickname: ");
                            nickname = keyboard.next();
                            keyboard.nextLine();
                            boolean riuscito = false;
                            riuscito = impl.join(impl.createAUserProfileKey(risposte), nickname);
                            if (!riuscito)
                                System.out.println("[ERRORE] Iscrizione fallita! Riprova.");
                            else {
                                System.out.println("[INFO] Ti sei iscritto con successo!");
                            }
                            break;
                        case "a":
                            List<String> friends = new ArrayList<>();
                            friends = impl.getFriends();
                            if(friends.isEmpty())
                                System.out.println("[INFO] Non hai ancora amici.");
                            else{
                                System.out.println("[INFO] Ecco i tuoi amici: ");
                                for (String f : friends)
                                    System.out.println(f);
                            }
                            break;
                        case "m":
                            System.out.println("A chi vuoi mandare un messaggio? Scrivi il suo nickname: ");
                            String target = keyboard.next();
                            keyboard.nextLine();
                            System.out.println("Ora scrivi il messaggio: ");
                            String message = keyboard.nextLine();
                            if (impl.sendMessage(target, message))
                                System.out.println("[INFO] Messaggio inviato.");
                            else
                                System.out.println("[ERRORE] Qualcosa è andato storto, controlla che siete amici oppure che il nickname esiste.");
                            break;
                        case "e":
                            impl.leaveNetwork();
                            System.exit(0);
                            break;

                        default:
                            break;
                    }
                }
                catch(Exception e){
                    System.out.println(e.getMessage());

                }
            }
        } catch (CmdLineException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            if (impl != null) {
                try {
                    impl.leaveNetwork();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
