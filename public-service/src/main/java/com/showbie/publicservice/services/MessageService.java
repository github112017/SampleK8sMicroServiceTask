package com.showbie.publicservice.services;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Utility class for generating messages
 */
@Service
public class MessageService {

    List<String> phrases = new ArrayList<>(Arrays.asList(
        "Delight the world with compassion, kindness and grace.",
        "The early bird gets the worm, but the second mouse gets the cheese.",
        "Some days you are pigeon, some days you are statue. Today, bring umbrella.",
        "Be on the alert to recognize your prime at whatever time of your life it may occur.",
        "Your reality check about to bounce.",
        "Tension is who you think you should be. Relaxation is who you are.",
        "When blind leading the blind... get out of the way.",
        "Everyone seems normal until you get to know them.",
        "Only difference between a rut and a grave is depth.",
        "Experience is what you have left when everything else gone.",
        "A closed mouth gathers no feet.",
        "A conclusion is simply the place where you got tired of thinking.",
        "A cynic is only a frustrated optimist.",
        "Your road to glory will be rocky but fulfilling.",
        "Courage is not simply one of the virtues, but the form of every virtue at the testing point.",
        "Patience is your ally at the moment. Don’t worry!",
        "Nothing is impossible to a willing heart.",
        "Don’t worry about money. The best things in life are free.",
        "Don’t pursue happiness – create it.",
        "If at first you don’t succeed, skydiving not for you.",
        "Ninety-nine percent of all lawyers give the rest a bad name.",
        "Easiest way to find a lost object is to buy a replacement.",
        "Inside every old person is a young person wondering what the hell happened.",
        "You are cleverly disguised as a responsible adult.",
        "Tomorrow at breakfast, listen carefully: do what the rice krispies tell you to.",
        "Drive like hell, you will get there.",
        "Hard work pays off in the future. Laziness pays off now.",
        "You think it’s a secret, but they know.",
        "If a turtle doesn’t have a shell, is it naked or homeless?",
        "Change is inevitable, except for vending machines.",
        "A fanatic is one who can’t change his mind and won’t change the subject.",
        "If you look back, you’ll soon be going that way.",
        "An alien of some sort will be appearing to you shortly.",
        "Courage is not the absence of fear; it is the conquest of it.",
        "Nothing is so much to be feared as fear.",
        "All things are difficult before they are easy.",
        "The real kindness comes from within you.",
        "A ship in harbor is safe, but that’s not why ships are built.",
        "It's okay to look at the past and future. Just don’t stare.",
        "A wise person needs either good manners or fast reflexes.",
        "You will soon have an out of money experience.",
        "One tequila, two tequila, three tequila, floor.",
        "The older you get, the better you were.",
        "Age is high price to pay for maturity.",
        "Procrastination is art of keeping up with yesterday.",
        "A fool and his money are soon partying.",
        "Do not mistake temptation for opportunity.",
        "Flattery will go far tonight.",
        "He who laughs at himself never runs out of things to laugh at.",
        "He who laughs last is laughing at you.",
        "He who throws dirt is losing ground.",
        "Someone will invite you to a Karaoke party.",
        "That wasn’t chicken.",
        "There is no mistake so great as that of being always right.",
        "You don’t need strength to let go of something. What you really need is understanding.",
        "If you want the rainbow, you have to tolerate the rain.",
        "Fear is interest paid on a debt you may not owe.",
        "Hardly anyone knows how much is gained by ignoring the future.",
        "The wise man is the one that makes you think that he is dumb.",
        "The usefulness of a cup is in its emptiness.",
        "He who throws mud loses ground.",
        "Success lies in the hands of those who wants it.",
        "To avoid criticism, do nothing, say nothing, be nothing.",
        "One that would have the fruit must climb the tree.",
        "Probability of being seen directly proportional to stupidity of act.",
        "He who dies with most toys, still dies.",
        "Person who rests on laurels gets thorn in backside.",
        "Practice safe eating. Always use condiments.",
        "Person who give self-haircut after rice wine will be buzzed.",
        "Politicians are like diapers: change often, and for same reason.",
        "Fat person not afraid of heights – afraid of widths.",
        "You have kleptomania. Take something for it.",
        "When marriage outlawed, only outlaws have in-laws.",
        "We don’t know the future, but here’s a cookie.",
        "The world may be your oyster, but it doesn’t mean you’ll get its pearl.",
        "You will be hungry again in one hour.",
        "Don’t behave with cold manners.",
        "Don’t forget you are always on our minds.",
        "Message not found? Abort, Retry, Ignore.",
        "It takes less time to do a thing right than it does to explain why you did it wrong.",
        "Big journeys begin with a single step.",
        "Of all our human resources, the most precious is the desire to improve.",
        "Do the thing you fear, and the death of fear is certain."
    ));

    /**
     * Randomly generates a message
     *
     * @return returns a random string
     */
    public String getPhrase() {
        Random random = new Random();
        return phrases.get(random.nextInt(phrases.size()));
    }
}
