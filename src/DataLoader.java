import java.io.File;
import java.util.Scanner;
/**
 * Třída pro načtení dat ze souborů
 * a inicializaci datových struktur pro práci s daty
 * @author Xuan Toan Dinh a Václav Šíma
 * @version 1.0
 *
 */
public class DataLoader {

	/**Cesta k souboru*/
	private final String path = "data/real_large.txt";

	/**True pokud jsou nastevny velikosti polí (Převoz, Zásoby,Poptávka, Produkce)
	 *korespondující s úvodními hodnotami souboru*/
	private boolean initLoaded = false;

	/**
	 * Uložení všech 2D polí do jednoho 3D pole
	 * Ukládány v tomto pořadí:
	 * c = cena převozu
	 * q = počateční skladové zásoby
	 * p = produkce továren
	 * r = poptávka zboží
	 * */
	private int[][][] c_q_p_r;


	/**
	 * Metoda pro načtení vstupních dat
	 * Pomocí while cyklu iteruje skrz jednotlivé řádky a ukládá je
	 * do příslušeného pole ve formě integeru
	 * */
	public void loadData() {
		//counter pro řádky v matici
		int counter = 0;

		/*counter pro jednotlivé matice
		 * 0 = cena prevozu, 1 = zásoby, 2 = produkce, 3 = poptavka
		 * */
		int cycle = 0;

		//scanner pro načtení dat
		Scanner sc = null;

		try {
			File fileWithData = new File(path);
			sc = new Scanner(fileWithData);

			while (sc.hasNextLine()) {
				String line = sc.nextLine();

				// vyfiltrování validních dat
				if (!line.isBlank() && !(line.charAt(0) == '#')) {

					//rozdělení dat do pole podle " "
					String[] data = line.split(" ");

					int rowLength = 0;
					for(int i = 0; i < data.length; i++) {
						if(!data[i].isBlank()) {
							rowLength++;
						}
					}

					int[] rowInt = new int[rowLength];

					// přetypování řádku na pole int
					for (int i = 0; i < rowInt.length; i++) {
						//double space fix pro real_small_sink - vyfiltrovani mezery
						if(data[i].equals(""))
						{
							continue;
						}
						rowInt[i] = Integer.parseInt(data[i]);
					}

					// načtění vstupních parametrů scénáře
					if (!initLoaded) {
						loadInitData(rowInt);
					}

					// kopirovani radku do pole
					else {
						System.arraycopy(rowInt, 0, c_q_p_r[cycle][counter], 0, rowInt.length);
						counter++;

						//cena
						if(counter == DataProcessor.factoryCount && cycle == 0) {
							cycle++;
							counter = 0;
						}

						//zasoby
						else if(counter == DataProcessor.goodsCount && cycle == 1) {
							cycle++;
							counter = 0;
						}

						//poptavka & produkce
						else if (counter == DataProcessor.goodsCount *  DataProcessor.daysCount) {
							cycle++;
							counter = 0;
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Can't read file");
			System.exit(1);
		}
		finally {
			sc.close();
		}
	}

	/**
	 * Inicializuje datové struktury ze třídy DataProcessor a nastaví
	 * vhodnou velikost počátečním parametrům scénáře
	 * @param rowInt []rowInt řádek ve formě int pole
	 * */
	private void loadInitData(int[] rowInt) {
		DataProcessor.factoryCount = rowInt[0];
		DataProcessor.supermarketCount = rowInt[1];
		DataProcessor.goodsCount = rowInt[2];
		DataProcessor.daysCount = rowInt[3];

		DataProcessor.expenditureCosts = new int[DataProcessor.factoryCount][ DataProcessor.supermarketCount];
		DataProcessor.stocks = new int[DataProcessor.goodsCount][DataProcessor.supermarketCount];
		DataProcessor.factoryProduction = new int[DataProcessor.daysCount * DataProcessor.goodsCount][DataProcessor.factoryCount];
		DataProcessor.goodsDemand = new int[DataProcessor.daysCount * DataProcessor.goodsCount][DataProcessor.supermarketCount];

		c_q_p_r = new int[][][]{DataProcessor.expenditureCosts, DataProcessor.stocks, DataProcessor.factoryProduction, DataProcessor.goodsDemand};

		initLoaded = true;
	}
}
