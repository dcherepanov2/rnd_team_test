import java.util.*;

public class RndMain {

    public static void main(String[] args) {//если считать, что слова всегда раздиляются одним пробелом
        Date date = new Date();
        List<String> words = new ArrayList<>(Arrays.asList("da da", "da", "ф ф", "ф", "da", "ф ф", "ф ф","da da", "da", "ф ф", "ф ф", "da", "ф ф", "ф ф","da da", "da", "ф ф", "ф ф", "da", "ф ф", "ф ф","da da", "da", "ф ф", "ф ф", "da", "ф ф", "ф ф","da da", "da", "ф ф", "ф ф", "da", "ф ф", "ф ф","da da", "da", "ф ф", "ф ф", "da", "ф ф", "ф ф","da da", "da", "ф ф", "ф ф", "da", "ф ф", "ф ф","da da", "da", "ф ф", "ф ф", "da", "ф ф", "ф ф","da da", "da", "ф ф", "ф ф", "da", "ф ф", "ф ф","da da", "da", "ф ф", "ф ф", "da", "ф ф", "ф ф","da da", "da", "ф ф", "ф ф", "da", "ф ф", "ф ф","da da", "da", "ф ф", "ф ф", "da", "ф ф", "ф ф","da da", "da", "ф ф", "ф ф", "da", "ф ф", "ф ф","da da", "da", "ф ф", "ф ф", "da", "ф ф", "ф ф","da da", "da", "ф ф", "ф ф", "da", "ф ф", "ф ф","da da", "da", "ф ф", "ф ф", "da", "ф ф", "ф ф","da da", "da", "ф ф", "ф ф", "da", "ф ф", "ф ф","da da", "da", "ф ф", "ф ф", "da", "ф ф", "ф ф","da da", "da", "ф ф", "ф ф", "da", "ф ф", "ф ф","da da", "da", "ф ф", "ф ф", "da", "ф ф", "ф ф","da da", "da", "ф ф", "ф ф", "da", "ф ф", "ф ф"));
        Map<String,Integer> counter = new HashMap<>();
        int counterMax = 0;
        String stringMax = "";
        while (words.size() != 0){
            int j = 0;
            counter.put(words.get(j),1);
            for(int i = j+1;i< words.size();i++){
                if(words.get(j).equals(words.get(i))) {
                    int counterLocal = counter.get(words.get(j)) + 1;
                    counter.put(words.get(j), counterLocal);
                    if(counterLocal>counterMax){
                        counterMax = counterLocal;
                        stringMax = words.get(j);
                    }
                }
            }
            words.removeAll(Collections.singleton(words.get(j)));
        }
        Date date1 = new Date();
        System.out.println(date1.getTime() - date.getTime());
        System.out.println("Количество слов в самом часто встречающемся слове: "+stringMax.split(" ").length);
    }
}
