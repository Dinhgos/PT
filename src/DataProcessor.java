
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Třída zpracovává data načtené z třídy DataLoader
 * 
 * @author Xuan Toan Dinh a Václav Šíma
 * @version 1.0
 *
 */
public class DataProcessor {
	/**Celková cena dopravy*/
    private static int totalCost = 0;
    
    /**Počet továren (podle zadání)*/
    public static int factoryCount = 0;
    /**Počet supermarketů (podle zadání)*/
    public static int supermarketCount = 0;
    /**Počet druhů zboží (podle zadání)*/
    public static int goodsCount = 0;
    /**Počet dní (podle zadání)*/
    public static int daysCount = 0;

    /**Matice uchovávající informace o nákladech
     * Sloupce - supermarkety 
     * Řádky - továrny
     * */
    public static int[][] expenditureCosts;
    
    /**Matice uchovávající informace o zásobách
     * Sloupce - supermarkety
     * Řádky -  pocet druhu zbozi
     * */ 
    public static int[][] stocks;
    
    /**Matice uchovávající informace o produkci továren
     * Sloupce - továrny
     * Řádky - pocet druhu zbozi * pocet dni
     * */
    public static int[][] factoryProduction;
    
    /**Matice uchovávající informace o poptávce supermarketů
     * Sloupce - supermarkety
     * Řádky - pocet druhu zbozi * pocet dni
     * */
    public static int[][] goodsDemand;

    /**Instance FileWriteru pro zápis dat do výstupního souboru*/
    private static FileWriter myWriter;

    /**
     * Načte data pomocí třídy DataLoader
     * */
    public void loadData() {
        //načtení dat
        DataLoader dataLoader = new DataLoader();
        dataLoader.loadData();
        createOutput();
    }
    
    /**
     * Vstupní bod pro zpracování dat
     * Vypočte celkovou cenu a zapíše ji do souboru
     * @throws IOException chyba behem zapisu
     * */
    public void processData() throws IOException {
        long start = System.nanoTime();

        myWriter = new FileWriter("output.txt");

        //hlavní cyklus pro vypočtení celkové ceny
        for(int i = 0; i < Main.days; i++) {
            myWriter.write("Den: " + i + "\n");

            if (Main.storage) {
                printStorage();
            }

            for (int j = 0; j < goodsCount; j++) {
                for (int k = 0; k < supermarketCount; k++) {
                    // metoda rozhodne, jestli může poptávku splnit jenom pomocí zásob nebo ne
                    poptavkaMinusZasoby(i,j,k);
                }

                if (Main.smart_method && i < daysCount - 1) {
                    // metoda zkontroluje zásoby, bez téhle metody program vypíše 67 (1.přístup)
                    storageCheck(i,j);
                }
            }

            myWriter.write("\n");
        }

        if (Main.factory) {
            wastedProducts();
        }

        long finish = System.nanoTime();
        myWriter.write("Celkova cena prepravy: " + totalCost + "\n");
        myWriter.write("Celkovy cas: " + (finish-start)/1000000 + " ms");


        myWriter.close();
    }

    /**
     * Vypise kazdodenni rozpis skladovych zasob = matice "zasoby"
     */
    private void printStorage() throws IOException {
        myWriter.write("Storage: \n");

        for (int i = 0; i < goodsCount; i++) {
            for (int j = 0; j < supermarketCount; j++) {
                myWriter.write(stocks[i][j] + " ");
            }
            myWriter.write("\n");
        }

    }

    /**
     * Vypise matici "produkce"
     * taktez vypise kolik zbozi vyprodukovaly zbytecne
     */
    private void wastedProducts() throws IOException {
        myWriter.write("Wasted products: \n");


        for (int i = 0; i < daysCount * goodsCount; i++) {
            for (int j = 0; j < factoryCount; j++) {
                myWriter.write(factoryProduction[i][j] + " ");

            }
            myWriter.write("\n");
        }
    }

    /**
     * Vytvori txt soubor
     * do souboru se bude psat to bylo vypsano metodou System.out.println
     */
    private void createOutput() {
        try {
            File output = new File("output.txt");
            output.createNewFile();
        } catch(Exception e) {
            System.out.println("Could not create output file.");
        }
    }

