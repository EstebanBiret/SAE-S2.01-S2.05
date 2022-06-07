package application.control;

import application.DailyBankApp;
import application.DailyBankState;
import application.tools.EditionMode;
import application.tools.StageManagement;
import application.view.ClientsManagementController;
import application.view.EmployeEditorPaneController;
import application.view.EmployeManagementController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.data.Employe;


/**
 * @author yann
 * classe dédié à la gestion des informations d'un nouveau client
 */
public class EmployeEditorPane {

	/**
	 * Attribut
	 */
	
	private Stage primaryStage; //la fenêtre principale
	private EmployeEditorPaneController epc; //controller relié à l'édition d'un client

	/**
	 * @param _parentStage
	 * @param _dbstate
	 * représente la fenêtre de l'application de renseignement des informations d'un nouveau client
	 */
	public EmployeEditorPane(Stage _parentStage, DailyBankState _dbstate) {

		try {
			FXMLLoader loader = new FXMLLoader(EmployeManagementController.class.getResource("employeeditorpane.fxml"));
			BorderPane root = loader.load();

			Scene scene = new Scene(root, root.getPrefWidth()+20, root.getPrefHeight()+10);
			scene.getStylesheets().add(DailyBankApp.class.getResource("application.css").toExternalForm());

			this.primaryStage = new Stage();
			this.primaryStage.initModality(Modality.WINDOW_MODAL);
			this.primaryStage.initOwner(_parentStage);
			StageManagement.manageCenteringStage(_parentStage, this.primaryStage);
			this.primaryStage.setScene(scene);
			this.primaryStage.setTitle("Gestion d'un employé");
			this.primaryStage.setResizable(false);

			this.epc = loader.getController();
			this.epc.initContext(this.primaryStage, _dbstate);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param client
	 * @param em
	 * @return 
	 */
	public Employe doEmployeEditorDialog(Employe employe, EditionMode em) {
		return this.epc.displayDialog(employe, em);
	}
}