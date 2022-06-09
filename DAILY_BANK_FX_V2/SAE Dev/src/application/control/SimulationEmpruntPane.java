package application.control;

import application.DailyBankApp;
import application.DailyBankState;
import application.tools.StageManagement;
import application.view.SimulationEmpruntController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SimulationEmpruntPane {
	
	/*
	 * Attributs
	 */
	private Stage primaryStage; //la fenêtre
	private SimulationEmpruntController simu;
	
	/**
	 * @param _parentStage
	 * @param _dbstate
	 */
	public SimulationEmpruntPane(Stage _parentStage) {
		try {
			FXMLLoader loader = new FXMLLoader(SimulationEmpruntController.class.getResource("simulationemprunt.fxml"));
			BorderPane root = loader.load();
			
			Scene scene = new Scene(root);
			scene.getStylesheets().add(DailyBankApp.class.getResource("application.css").toExternalForm());
			
			this.primaryStage = new Stage();
			this.primaryStage.initModality(Modality.WINDOW_MODAL);
			this.primaryStage.initOwner(_parentStage);
			StageManagement.manageCenteringStage(_parentStage, this.primaryStage);
			this.primaryStage.setScene(scene);
			this.primaryStage.setTitle("Simulation d'un emprunt");
			this.primaryStage.setResizable(false);
			
			this.simu = loader.getController();
			this.simu.initContext(_parentStage);
			
			this.primaryStage.showAndWait();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}