    /**
     * Metoda zkontroluje, jestli všechny supermarkety mají zásoby.
     * Pokud ne zavolá metodu doplnZasoby, která doplní zásoby.
     * OptimalniZasoba je počet produktů, kterých se snaží každý supermarket dosáhnout.
     *
     * @param den určuje jaký den je
     * @param zbozi druh zboží
     */
    private void storageCheck(int den, int zbozi) {
        for (int i = 0; i < supermarketCount; i++) {
            if (stocks[zbozi][i] < Main.optReserve) {
                doplnZasoby(den, zbozi, i);
            }
        }
    }

    /**
     * Metoda doplnZasoby využívá stejnou metodu najdiTovarnu pro nalezení nejlevnéjší továrny. Jelikož bude potřebovat
     * jenom jednu nejlevnější továrnu a nemusí hledat další, tak nepotřebuje arraylist.
     *
     * @param den určuje jaký den je
     * @param zbozi druh zboží
     * @param supermarket index supermarketu
     */
    private static void doplnZasoby(int den, int zbozi, int supermarket) {
        List<Integer> nejlevnejsiTovarny = najdiNejlevnejsiTovarny(supermarket);
        int pocetChybejicihoZbozi = Main.optReserve - stocks[zbozi][supermarket];
        int counter = 0;

        do {
            if (factoryProduction[zbozi * daysCount + den][nejlevnejsiTovarny.get(counter)] >= pocetChybejicihoZbozi) {
                factoryProduction[zbozi * daysCount + den][nejlevnejsiTovarny.get(counter)] -= pocetChybejicihoZbozi;
                stocks[zbozi][supermarket] += pocetChybejicihoZbozi;
                totalCost += pocetChybejicihoZbozi * expenditureCosts[nejlevnejsiTovarny.get(counter)][supermarket];
                pocetChybejicihoZbozi = 0;
            } else {
                stocks[zbozi][supermarket] += factoryProduction[zbozi * daysCount + den][nejlevnejsiTovarny.get(counter)];
                pocetChybejicihoZbozi -= factoryProduction[zbozi * daysCount + den][nejlevnejsiTovarny.get(counter)];
                totalCost += factoryProduction[zbozi * daysCount + den][nejlevnejsiTovarny.get(counter)] * expenditureCosts[nejlevnejsiTovarny.get(counter)][supermarket];
                factoryProduction[zbozi * daysCount + den][nejlevnejsiTovarny.get(counter)] = 0;
            }

            counter++;
        } while (pocetChybejicihoZbozi != 0 && counter != nejlevnejsiTovarny.size());
    }

    /**
     * V Arraylistu jsou indexy nejlevnejsich tovaren
     * vsechny tovarny by meli mit stejnou cenu
     * funguje podobne jako metoda najdiTovarnu
     * @param supermarket index supermarketu
     * @return seznam nejlevnejsich tovaren
     */
    private static List<Integer> najdiNejlevnejsiTovarny(int supermarket) {
        List<Integer> nejlevnejsiTovarny = new ArrayList<>();
        int cenaNejlevnejsiTovarny = Integer.MAX_VALUE;

        for (int i = 0; i < factoryCount; i++) {
            if (expenditureCosts[i][supermarket] < cenaNejlevnejsiTovarny) {
                nejlevnejsiTovarny.clear();
                nejlevnejsiTovarny.add(i);
                cenaNejlevnejsiTovarny = expenditureCosts[i][supermarket];
            }

            else if (expenditureCosts[i][supermarket] == cenaNejlevnejsiTovarny) {
                nejlevnejsiTovarny.add(i);
            }
        }

        return nejlevnejsiTovarny;
    }

    /**
     * Metoda rozhodne, jestli může poptávku splnit jenom pomocí zásob nebo ne.
     * Pokud je poptávka menší nebo je rovna zásob, tak se jednoduše odečtou zásoby od poptávky.
     * Pokud je poptávka větší než zásoby, tak se zavolá metoda dovezZbozi.
     *
     * @param den určuje jaký den je
     * @param zbozi druh zboží
     * @param supermarket index supermarketu
     */
    private static void poptavkaMinusZasoby(int den, int zbozi, int supermarket) throws IOException {
        if(goodsDemand[zbozi * daysCount + den][supermarket] <= stocks[zbozi][supermarket]) {
            stocks[zbozi][supermarket] -= goodsDemand[zbozi * daysCount + den][supermarket];
            goodsDemand[zbozi * daysCount + den][supermarket] = 0;
        }
        else {
            goodsDemand[zbozi * daysCount + den][supermarket] -= stocks[zbozi][supermarket];
            stocks[zbozi][supermarket] = 0;
            dovezZbozi(den, zbozi, supermarket);
        }
    }

