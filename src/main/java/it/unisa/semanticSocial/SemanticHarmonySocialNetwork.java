package it.unisa.semanticSocial;

import java.util.List;

public interface SemanticHarmonySocialNetwork{
    /**
     * Gets the social network users questions.
     * @return a list of String that is the profile questions.
     */
    List<String> getUserProfileQuestions();

    /**
     * Creates a new user profile key according the user answers.
     * @param _answer a list of answers.
     * @return a String, the obtained profile key.
     */
    String createAUserProfileKey(List<Integer> _answer) throws Exception;

    /**
     * Joins in the Network. An automatic messages to each potential new friend is generated.
     * @param _profile_key a String, the user profile key according the user answers
     * @param _nick_name a String, the nickname of the user in the network.
     * @return true if the join success, fail otherwise.
     */
    boolean join(String _profile_key, String _nick_name) throws Exception;

    /**
     * Update a user profile key after answered new questions
     * @param _profile_key the new profile key
     * @return true if success, false otherwise.
     */
    boolean updateProfile(String _profile_key) throws Exception;

    /**
     * Gets the nicknames of all automatically creates friendships.
     * @return a list of String.
     */
    List<String> getFriends() throws Exception;

    /**
     * Create a new question and add it to the Network
     * @return true if success, false otherwise.
     */
    boolean addQuestionToNetwork(Question newQuestion) throws Exception;

    boolean sendMessage(String target, String message) throws Exception;
}