import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Vstupní třída programu
 * včetně tvorby GUI pomocí knihovny JavaFX
 * @author Xuan Toan Dinh  a Václav Šíma
 * @version 1.0
 *
 */
public class Main extends Application {
	
	/**Nasteveni optimalni zasoby z textboxu aplikace*/
	public static int optReserve;
	
	/** Počet dní pro který se provede simulace */
	public static int days;
	
	/** Informace z checkboxu pro zajisteni "kreckovani"*/
	public static boolean smart_method;
	/** Informace z checkboxu pro zajisteni vypisu neprodanych produktu*/
	public static boolean factory;
	/** Informace z checkboxu pro zajisteni vypisu zasob*/
	public static boolean storage;

	// GUI prvky z JavaFX
	/**Scéna pro zobrazení*/
	private Scene procDataScene;
	/**Okno aplikace*/
	private Stage window;

	/**Instance tridy DataProcessor pro nacteni a zpracovani dat*/
	private DataProcessor dataProcessor;

	/**
	 * Vstupní bod programu a spuštění JavaFX
	 * @param args = argumenty pro spuštění JavyFX
	 * */
	public static void main(String[] args) {
		launch(args);
	}

	/**
	 * Rozvržení layoutu a inicializace instance třídy {@code DataProcessor}
	 * @param stage stage pro tvorbu layoutu
	 * 
	 * */
	@Override
	public void start(Stage stage) {
		dataProcessor = new DataProcessor();
		dataProcessor.loadData();

		window = stage;

		stage.setTitle("Layout");
		stage.setScene(setScenes());

		stage.setMinHeight(550);
		stage.setMinWidth(500);
		stage.show();
	}
	
	/**
	 * Vytvoření borderpainu a rozvžení scény
	 * @return Scene zobrazená scéna
	 * */
	private Scene setScenes() {
		BorderPane bp = new BorderPane();
		procDataScene = new Scene(getRoot());
		Scene mainMenu = new Scene(bp);

		bp.setCenter(setControlls());

		return mainMenu;
	}
	
	/**
	 * Vytvoření GUI a konfigurace ovládacích prvků
	 * @return GridPane s ovládacími prvky
	 * */
	private Node setControlls() {
		GridPane controlPaneGP = new GridPane();
		controlPaneGP.setHgap(10);
		controlPaneGP.setVgap(10);
		controlPaneGP.setPadding(new Insets(10));

		Button exit = new Button("Exit");
		controlPaneGP.add(exit, 0, 1);
		exit.setPrefSize(70, 35);
		exit.setOnAction(actionEvent -> System.exit(0));

		Label labelOZ = new Label("Optimální zásoba:");
		controlPaneGP.add(labelOZ, 0, 2);

		TextField optimalReserve = new TextField("20");
		controlPaneGP.add(optimalReserve, 0, 3);


		CheckBox method = new CheckBox("Smart method");
		controlPaneGP.add(method, 0, 4);

		CheckBox factory = new CheckBox("Factory");
		controlPaneGP.add(factory, 0, 5);
		CheckBox storage = new CheckBox("Storage");
		controlPaneGP.add(storage, 0, 6);

		Label labelDny = new Label("Počet dní:");
		controlPaneGP.add(labelDny, 0, 7);

		Slider slider = new Slider();
		slider.setMin(0);
		slider.setMax(DataProcessor.daysCount);
		slider.setValue(0);
		slider.setShowTickLabels(true);
		slider.setShowTickMarks(true);
		slider.setMajorTickUnit(50);
		slider.setMinorTickCount(5);
		slider.setBlockIncrement(10);
		slider.valueProperty().addListener((obs, oldval, newVal) ->
				slider.setValue(newVal.intValue()));
		controlPaneGP.add(slider, 0, 8);

		Label  label  = new Label();
		label.textProperty().bind(
				Bindings.format(
						"%.2f",
						slider.valueProperty()
				)
		);
		controlPaneGP.add(label, 1, 8);

		Button start = new Button("Start");
		controlPaneGP.add(start, 0, 0);
		start.setPrefSize(70, 35);
		start.setOnAction(actionEvent -> {
			try {
				startProcessing(optimalReserve, method, factory, storage, slider);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		controlPaneGP.setAlignment(Pos.CENTER);
		return controlPaneGP;
	}
	
	/**
	 * Konfigurace výpočtu podle vstupu od uživatele
	 * @param optRes optimální zásoby
	 * @param method možnost "křečkování"
	 * @param factory co kam a kdy produkovaly a kolik zboží vyprodukovaly zbytečně
	 * @param storage každodenní rozpis skladových zásob
	 * @param days počet dní pro výpočet
	 * */
	private void startProcessing(TextField optRes, CheckBox method, CheckBox factory, CheckBox storage, Slider days) throws IOException {
		String str = optRes.getText();
		int temp = Integer.MAX_VALUE;
		try {
			temp = Integer.parseInt(str);
		} catch (Exception e){
			System.out.println("optimal reserve error");
		}
		optReserve = temp;

		if (method.isSelected()) {Main.smart_method = true;}
		if (factory.isSelected()) {Main.factory = true;}
		if (storage.isSelected()) {Main.storage = true;}
		Main.days = (int) days.getValue();

		dataProcessor.processData();
		procDataScene = new Scene(getRoot());
		window.setScene(procDataScene);
	}

	/**
	 * Vytvoření instance třídy BorderPane pro zobrazení dat
	 * @return instance třídy BorderPane
	 * */
	private Parent getRoot() {
		BorderPane rootPaneBP = new BorderPane();

		rootPaneBP.setCenter(showData());

		return rootPaneBP;
	}

	/**
	 * Do textArea se vypíše informace o simulaci
	 * @return textArea s daty
	 * */
	private Node showData() {
		// Pro výpis informací do okna
		TextArea TA_data = new TextArea();
		TA_data.setEditable(false);

		TA_data.appendText("Simulace proběhla úspěšně\n");
		TA_data.appendText("Výpis najdete v souboru output.txt");

		return TA_data;
	}
}
