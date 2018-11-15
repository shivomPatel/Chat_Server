import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class ChatFilter {

    private ArrayList<String> badWords;

    public ChatFilter(String badWordsFileName) {

        badWords = new ArrayList<>();
        File f = new File(badWordsFileName);
        try {
            Scanner sc = new Scanner(f);
            while (sc.hasNextLine()) {
                badWords.add(sc.nextLine());
            }
        } catch (FileNotFoundException e) {
            System.out.println("File was not found.");
        }

    }

    public String filter(String msg) {
        for (int i = 0; i < badWords.size(); i++) {
            String replace = "";
            for (int j = 0; j < badWords.get(i).length(); j++) {
                replace += "*";
            }
            //TODO what happens if username has a bad word
            msg = msg.replaceAll("(?i)" + badWords.get(i), replace); // TODO need to fix this

        }
        return msg;
    }

    public static void main(String[] args) {
        ChatFilter filter = new ChatFilter("/Users/bwu2018/IdeaProjects/Project 4/src/badwords.txt");
//        for (int i = 0; i < filter.badWords.size(); i++) {
//            System.out.println(filter.badWords.get(i));
//        }

        System.out.println(filter.filter("IU"));
    }
}
