package application.control;

import application.DailyBankApp;
import application.DailyBankState;
import application.tools.CategorieOperation;
import application.tools.StageManagement;
import application.view.OperationEditorPaneController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.data.CompteCourant;
import model.data.Operation;


public class OperationEditorPane {

	/**
	 * Attributs
	 */
	
	private Stage primaryStage; //la fenêtre
	private OperationEditorPaneController oepc; //le controller relié à la gestion des opérations

	/**
	 * @param _parentStage
	 * @param _dbstate
	 * représente la fenêtre d'enregistrement d'une opération
	 */
	public OperationEditorPane(Stage _parentStage, DailyBankState _dbstate) {

		try {
			FXMLLoader loader = new FXMLLoader(
					OperationEditorPaneController.class.getResource("operationeditorpane.fxml"));
			BorderPane root = loader.load();

			Scene scene = new Scene(root, 500 + 70, 250 + 50);
			scene.getStylesheets().add(DailyBankApp.class.getResource("application.css").toExternalForm());

			this.primaryStage = new Stage();
			this.primaryStage.initModality(Modality.WINDOW_MODAL);
			this.primaryStage.initOwner(_parentStage);
			StageManagement.manageCenteringStage(_parentStage, this.primaryStage);
			this.primaryStage.setScene(scene);
			this.primaryStage.setTitle("Enregistrement d'une opération");
			this.primaryStage.setResizable(false);

			this.oepc = loader.getController();
			this.oepc.initContext(this.primaryStage, _dbstate);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Operation doOperationEditorDialog(CompteCourant cpte, CategorieOperation cm) {
		return this.oepc.displayDialog(cpte, cm);
	}
	
	
	/**
	 * @return le controller de la classe : OperationEditorPane
	 */
	public OperationEditorPaneController getOepc() {
		return oepc;
	}
}