    /**
     * Metoda dovezZboží hledá nejlevnější továrnu pro daný supermarket pomocí metody najdiTovarnu.
     * Poté zjistí, jestli daná továrna má dost velkou produkci na to, aby splnila poptávku. Pokud ano tak jednoduše
     * odečte poptávku od produkce, pridá cenu do celkovaCena a sníží poptávku na nulu. V opačném případě nakoupí
     * všechno co vyprodukuje od nejlevnější továrny a poté přidá její index do arraylistu prazdnyTovarny a hledá
     * další nejlejnější továrnu. Tohle dělá tak dlouho, dokud nebude splněna poptávka nebo neprojede všechny továrny.
     * Nakonci žjistí jestli poptávka byla splněna nebo ne. Pokud nebude splněna poptávka vypíše se text, den ,zboží a supermarket.
     *
     * @param den určuje jaký den je
     * @param zbozi druh zboží
     * @param supermarket index supermarketu
     */
    private static void dovezZbozi(int den, int zbozi, int supermarket) throws IOException {

        ArrayList<Integer> prazdnyTovarny = new ArrayList<>();

        do {
            int indexNejlevnejsiTovarny = najdiTovarnu(supermarket, prazdnyTovarny);

            if (goodsDemand[zbozi * daysCount + den][supermarket] <= factoryProduction[zbozi * daysCount + den][indexNejlevnejsiTovarny]) {
                myWriter.write( goodsDemand[zbozi * daysCount + den][supermarket] + "x" + zbozi + " -> " + supermarket + "\n");
                factoryProduction[zbozi * daysCount + den][indexNejlevnejsiTovarny] -= goodsDemand[zbozi * daysCount + den][supermarket];
                totalCost += goodsDemand[zbozi * daysCount + den][supermarket] * expenditureCosts[indexNejlevnejsiTovarny][supermarket];
                goodsDemand[zbozi * daysCount + den][supermarket] = 0;

            } else {
                prazdnyTovarny.add(indexNejlevnejsiTovarny);
                myWriter.write( factoryProduction[zbozi * daysCount + den][indexNejlevnejsiTovarny] + "x" + zbozi + " -> " + supermarket + "\n");
                goodsDemand[zbozi * daysCount + den][supermarket] -= factoryProduction[zbozi * daysCount + den][indexNejlevnejsiTovarny];
                totalCost += factoryProduction[zbozi * daysCount + den][indexNejlevnejsiTovarny] * expenditureCosts[indexNejlevnejsiTovarny][supermarket];
                factoryProduction[zbozi * daysCount + den][indexNejlevnejsiTovarny] = 0;
            }

        } while (goodsDemand[zbozi * daysCount + den][supermarket] != 0 && prazdnyTovarny.size() != supermarketCount);

        if (prazdnyTovarny.size() == supermarketCount && goodsDemand[zbozi * daysCount + den][supermarket] > 0) {
            myWriter.write("Poptávka nebyla splněna!\n");
            myWriter.write("Den: " + den);
            myWriter.write(" Pocet: " + goodsDemand[zbozi * daysCount + den][supermarket]);
            myWriter.write(" Zboží: " + zbozi);
            myWriter.write(" Supermarket: " + supermarket + "\n");


        }
    }

    /**
     * Metoda hledá nejlevnějśí továrnu pro daný supermarket. Metoda porovnává ceny přepravy od každý továrny k danému
     * supermarketu. Dále kontroluje jestli od daná továrna není v arraylistu prazdnyTovarny. Pokud je přeskočí ji a
     * hledá další nejlevnější továrnu.
     *
     * @param supermarket index supermarketu
     * @param prazdnyTovarny je seznam, ze kterých už nemůže nakoupit
     * @return indexNejlevnejsiTovarny  index nejlevnější továrny
     */
    private static int najdiTovarnu(int supermarket, List<Integer> prazdnyTovarny) {
        int indexNejlevnejsiTovarny = 0;
        int cenaNejlevnejsiTovarny = Integer.MAX_VALUE;

        for (int i = 0; i < factoryCount; i++) {
            if (expenditureCosts[i][supermarket] < cenaNejlevnejsiTovarny && !prazdnyTovarny.contains(i)) {
                indexNejlevnejsiTovarny = i;
                cenaNejlevnejsiTovarny = expenditureCosts[i][supermarket];
            }
        }

        return indexNejlevnejsiTovarny;
    }
}
