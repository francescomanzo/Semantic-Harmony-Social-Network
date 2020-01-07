package it.unisa.semanticSocial;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Question implements Serializable {

    private String questionText;
    private List<String> answers;

    public Question(String text) {
        questionText = text;
        answers = new ArrayList<>();
    }

    public Question addAnswer(String answerText){
        answers.add(answerText);
        return this;
    }

    public String getQuestion(){
        return questionText;
    }

    public List<String> getAnswers(){
        return answers;
    }
}
